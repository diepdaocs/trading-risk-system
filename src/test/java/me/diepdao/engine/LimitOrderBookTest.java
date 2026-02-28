package me.diepdao.engine;

import me.diepdao.domain.AssetClass;
import me.diepdao.domain.Instrument;
import me.diepdao.domain.Order;
import me.diepdao.domain.Side;
import me.diepdao.domain.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LimitOrderBookTest {

    private Instrument instrument;
    private LimitOrderBook orderBook;

    @BeforeEach
    void setUp() {
        instrument = new Instrument("AAPL", AssetClass.EQUITY);
        orderBook = new LimitOrderBook(instrument);
    }

    @Test
    void testAddOrdersWithoutMatching() {
        Order buy1 = new Order(instrument, Side.BUY, 150.0, 100);
        Order buy2 = new Order(instrument, Side.BUY, 149.0, 50);
        Order sell1 = new Order(instrument, Side.SELL, 151.0, 200);

        assertTrue(orderBook.processOrder(buy1).isEmpty());
        assertTrue(orderBook.processOrder(buy2).isEmpty());
        assertTrue(orderBook.processOrder(sell1).isEmpty());

        assertEquals(150.0, orderBook.getBestBid());
        assertEquals(151.0, orderBook.getBestAsk());
        assertEquals(150.5, orderBook.getMidPrice());

        assertEquals(100, orderBook.getBids().get(150.0));
        assertEquals(50, orderBook.getBids().get(149.0));
        assertEquals(200, orderBook.getAsks().get(151.0));
    }

    @Test
    void testFullMatch() {
        Order sell = new Order(instrument, Side.SELL, 150.0, 100);
        orderBook.processOrder(sell);

        Order buy = new Order(instrument, Side.BUY, 150.0, 100);
        List<Trade> trades = orderBook.processOrder(buy);

        assertEquals(1, trades.size());
        Trade trade = trades.get(0);
        assertEquals(150.0, trade.getPrice());
        assertEquals(100, trade.getQuantity());
        assertEquals(buy.getId(), trade.getBuyerOrderId());
        assertEquals(sell.getId(), trade.getSellerOrderId());

        assertTrue(buy.isFilled());
        assertTrue(sell.isFilled());

        // Book should be empty
        assertEquals(0.0, orderBook.getBestBid());
        assertEquals(0.0, orderBook.getBestAsk());
    }

    @Test
    void testPartialMatch() {
        Order sell = new Order(instrument, Side.SELL, 150.0, 100);
        orderBook.processOrder(sell);

        Order buy = new Order(instrument, Side.BUY, 150.0, 60);
        List<Trade> trades = orderBook.processOrder(buy);

        assertEquals(1, trades.size());
        assertTrue(buy.isFilled());
        assertFalse(sell.isFilled());
        assertEquals(40, sell.getRemainingQuantity());

        // Sell order should remain in the book
        assertEquals(0.0, orderBook.getBestBid());
        assertEquals(150.0, orderBook.getBestAsk());
        assertEquals(40, orderBook.getAsks().get(150.0));
    }

    @Test
    void testPriceTimePriority() {
        // Two sell orders at the same price, different times
        Order sell1 = new Order(instrument, Side.SELL, 150.0, 100); // Earlier
        Order sell2 = new Order(instrument, Side.SELL, 150.0, 100); // Later
        Order sell3 = new Order(instrument, Side.SELL, 149.0, 50);  // Better price

        orderBook.processOrder(sell1);
        orderBook.processOrder(sell2);
        orderBook.processOrder(sell3);

        // Buy order comes in that crosses the book
        Order buy = new Order(instrument, Side.BUY, 155.0, 120);
        List<Trade> trades = orderBook.processOrder(buy);

        // Should match against sell3 first (better price), then sell1 (earlier)
        assertEquals(2, trades.size());

        Trade trade1 = trades.get(0);
        assertEquals(149.0, trade1.getPrice());
        assertEquals(50, trade1.getQuantity());
        assertEquals(sell3.getId(), trade1.getSellerOrderId());

        Trade trade2 = trades.get(1);
        assertEquals(150.0, trade2.getPrice());
        assertEquals(70, trade2.getQuantity());
        assertEquals(sell1.getId(), trade2.getSellerOrderId());

        assertTrue(sell3.isFilled());
        assertFalse(sell1.isFilled());
        assertEquals(30, sell1.getRemainingQuantity());
        assertEquals(100, sell2.getRemainingQuantity()); // Completely untouched
    }
}
