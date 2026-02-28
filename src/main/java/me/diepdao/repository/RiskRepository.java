package me.diepdao.repository;

import me.diepdao.domain.Instrument;
import me.diepdao.domain.RiskMetrics;
import java.util.List;

public interface RiskRepository {
    RiskMetrics getMetrics(Instrument instrument);
    void saveMetrics(RiskMetrics metrics);
    List<RiskMetrics> getAllMetrics();
}
