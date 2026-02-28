package me.diepdao.module;

import com.google.inject.AbstractModule;
import me.diepdao.engine.BasicMatchingEngine;
import me.diepdao.engine.BasicRiskEngine;
import me.diepdao.engine.MatchingEngine;
import me.diepdao.engine.RiskEngine;
import me.diepdao.messaging.EventPublisher;
import me.diepdao.messaging.EventSubscriber;
import me.diepdao.messaging.InMemoryEventBus;
import me.diepdao.repository.InMemoryRiskRepository;
import me.diepdao.repository.InMemoryTradeRepository;
import me.diepdao.repository.RiskRepository;
import me.diepdao.repository.TradeRepository;

public class TradingSystemModule extends AbstractModule {
    @Override
    protected void configure() {
        // Storage
        bind(TradeRepository.class).to(InMemoryTradeRepository.class).asEagerSingleton();
        bind(RiskRepository.class).to(InMemoryRiskRepository.class).asEagerSingleton();

        // Messaging - EventBus acts as both Publisher and Subscriber
        InMemoryEventBus eventBus = new InMemoryEventBus();
        bind(EventPublisher.class).toInstance(eventBus);
        bind(EventSubscriber.class).toInstance(eventBus);

        // Core Engines
        bind(MatchingEngine.class).to(BasicMatchingEngine.class).asEagerSingleton();
        bind(RiskEngine.class).to(BasicRiskEngine.class).asEagerSingleton();
    }
}
