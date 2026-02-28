package me.diepdao.engine;

import me.diepdao.domain.Instrument;
import me.diepdao.domain.Order;

public interface MatchingEngine {
    void submitOrder(Order order);
    OrderBook getOrderBook(Instrument instrument);
}
