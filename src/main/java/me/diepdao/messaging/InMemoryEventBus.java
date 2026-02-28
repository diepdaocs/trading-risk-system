package me.diepdao.messaging;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryEventBus implements EventPublisher, EventSubscriber {
    private final ConcurrentHashMap<Class<? extends Event>, List<Consumer<? extends Event>>> subscribers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        List<Consumer<? extends Event>> consumers = subscribers.get(event.getClass());
        if (consumers != null) {
            for (Consumer<? extends Event> consumer : consumers) {
                ((Consumer<Event>) consumer).accept(event);
            }
        }
    }

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
}
