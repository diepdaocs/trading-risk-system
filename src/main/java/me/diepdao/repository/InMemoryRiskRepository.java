package me.diepdao.repository;

import me.diepdao.domain.Instrument;
import me.diepdao.domain.RiskMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryRiskRepository implements RiskRepository {
    private final ConcurrentMap<Instrument, RiskMetrics> storage = new ConcurrentHashMap<>();

    @Override
    public RiskMetrics getMetrics(Instrument instrument) {
        return storage.computeIfAbsent(instrument, RiskMetrics::new);
    }

    @Override
    public void saveMetrics(RiskMetrics metrics) {
        storage.put(metrics.getInstrument(), metrics);
    }

    @Override
    public List<RiskMetrics> getAllMetrics() {
        return new ArrayList<>(storage.values());
    }
}
