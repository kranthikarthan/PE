# ADR 0002: Apache Kafka for Event-Driven Architecture

## Status
Accepted

## Context
Our payment processing system requires:
- Asynchronous message processing
- High throughput and low latency
- Exactly-once semantics for payment messages
- Event sourcing and audit trail
- Integration between multiple microservices
- Ability to replay events for recovery

## Decision
We will use **Apache Kafka** as our primary event streaming platform with the following configuration:

### Configuration Highlights:
- **Producer Idempotence**: Enabled to prevent duplicate messages
- **Transactional Messaging**: Enabled for exactly-once semantics
- **Topic Versioning**: `<domain>.<event-type>.v<version>` naming convention
- **Dead Letter Queues**: Separate DLQ topics for failed message processing
- **Replication Factor**: 3 for production topics
- **Partitioning**: 3 partitions per topic for parallelism

## Consequences

### Positive:
- ✅ Exactly-once semantics for critical payment messages
- ✅ High throughput (millions of messages per second)
- ✅ Built-in message persistence and replay capabilities
- ✅ Strong ecosystem and tooling
- ✅ Native support for event sourcing patterns
- ✅ Horizontal scalability

### Negative:
- ❌ Operational complexity (requires ZooKeeper, though KRaft mode is emerging)
- ❌ Not suitable for request-response patterns
- ❌ Storage costs for long retention periods
- ❌ Requires careful configuration for optimal performance

### Neutral:
- ⚠️ Need robust monitoring and alerting
- ⚠️ Requires disaster recovery planning
- ⚠️ Schema evolution must be managed carefully

## Alternatives Considered

### RabbitMQ
- ❌ Weaker guarantees for message ordering
- ❌ Lower throughput compared to Kafka
- ✅ Better for traditional message queue patterns

### AWS SQS/SNS
- ❌ Vendor lock-in
- ❌ Limited message replay capabilities
- ✅ Fully managed service

### Apache Pulsar
- ✅ Similar features to Kafka with some enhancements
- ❌ Smaller ecosystem and community
- ❌ Less mature tooling

## Implementation Notes
- All producers use idempotent configuration
- DLQ pattern implemented for failed message processing
- Resilience4j circuit breakers wrap Kafka producers
- Topic auto-creation disabled in production
- Schema registry considered for future implementation

## References
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka Best Practices](https://www.confluent.io/blog/kafka-best-practices/)
- Internal: Kafka Operations Runbook

## Date
2025-10-10

## Author
Payment Engine Team
