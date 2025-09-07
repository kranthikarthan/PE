package com.paymentengine.middleware.eventsourcing.domain;

import java.util.List;
import java.util.UUID;

public class EventStream {
    
    private String aggregateId;
    private int fromVersion;
    private int toVersion;
    private List<DomainEvent> events;
    private UUID streamId;
    private long totalEvents;
    private boolean hasMoreEvents;
    private String nextEventId;
    
    public EventStream() {
        this.streamId = UUID.randomUUID();
    }
    
    public EventStream(String aggregateId, List<DomainEvent> events) {
        this();
        this.aggregateId = aggregateId;
        this.events = events;
        this.totalEvents = events.size();
        this.fromVersion = events.isEmpty() ? 0 : events.get(0).getVersion();
        this.toVersion = events.isEmpty() ? 0 : events.get(events.size() - 1).getVersion();
    }
    
    public EventStream(String aggregateId, List<DomainEvent> events, int fromVersion, int toVersion) {
        this(aggregateId, events);
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }
    
    // Getters and Setters
    public String getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public int getFromVersion() {
        return fromVersion;
    }
    
    public void setFromVersion(int fromVersion) {
        this.fromVersion = fromVersion;
    }
    
    public int getToVersion() {
        return toVersion;
    }
    
    public void setToVersion(int toVersion) {
        this.toVersion = toVersion;
    }
    
    public List<DomainEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<DomainEvent> events) {
        this.events = events;
    }
    
    public UUID getStreamId() {
        return streamId;
    }
    
    public void setStreamId(UUID streamId) {
        this.streamId = streamId;
    }
    
    public long getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }
    
    public boolean isHasMoreEvents() {
        return hasMoreEvents;
    }
    
    public void setHasMoreEvents(boolean hasMoreEvents) {
        this.hasMoreEvents = hasMoreEvents;
    }
    
    public String getNextEventId() {
        return nextEventId;
    }
    
    public void setNextEventId(String nextEventId) {
        this.nextEventId = nextEventId;
    }
    
    // Utility methods
    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }
    
    public int size() {
        return events != null ? events.size() : 0;
    }
    
    public DomainEvent getFirstEvent() {
        return events != null && !events.isEmpty() ? events.get(0) : null;
    }
    
    public DomainEvent getLastEvent() {
        return events != null && !events.isEmpty() ? events.get(events.size() - 1) : null;
    }
    
    public DomainEvent getEvent(int index) {
        return events != null && index >= 0 && index < events.size() ? events.get(index) : null;
    }
    
    public boolean containsEvent(UUID eventId) {
        if (events == null) {
            return false;
        }
        return events.stream().anyMatch(event -> event.getEventId().equals(eventId));
    }
    
    public List<DomainEvent> getEventsByType(String eventType) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .filter(event -> event.getEventType().equals(eventType))
                .toList();
    }
    
    public List<DomainEvent> getEventsSince(int version) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .filter(event -> event.getVersion() >= version)
                .toList();
    }
    
    public List<DomainEvent> getEventsUntil(int version) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .filter(event -> event.getVersion() <= version)
                .toList();
    }
    
    public List<DomainEvent> getEventsBetween(int fromVersion, int toVersion) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .filter(event -> event.getVersion() >= fromVersion && event.getVersion() <= toVersion)
                .toList();
    }
    
    public int getVersionRange() {
        return toVersion - fromVersion + 1;
    }
    
    public boolean isComplete() {
        return !hasMoreEvents;
    }
    
    public boolean isPartial() {
        return hasMoreEvents;
    }
    
    public void addEvent(DomainEvent event) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }
        events.add(event);
        totalEvents++;
        
        if (events.size() == 1) {
            fromVersion = event.getVersion();
        }
        toVersion = event.getVersion();
    }
    
    public void addEvents(List<DomainEvent> newEvents) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }
        events.addAll(newEvents);
        totalEvents += newEvents.size();
        
        if (events.size() == newEvents.size()) {
            fromVersion = newEvents.get(0).getVersion();
        }
        toVersion = newEvents.get(newEvents.size() - 1).getVersion();
    }
    
    public void clear() {
        if (events != null) {
            events.clear();
        }
        totalEvents = 0;
        fromVersion = 0;
        toVersion = 0;
        hasMoreEvents = false;
        nextEventId = null;
    }
    
    @Override
    public String toString() {
        return "EventStream{" +
                "aggregateId='" + aggregateId + '\'' +
                ", fromVersion=" + fromVersion +
                ", toVersion=" + toVersion +
                ", totalEvents=" + totalEvents +
                ", hasMoreEvents=" + hasMoreEvents +
                ", streamId=" + streamId +
                '}';
    }
}