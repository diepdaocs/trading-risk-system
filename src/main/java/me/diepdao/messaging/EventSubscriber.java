package me.diepdao.messaging;

import java.util.function.Consumer;

public interface EventSubscriber {
    <T extends Event> void subscribe(Class<T> eventType, Consumer<T> handler);
}
