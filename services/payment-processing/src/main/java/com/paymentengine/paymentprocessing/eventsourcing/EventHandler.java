package com.paymentengine.paymentprocessing.eventsourcing;

public interface EventHandler {
    void handle(Object event);
    // ... add more methods as needed
}
