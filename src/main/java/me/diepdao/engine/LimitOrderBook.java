package me.diepdao.engine;

import me.diepdao.domain.Instrument;
import me.diepdao.domain.Order;
import me.diepdao.domain.Side;
import me.diepdao.domain.Trade;

import java.util.*;

public class LimitOrderBook implements OrderBook {
    private final Instrument instrument;

    // Price -> Queue of Orders (Price-Time Priority)
    private final TreeMap<Double, Queue<Order>> bids = new TreeMap<>(Collections.reverseOrder()); // Highest to lowest
    private final TreeMap<Double, Queue<Order>> asks = new TreeMap<>(); // Lowest to highest

    private double lastTradedPrice = 0.0;

    public LimitOrderBook(Instrument instrument) {
        this.instrument = instrument;
    }

    public synchronized List<Trade> processOrder(Order order) {
        if (!order.getInstrument().equals(instrument)) {
            throw new IllegalArgumentException("Instrument mismatch");
        }

        List<Trade> trades = new ArrayList<>();

        if (order.getSide() == Side.BUY) {
            matchBuyOrder(order, trades);
            if (!order.isFilled() && order.getPrice() > 0) { // Limit order that wasn't fully filled
                addOrderToBook(bids, order);
            }
        } else {
            matchSellOrder(order, trades);
            if (!order.isFilled() && order.getPrice() > 0) {
                addOrderToBook(asks, order);
            }
        }

        return trades;
    }

    private void matchBuyOrder(Order buyOrder, List<Trade> trades) {
        Iterator<Map.Entry<Double, Queue<Order>>> askIterator = asks.entrySet().iterator();

        while (askIterator.hasNext() && !buyOrder.isFilled()) {
            Map.Entry<Double, Queue<Order>> askLevel = askIterator.next();
            double askPrice = askLevel.getKey();

            // If the buy is a limit order and its price is lower than the best ask, no match
            if (buyOrder.getPrice() > 0 && buyOrder.getPrice() < askPrice) {
                break;
            }

            Queue<Order> ordersAtPrice = askLevel.getValue();
            while (!ordersAtPrice.isEmpty() && !buyOrder.isFilled()) {
                Order sellOrder = ordersAtPrice.peek();
                int tradeQuantity = Math.min(buyOrder.getRemainingQuantity(), sellOrder.getRemainingQuantity());

                buyOrder.fill(tradeQuantity);
                sellOrder.fill(tradeQuantity);

                Trade trade = new Trade(instrument, askPrice, tradeQuantity, buyOrder.getId(), sellOrder.getId());
                trades.add(trade);
                lastTradedPrice = askPrice;

                if (sellOrder.isFilled()) {
                    ordersAtPrice.poll(); // Remove fully filled resting order
                }
            }

            if (ordersAtPrice.isEmpty()) {
                askIterator.remove(); // Remove empty price level
            }
        }
    }

    private void matchSellOrder(Order sellOrder, List<Trade> trades) {
        Iterator<Map.Entry<Double, Queue<Order>>> bidIterator = bids.entrySet().iterator();

        while (bidIterator.hasNext() && !sellOrder.isFilled()) {
            Map.Entry<Double, Queue<Order>> bidLevel = bidIterator.next();
            double bidPrice = bidLevel.getKey();

            // If the sell is a limit order and its price is higher than the best bid, no match
            if (sellOrder.getPrice() > 0 && sellOrder.getPrice() > bidPrice) {
                break;
            }

            Queue<Order> ordersAtPrice = bidLevel.getValue();
            while (!ordersAtPrice.isEmpty() && !sellOrder.isFilled()) {
                Order buyOrder = ordersAtPrice.peek();
                int tradeQuantity = Math.min(sellOrder.getRemainingQuantity(), buyOrder.getRemainingQuantity());

                sellOrder.fill(tradeQuantity);
                buyOrder.fill(tradeQuantity);

                Trade trade = new Trade(instrument, bidPrice, tradeQuantity, buyOrder.getId(), sellOrder.getId());
                trades.add(trade);
                lastTradedPrice = bidPrice;

                if (buyOrder.isFilled()) {
                    ordersAtPrice.poll();
                }
            }

            if (ordersAtPrice.isEmpty()) {
                bidIterator.remove();
            }
        }
    }

    private void addOrderToBook(TreeMap<Double, Queue<Order>> book, Order order) {
        book.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
    }

    @Override
    public synchronized Map<Double, Integer> getBids() {
        Map<Double, Integer> aggregated = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Double, Queue<Order>> entry : bids.entrySet()) {
            if (count++ >= 5) break; // Top 5 levels
            aggregated.put(entry.getKey(), entry.getValue().stream().mapToInt(Order::getRemainingQuantity).sum());
        }
        return aggregated;
    }

    @Override
    public synchronized Map<Double, Integer> getAsks() {
        Map<Double, Integer> aggregated = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Double, Queue<Order>> entry : asks.entrySet()) {
            if (count++ >= 5) break; // Top 5 levels
            aggregated.put(entry.getKey(), entry.getValue().stream().mapToInt(Order::getRemainingQuantity).sum());
        }
        return aggregated;
    }

    @Override
    public synchronized double getBestBid() {
        return bids.isEmpty() ? 0.0 : bids.firstKey();
    }

    @Override
    public synchronized double getBestAsk() {
        return asks.isEmpty() ? 0.0 : asks.firstKey();
    }

    @Override
    public synchronized double getMidPrice() {
        double bestBid = getBestBid();
        double bestAsk = getBestAsk();
        if (bestBid > 0 && bestAsk > 0) {
            return (bestBid + bestAsk) / 2.0;
        } else if (lastTradedPrice > 0) {
            return lastTradedPrice; // Fallback to last traded if book is empty on one side
        } else if (bestBid > 0) {
            return bestBid;
        } else if (bestAsk > 0) {
            return bestAsk;
        }
        return 0.0;
    }

    public double getLastTradedPrice() {
        return lastTradedPrice;
    }
}
