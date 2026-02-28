package me.diepdao.engine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.diepdao.domain.Instrument;
import me.diepdao.domain.Order;
import me.diepdao.domain.Trade;
import me.diepdao.messaging.EventPublisher;
import me.diepdao.messaging.TradeExecutedEvent;
import me.diepdao.repository.TradeRepository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class BasicMatchingEngine implements MatchingEngine {
    private final ConcurrentMap<Instrument, LimitOrderBook> orderBooks = new ConcurrentHashMap<>();
    private final EventPublisher eventPublisher;
    private final TradeRepository tradeRepository;

    @Inject
    public BasicMatchingEngine(EventPublisher eventPublisher, TradeRepository tradeRepository) {
        this.eventPublisher = eventPublisher;
        this.tradeRepository = tradeRepository;
    }

    @Override
    public void submitOrder(Order order) {
        LimitOrderBook book = orderBooks.computeIfAbsent(order.getInstrument(), LimitOrderBook::new);

        List<Trade> executedTrades = book.processOrder(order);

        for (Trade trade : executedTrades) {
            tradeRepository.save(trade);
            eventPublisher.publish(new TradeExecutedEvent(trade));
        }
    }

    @Override
    public OrderBook getOrderBook(Instrument instrument) {
        return orderBooks.computeIfAbsent(instrument, LimitOrderBook::new);
    }
}
