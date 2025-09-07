package com.paymentengine.paymentprocessing.eventsourcing;

import com.paymentengine.paymentprocessing.eventsourcing.domain.DomainEvent;
import com.paymentengine.paymentprocessing.eventsourcing.domain.EventStream;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventStore {
    
    // Event Storage
    void saveEvents(String aggregateId, List<DomainEvent> events, int expectedVersion);
    
    void saveEvent(String aggregateId, DomainEvent event, int expectedVersion);
    
    // Event Retrieval
    EventStream getEvents(String aggregateId);
    
    EventStream getEvents(String aggregateId, int fromVersion);
    
    EventStream getEvents(String aggregateId, int fromVersion, int toVersion);
    
    List<DomainEvent> getAllEvents();
    
    List<DomainEvent> getEventsByType(String eventType);
    
    List<DomainEvent> getEventsByAggregateType(String aggregateType);
    
    // Event Querying
    List<DomainEvent> getEventsSince(UUID eventId);
    
    List<DomainEvent> getEventsSince(long timestamp);
    
    List<DomainEvent> getEventsBetween(long fromTimestamp, long toTimestamp);
    
    // Event Metadata
    Optional<DomainEvent> getEvent(UUID eventId);
    
    long getEventCount(String aggregateId);
    
    long getTotalEventCount();
    
    // Event Snapshots
    void saveSnapshot(String aggregateId, Object snapshot, int version);
    
    Optional<Object> getSnapshot(String aggregateId);
    
    Optional<Object> getSnapshot(String aggregateId, int version);
    
    void deleteSnapshot(String aggregateId, int version);
    
    // Event Replay
    void replayEvents(String aggregateId, EventHandler eventHandler);
    
    void replayEvents(String aggregateId, int fromVersion, EventHandler eventHandler);
    
    void replayAllEvents(EventHandler eventHandler);
    
    // Event Projections
    void createProjection(String projectionName, String query);
    
    void updateProjection(String projectionName, String query);
    
    void deleteProjection(String projectionName);
    
    List<String> getProjections();
    
    // Event Archiving
    void archiveEvents(String aggregateId, int upToVersion);
    
    void archiveEvents(long upToTimestamp);
    
    void restoreArchivedEvents(String aggregateId);
    
    // Event Monitoring
    long getEventStoreSize();
    
    List<String> getAggregateIds();
    
    List<String> getEventTypes();
    
    // Event Validation
    boolean validateEvent(DomainEvent event);
    
    List<String> validateEventStream(String aggregateId);
    
    // Event Compression
    void compressEvents(String aggregateId);
    
    void compressAllEvents();
    
    // Event Encryption
    void encryptEvents(String aggregateId);
    
    void decryptEvents(String aggregateId);
    
    boolean areEventsEncrypted(String aggregateId);
    
    // Event Backup
    void backupEvents(String aggregateId);
    
    void backupAllEvents();
    
    void restoreEvents(String aggregateId, String backupId);
    
    List<String> getAvailableBackups(String aggregateId);
    
    // Event Migration
    void migrateEvents(String fromAggregateId, String toAggregateId);
    
    void migrateEventSchema(String aggregateId, String fromSchema, String toSchema);
    
    // Event Analytics
    Map<String, Object> getEventAnalytics(String aggregateId);
    
    List<String> getEventTrends(String aggregateId, String eventType);
    
    // Event Compliance
    boolean isEventCompliant(DomainEvent event);
    
    List<String> getEventComplianceIssues(String aggregateId);
    
    void markEventCompliant(String aggregateId, UUID eventId);
    
    // Event Performance
    void optimizeEventStore();
    
    void rebuildIndexes();
    
    Map<String, Object> getEventStorePerformanceMetrics();
    
    // Event Security
    void setEventAccessControl(String aggregateId, String userId, String permissions);
    
    boolean hasEventAccess(String aggregateId, String userId, String permission);
    
    void auditEventAccess(String aggregateId, String userId, String action);
    
    // Event Notifications
    void subscribeToEvents(String eventType, EventSubscriber subscriber);
    
    void unsubscribeFromEvents(String eventType, EventSubscriber subscriber);
    
    void notifyEventSubscribers(DomainEvent event);
    
    // Event Health Check
    boolean isEventStoreHealthy();
    
    Map<String, Object> getEventStoreHealthStatus();
    
    // Event Versioning
    String getEventVersion(String aggregateId);
    
    List<String> getEventVersions(String aggregateId);
    
    void tagEventVersion(String aggregateId, String version, String tag);
    
    // Event Export/Import
    String exportEvents(String aggregateId, String format);
    
    void importEvents(String aggregateId, String eventData, String format);
    
    // Event Cleanup
    void cleanupOldEvents(String aggregateId, int keepVersions);
    
    void cleanupOldEvents(long olderThanTimestamp);
    
    void cleanupOrphanedEvents();
}