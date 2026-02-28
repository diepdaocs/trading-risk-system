package me.diepdao.repository;

import me.diepdao.domain.Instrument;
import me.diepdao.domain.Trade;
import java.util.List;

public interface TradeRepository {
    void save(Trade trade);
    List<Trade> findRecentTrades(int limit);
    List<Trade> findByInstrument(Instrument instrument);
}
