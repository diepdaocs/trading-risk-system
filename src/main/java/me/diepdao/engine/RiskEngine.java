package me.diepdao.engine;

import me.diepdao.domain.Instrument;

public interface RiskEngine {
    void processMarketDataUpdate(Instrument instrument, double newPrice);
}
