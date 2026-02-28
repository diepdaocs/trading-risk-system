package me.diepdao.engine;

import me.diepdao.domain.AssetClass;
import me.diepdao.domain.Instrument;
import me.diepdao.domain.RiskMetrics;
import me.diepdao.domain.Trade;
import me.diepdao.messaging.InMemoryEventBus;
import me.diepdao.messaging.TradeExecutedEvent;
import me.diepdao.repository.InMemoryRiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BasicRiskEngineTest {

    private InMemoryEventBus eventBus;
    private InMemoryRiskRepository riskRepository;
    private BasicRiskEngine riskEngine;
    private Instrument equityInst;
    private Instrument fxInst;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryEventBus();
        riskRepository = new InMemoryRiskRepository();
        riskEngine = new BasicRiskEngine(eventBus, riskRepository);

        equityInst = new Instrument("AAPL", AssetClass.EQUITY);
        fxInst = new Instrument("EUR/USD", AssetClass.FX);
    }

    @Test
    void testTradeExecutionUpdatesRiskMetrics() {
        Trade trade = new Trade(equityInst, 150.0, 10, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        TradeExecutedEvent event = new TradeExecutedEvent(trade);

        eventBus.publish(event);

        RiskMetrics metrics = riskRepository.getMetrics(equityInst);
        assertNotNull(metrics);
        assertEquals(150.0, metrics.getCurrentPrice());
        assertEquals(100, metrics.getNetPosition()); // Initial assignment in BasicRiskEngine logic
        assertTrue(metrics.getUnrealizedPnl() > 0); // (150 - 150*0.99) * 100 > 0
        assertEquals(100.0, metrics.getDelta()); // Delta = net position for Equities
    }

    @Test
    void testMarketDataUpdateReflectsOnPnL() {
        // Initialize position
        Trade trade = new Trade(equityInst, 150.0, 10, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        eventBus.publish(new TradeExecutedEvent(trade));

        RiskMetrics metrics = riskRepository.getMetrics(equityInst);
        double initialPnl = metrics.getUnrealizedPnl();

        // Price goes up -> PnL should increase for a long position
        riskEngine.processMarketDataUpdate(equityInst, 160.0);

        metrics = riskRepository.getMetrics(equityInst);
        assertEquals(160.0, metrics.getCurrentPrice());
        assertTrue(metrics.getUnrealizedPnl() > initialPnl);
    }

    @Test
    void testFxRiskMetrics() {
        Trade trade = new Trade(fxInst, 1.10, 1000, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        eventBus.publish(new TradeExecutedEvent(trade));

        RiskMetrics metrics = riskRepository.getMetrics(fxInst);
        assertNotNull(metrics);
        assertEquals(1.10, metrics.getCurrentPrice());
        assertEquals(100, metrics.getNetPosition());
        assertEquals(110.0, metrics.getDelta(), 0.001); // 100 * 1.10
        assertEquals(0.01, metrics.getDv01(), 0.001); // 100 * 0.0001
    }
}
