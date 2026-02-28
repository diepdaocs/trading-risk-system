package me.diepdao.repository;

import me.diepdao.domain.Instrument;
import me.diepdao.domain.Trade;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InMemoryTradeRepository implements TradeRepository {
    private final List<Trade> trades = new CopyOnWriteArrayList<>();

    @Override
    public void save(Trade trade) {
        trades.add(trade);
    }

    @Override
    public List<Trade> findRecentTrades(int limit) {
        int size = trades.size();
        if (size == 0) return Collections.emptyList();

        int start = Math.max(0, size - limit);
        // Returns the most recent first
        List<Trade> recent = trades.subList(start, size);
        List<Trade> reversed = new java.util.ArrayList<>(recent);
        Collections.reverse(reversed);
        return reversed;
    }

    @Override
    public List<Trade> findByInstrument(Instrument instrument) {
        return trades.stream()
                .filter(t -> t.getInstrument().equals(instrument))
                .collect(Collectors.toList());
    }
}
