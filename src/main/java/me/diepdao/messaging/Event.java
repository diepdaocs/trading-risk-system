package me.diepdao.messaging;

public interface Event {
    String getEventId();
    long getTimestamp();
}
