package me.diepdao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import me.diepdao.module.TradingSystemModule;
import me.diepdao.service.SimulationService;
import me.diepdao.web.ApiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Initializing Trading and Risk System...");

        // Setup Dependency Injection
        Injector injector = Guice.createInjector(new TradingSystemModule());

        // Start API Server
        ApiServer apiServer = injector.getInstance(ApiServer.class);
        apiServer.start(8080);

        // Start Simulation Service to generate order flow and market data
        SimulationService simulationService = injector.getInstance(SimulationService.class);
        simulationService.start();

        logger.info("System is up and running.");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down system...");
            simulationService.stop();
            apiServer.stop();
        }));
    }
}
