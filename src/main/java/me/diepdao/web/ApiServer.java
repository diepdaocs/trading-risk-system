package me.diepdao.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import me.diepdao.domain.AssetClass;
import me.diepdao.domain.Instrument;
import me.diepdao.domain.Order;
import me.diepdao.domain.Side;
import me.diepdao.engine.MatchingEngine;
import me.diepdao.engine.OrderBook;
import me.diepdao.repository.RiskRepository;
import me.diepdao.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ApiServer {
    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);
    private final MatchingEngine matchingEngine;
    private final TradeRepository tradeRepository;
    private final RiskRepository riskRepository;
    private Javalin app;

    @Inject
    public ApiServer(MatchingEngine matchingEngine, TradeRepository tradeRepository, RiskRepository riskRepository) {
        this.matchingEngine = matchingEngine;
        this.tradeRepository = tradeRepository;
        this.riskRepository = riskRepository;
    }

    public void start(int port) {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
            // config.requestLogger.http((ctx, ms) -> {
            //     logger.info("{} {} took {} ms", ctx.method(), ctx.path(), ms);
            // });
        }).start(port);

        setupRoutes();
        logger.info("API Server started on port {}", port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private void setupRoutes() {
        // Health Check
        app.get("/health", ctx -> ctx.result("OK"));

        // Get Available Instruments
        app.get("/api/instruments", ctx -> {
            ctx.json(new Instrument[]{
                new Instrument("AAPL", AssetClass.EQUITY),
                new Instrument("TSLA", AssetClass.EQUITY),
                new Instrument("EUR/USD", AssetClass.FX),
                new Instrument("USD/JPY", AssetClass.FX)
            });
        });

        // Get Order Book for Instrument
        app.get("/api/orderbook/{symbol}", ctx -> {
            String symbol = ctx.pathParam("symbol");
            AssetClass assetClass = getAssetClass(symbol);
            if (assetClass == null) {
                ctx.status(404).result("Instrument not found");
                return;
            }

            Instrument inst = new Instrument(symbol, assetClass);
            OrderBook book = matchingEngine.getOrderBook(inst);

            Map<String, Object> response = new HashMap<>();
            response.put("instrument", inst);
            response.put("bids", book.getBids());
            response.put("asks", book.getAsks());
            response.put("bestBid", book.getBestBid());
            response.put("bestAsk", book.getBestAsk());
            response.put("midPrice", book.getMidPrice());

            ctx.json(response);
        });

        // Submit Manual Order
        app.post("/api/orders", ctx -> {
            OrderRequest req = ctx.bodyAsClass(OrderRequest.class);
            AssetClass assetClass = getAssetClass(req.symbol);
            if (assetClass == null) {
                ctx.status(400).result("Invalid instrument symbol");
                return;
            }
            Instrument inst = new Instrument(req.symbol, assetClass);
            Order order = new Order(inst, req.side, req.price, req.quantity);
            matchingEngine.submitOrder(order);
            ctx.status(201).json(order);
        });

        // Get Recent Trades
        app.get("/api/trades", ctx -> {
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(50);
            ctx.json(tradeRepository.findRecentTrades(limit));
        });

        // Get Risk Dashboard Metrics
        app.get("/api/risk", ctx -> {
            ctx.json(riskRepository.getAllMetrics());
        });
    }

    private AssetClass getAssetClass(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "AAPL", "TSLA" -> AssetClass.EQUITY;
            case "EUR/USD", "USD/JPY" -> AssetClass.FX;
            default -> null;
        };
    }

    // DTO for order submission
    public static class OrderRequest {
        public String symbol;
        public Side side;
        public double price;
        public int quantity;
    }
}
