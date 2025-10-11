package com.paymentengine.paymentprocessing.eventsourcing.domain;

public interface EventSubscriber {
    
    void onEvent(DomainEvent event);
    
    
    // Default implementations
    default boolean isInterestedIn(DomainEvent event) {
        return true;
    }
    
    default String getSubscriberName() {
        return this.getClass().getSimpleName();
    }
    
    default int getPriority() {
        return 0;
    }
    
    default boolean isAsync() {
        return false;
    }
    
    default void onError(DomainEvent event, Exception error) {
        // Default implementation - log error
        System.err.println("Error in subscriber " + getSubscriberName() + " for event " + event.getEventId() + ": " + error.getMessage());
    }
    
    default void onSuccess(DomainEvent event) {
        // Default implementation - no action
    }
    
    default void onComplete(DomainEvent event) {
        // Default implementation - no action
    }
}