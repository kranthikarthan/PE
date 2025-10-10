package com.paymentengine.paymentprocessing.eventsourcing;

public interface EventSubscriber {
    void onEvent(Object event);
    // ... add more methods as needed
}
