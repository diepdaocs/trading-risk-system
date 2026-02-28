package me.diepdao.engine;

import me.diepdao.domain.Order;
import me.diepdao.domain.Trade;
import java.util.List;
import java.util.Map;

public interface OrderBook {
    List<Trade> processOrder(Order order);

    // For UI rendering
    Map<Double, Integer> getBids();
    Map<Double, Integer> getAsks();
    double getBestBid();
    double getBestAsk();
    double getMidPrice();
}
