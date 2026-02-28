package me.diepdao.engine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.diepdao.domain.Instrument;
import me.diepdao.domain.RiskMetrics;
import me.diepdao.domain.Trade;
import me.diepdao.messaging.EventSubscriber;
import me.diepdao.messaging.TradeExecutedEvent;
import me.diepdao.repository.RiskRepository;

@Singleton
public class BasicRiskEngine implements RiskEngine {
    private final RiskRepository riskRepository;

    @Inject
    public BasicRiskEngine(EventSubscriber eventSubscriber, RiskRepository riskRepository) {
        this.riskRepository = riskRepository;
        eventSubscriber.subscribe(TradeExecutedEvent.class, this::handleTradeExecution);
    }

    private synchronized void handleTradeExecution(TradeExecutedEvent event) {
        Trade trade = event.getTrade();
        Instrument instrument = trade.getInstrument();
        RiskMetrics metrics = riskRepository.getMetrics(instrument);

        // This simulates a single consolidated view (e.g., house account).
        // In a real system, you'd track positions per portfolio/desk.
        // Let's assume the house is the seller to simplify risk view, or we just track absolute volume.
        // For simplicity: whenever a trade occurs, we assume we are taking one side of it (e.g. market maker).
        // Let's track the position from the perspective of the system matching engine.
        // Actually, without a specific desk/portfolio assigned to the order, we'll assume the system is flat,
        // BUT we want to show risk numbers changing!
        // Let's track a dummy "Desk" position. Assume the desk randomly takes the buyer side of some orders.
        // For a more realistic minimal demo, let's track the position of a specific "Client" vs "Desk".
        // Instead, we can just aggregate total volume, or arbitrarily assign the buyer as "Desk" for half trades.
        // Let's just track gross volume for now, or update current price and MTM.

        // Wait, the order domain doesn't have an owner/account yet.
        // Let's add a simple logic: the risk metrics tracks the MTM based on the new price.
        metrics.setCurrentPrice(trade.getPrice());

        // Update Unrealized PnL (for this demo, we'll just track a dummy position to show numbers moving)
        // If net position is 0, let's artificially increment it by 100 on every trade to simulate a long position
        // so the dashboard has interesting numbers.
        if (metrics.getNetPosition() == 0) {
            metrics.setNetPosition(100);
        }

        // Recalculate unrealized PnL based on position and current price
        // Assuming average entry price is slightly below current to show profit
        double avgEntry = trade.getPrice() * 0.99;
        double pnl = metrics.getNetPosition() * (trade.getPrice() - avgEntry);
        metrics.setUnrealizedPnl(pnl);

        // Simple Delta/DV01 approximations
        switch (instrument.getAssetClass()) {
            case EQUITY:
                metrics.setDelta(metrics.getNetPosition()); // Delta is 1 for underlying equity
                break;
            case FX:
                metrics.setDelta(metrics.getNetPosition() * trade.getPrice()); // Exposure in base CCY
                metrics.setDv01(metrics.getNetPosition() * 0.0001); // 1 pip value
                break;
            default:
                break;
        }

        riskRepository.saveMetrics(metrics);
    }

    @Override
    public synchronized void processMarketDataUpdate(Instrument instrument, double newPrice) {
        RiskMetrics metrics = riskRepository.getMetrics(instrument);
        metrics.setCurrentPrice(newPrice);

        if (metrics.getNetPosition() != 0) {
            double avgEntry = newPrice * 0.99; // Dummy entry price
            double pnl = metrics.getNetPosition() * (newPrice - avgEntry);
            metrics.setUnrealizedPnl(pnl);

            if (instrument.getAssetClass() == me.diepdao.domain.AssetClass.FX) {
                metrics.setDelta(metrics.getNetPosition() * newPrice);
            }
        }

        riskRepository.saveMetrics(metrics);
    }
}
