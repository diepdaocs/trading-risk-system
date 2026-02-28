package me.diepdao.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.diepdao.domain.AssetClass;
import me.diepdao.domain.Instrument;
import me.diepdao.domain.Order;
import me.diepdao.domain.Side;
import me.diepdao.engine.MatchingEngine;
import me.diepdao.engine.RiskEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
    private final MatchingEngine matchingEngine;
    private final RiskEngine riskEngine;

    private final List<Instrument> instruments = Arrays.asList(
            new Instrument("AAPL", AssetClass.EQUITY),
            new Instrument("TSLA", AssetClass.EQUITY),
            new Instrument("EUR/USD", AssetClass.FX),
            new Instrument("USD/JPY", AssetClass.FX)
    );

    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final double[] currentPrices = {150.0, 200.0, 1.10, 145.0}; // Starting prices

    @Inject
    public SimulationService(MatchingEngine matchingEngine, RiskEngine riskEngine) {
        this.matchingEngine = matchingEngine;
        this.riskEngine = riskEngine;
    }

    public void start() {
        logger.info("Starting simulation service...");
        // 1. Random Walk Market Data updates
        scheduler.scheduleAtFixedRate(this::simulateMarketDataUpdate, 0, 2, TimeUnit.SECONDS);

        // 2. Random Order Flow Generation
        scheduler.scheduleAtFixedRate(this::simulateOrderFlow, 1, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.info("Stopping simulation service...");
        scheduler.shutdown();
    }

    private void simulateMarketDataUpdate() {
        try {
            int idx = random.nextInt(instruments.size());
            Instrument inst = instruments.get(idx);

            // Random walk: +/- 0.5%
            double changePercent = (random.nextDouble() - 0.5) * 0.01;
            currentPrices[idx] = currentPrices[idx] * (1.0 + changePercent);

            riskEngine.processMarketDataUpdate(inst, currentPrices[idx]);
            logger.debug("Market Data Update: {} - {}", inst.getSymbol(), currentPrices[idx]);
        } catch (Exception e) {
            logger.error("Error in market data simulation", e);
        }
    }

    private void simulateOrderFlow() {
        try {
            int idx = random.nextInt(instruments.size());
            Instrument inst = instruments.get(idx);
            double basePrice = currentPrices[idx];

            // Generate random order near base price (+/- 1%)
            double orderPrice = basePrice * (1.0 + (random.nextDouble() - 0.5) * 0.02);

            // Round to 2 decimal places
            orderPrice = Math.round(orderPrice * 100.0) / 100.0;

            Side side = random.nextBoolean() ? Side.BUY : Side.SELL;
            int quantity = (random.nextInt(10) + 1) * 10; // 10 to 100 shares/lots

            Order order = new Order(inst, side, orderPrice, quantity);
            matchingEngine.submitOrder(order);

            // Occasionally drop a market order to clear the book
            if (random.nextDouble() < 0.1) {
                Order marketOrder = new Order(inst, side == Side.BUY ? Side.SELL : Side.BUY, 0, quantity);
                matchingEngine.submitOrder(marketOrder);
            }

            logger.debug("Submitted Order: {} {} {} @ {}", side, quantity, inst.getSymbol(), orderPrice);
        } catch (Exception e) {
            logger.error("Error in order flow simulation", e);
        }
    }
}
