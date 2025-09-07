package com.paymentengine.middleware.eventsourcing.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AggregateRoot {
    
    private String aggregateId;
    private int version;
    private List<DomainEvent> uncommittedEvents;
    private List<DomainEvent> committedEvents;
    private boolean isDeleted;
    private String aggregateType;
    
    public AggregateRoot() {
        this.aggregateId = UUID.randomUUID().toString();
        this.version = 0;
        this.uncommittedEvents = new ArrayList<>();
        this.committedEvents = new ArrayList<>();
        this.isDeleted = false;
        this.aggregateType = this.getClass().getSimpleName();
    }
    
    public AggregateRoot(String aggregateId) {
        this();
        this.aggregateId = aggregateId;
    }
    
    // Getters and Setters
    public String getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return uncommittedEvents;
    }
    
    public void setUncommittedEvents(List<DomainEvent> uncommittedEvents) {
        this.uncommittedEvents = uncommittedEvents;
    }
    
    public List<DomainEvent> getCommittedEvents() {
        return committedEvents;
    }
    
    public void setCommittedEvents(List<DomainEvent> committedEvents) {
        this.committedEvents = committedEvents;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public String getAggregateType() {
        return aggregateType;
    }
    
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }
    
    // Event handling methods
    protected void applyEvent(DomainEvent event) {
        event.setAggregateId(this.aggregateId);
        event.setVersion(this.version + 1);
        this.uncommittedEvents.add(event);
        this.version++;
        handleEvent(event);
    }
    
    protected abstract void handleEvent(DomainEvent event);
    
    public void markEventsAsCommitted() {
        this.committedEvents.addAll(this.uncommittedEvents);
        this.uncommittedEvents.clear();
    }
    
    public void loadFromHistory(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            this.version = event.getVersion();
            handleEvent(event);
        }
    }
    
    public void loadFromSnapshot(Object snapshot, int version) {
        this.version = version;
        loadFromSnapshot(snapshot);
    }
    
    protected abstract void loadFromSnapshot(Object snapshot);
    
    public abstract Object createSnapshot();
    
    // Utility methods
    public boolean hasUncommittedEvents() {
        return !uncommittedEvents.isEmpty();
    }
    
    public int getUncommittedEventCount() {
        return uncommittedEvents.size();
    }
    
    public int getCommittedEventCount() {
        return committedEvents.size();
    }
    
    public int getTotalEventCount() {
        return uncommittedEvents.size() + committedEvents.size();
    }
    
    public DomainEvent getLastUncommittedEvent() {
        return uncommittedEvents.isEmpty() ? null : uncommittedEvents.get(uncommittedEvents.size() - 1);
    }
    
    public DomainEvent getLastCommittedEvent() {
        return committedEvents.isEmpty() ? null : committedEvents.get(committedEvents.size() - 1);
    }
    
    public DomainEvent getLastEvent() {
        if (!uncommittedEvents.isEmpty()) {
            return getLastUncommittedEvent();
        }
        return getLastCommittedEvent();
    }
    
    public List<DomainEvent> getEventsByType(String eventType) {
        List<DomainEvent> result = new ArrayList<>();
        
        for (DomainEvent event : committedEvents) {
            if (event.getEventType().equals(eventType)) {
                result.add(event);
            }
        }
        
        for (DomainEvent event : uncommittedEvents) {
            if (event.getEventType().equals(eventType)) {
                result.add(event);
            }
        }
        
        return result;
    }
    
    public List<DomainEvent> getEventsSince(int version) {
        List<DomainEvent> result = new ArrayList<>();
        
        for (DomainEvent event : committedEvents) {
            if (event.getVersion() >= version) {
                result.add(event);
            }
        }
        
        for (DomainEvent event : uncommittedEvents) {
            if (event.getVersion() >= version) {
                result.add(event);
            }
        }
        
        return result;
    }
    
    public List<DomainEvent> getEventsUntil(int version) {
        List<DomainEvent> result = new ArrayList<>();
        
        for (DomainEvent event : committedEvents) {
            if (event.getVersion() <= version) {
                result.add(event);
            }
        }
        
        for (DomainEvent event : uncommittedEvents) {
            if (event.getVersion() <= version) {
                result.add(event);
            }
        }
        
        return result;
    }
    
    public List<DomainEvent> getEventsBetween(int fromVersion, int toVersion) {
        List<DomainEvent> result = new ArrayList<>();
        
        for (DomainEvent event : committedEvents) {
            if (event.getVersion() >= fromVersion && event.getVersion() <= toVersion) {
                result.add(event);
            }
        }
        
        for (DomainEvent event : uncommittedEvents) {
            if (event.getVersion() >= fromVersion && event.getVersion() <= toVersion) {
                result.add(event);
            }
        }
        
        return result;
    }
    
    public boolean hasEventOfType(String eventType) {
        for (DomainEvent event : committedEvents) {
            if (event.getEventType().equals(eventType)) {
                return true;
            }
        }
        
        for (DomainEvent event : uncommittedEvents) {
            if (event.getEventType().equals(eventType)) {
                return true;
            }
        }
        
        return false;
    }
    
    public int getEventCountByType(String eventType) {
        int count = 0;
        
        for (DomainEvent event : committedEvents) {
            if (event.getEventType().equals(eventType)) {
                count++;
            }
        }
        
        for (DomainEvent event : uncommittedEvents) {
            if (event.getEventType().equals(eventType)) {
                count++;
            }
        }
        
        return count;
    }
    
    public void clearUncommittedEvents() {
        this.uncommittedEvents.clear();
    }
    
    public void clearCommittedEvents() {
        this.committedEvents.clear();
    }
    
    public void clearAllEvents() {
        this.uncommittedEvents.clear();
        this.committedEvents.clear();
    }
    
    public void delete() {
        this.isDeleted = true;
    }
    
    public void restore() {
        this.isDeleted = false;
    }
    
    @Override
    public String toString() {
        return "AggregateRoot{" +
                "aggregateId='" + aggregateId + '\'' +
                ", version=" + version +
                ", uncommittedEvents=" + uncommittedEvents.size() +
                ", committedEvents=" + committedEvents.size() +
                ", isDeleted=" + isDeleted +
                ", aggregateType='" + aggregateType + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AggregateRoot that = (AggregateRoot) o;
        
        return aggregateId != null ? aggregateId.equals(that.aggregateId) : that.aggregateId == null;
    }
    
    @Override
    public int hashCode() {
        return aggregateId != null ? aggregateId.hashCode() : 0;
    }
}