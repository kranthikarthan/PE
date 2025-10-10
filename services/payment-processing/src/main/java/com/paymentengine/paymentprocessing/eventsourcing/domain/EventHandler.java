package com.paymentengine.paymentprocessing.eventsourcing.domain;

public interface EventHandler {
    
    void handle(DomainEvent event);
    
    boolean canHandle(DomainEvent event);
    String getHandlerName();
    int getPriority();
    boolean isAsync();
    void onError(DomainEvent event, Exception error);
    void onSuccess(DomainEvent event);
    void onComplete(DomainEvent event);
}