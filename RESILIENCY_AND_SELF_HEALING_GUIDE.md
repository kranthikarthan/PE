# Resiliency and Self-Healing Guide

## Overview

This guide provides documentation for the resiliency and self-healing mechanisms implemented in the Payment Engine. These patterns extend the existing infrastructure to ensure comprehensive coverage across all external service calls while avoiding redundancy with existing Istio service mesh, API Gateway, and Resilience4j patterns.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Resiliency Patterns](#resiliency-patterns)
3. [Self-Healing Mechanisms](#self-healing-mechanisms)
4. [Configuration Management](#configuration-management)
5. [Message Queuing System](#message-queuing-system)
6. [Health Monitoring](#health-monitoring)
7. [React Frontend](#react-frontend)
8. [API Reference](#api-reference)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

## Architecture Overview

The resiliency and self-healing system extends existing infrastructure with targeted enhancements:

```
┌─────────────────────────────────────────────────────────────────┐
│                    EXISTING INFRASTRUCTURE                     │
├─────────────────────────────────────────────────────────────────┤
│  Istio Service Mesh    │  Circuit Breaker, Retry, Timeout      │
│  API Gateway          │  Rate Limiting, Circuit Breaker        │
│  Existing Services    │  Resilience4j Patterns                 │
├─────────────────────────────────────────────────────────────────┤
│                    NEW DELTA IMPLEMENTATION                    │
├─────────────────────────────────────────────────────────────────┤
│  ResilientFraudApiService    │  Fraud API Resiliency           │
│  ResilientCoreBankingService │  Core Banking Resiliency        │
│  MessageQueueService         │  Offline Message Queuing        │
│  SelfHealingService          │  Automated Recovery             │
└─────────────────────────────────────────────────────────────────┘
```

## Resiliency Patterns

### 1. Circuit Breaker Pattern

The Circuit Breaker pattern prevents cascading failures by monitoring the success/failure rate of external service calls.

#### Configuration
```json
{
  "failureThreshold": 5,
  "successThreshold": 3,
  "waitDurationSeconds": 60,
  "slowCallThresholdSeconds": 5,
  "slowCallRateThreshold": 0.5,
  "permittedCallsInHalfOpen": 3,
  "automaticTransitionFromOpenToHalfOpen": true
}
```

#### States
- **CLOSED**: Normal operation, calls pass through
- **OPEN**: Circuit is open, calls fail fast
- **HALF_OPEN**: Testing if service has recovered

#### Implementation
```java
@Service
public class ResiliencyServiceImpl implements ResiliencyConfigurationService {
    
    public <T> T executeResilientCall(String serviceName, String tenantId, 
                                    Supplier<T> call, Function<Exception, T> fallback) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName, tenantId);
        
        return circuitBreaker.executeSupplier(() -> {
            try {
                return call.get();
            } catch (Exception e) {
                return fallback.apply(e);
            }
        });
    }
}
```

### 2. Retry Pattern

Implements exponential backoff retry logic for transient failures.

#### Configuration
```json
{
  "maxAttempts": 3,
  "waitDurationSeconds": 1,
  "exponentialBackoffMultiplier": 2.0,
  "maxWaitDurationSeconds": 30,
  "retryOnExceptions": "java.net.ConnectException,java.net.SocketTimeoutException",
  "ignoreExceptions": "java.lang.IllegalArgumentException"
}
```

#### Retry Logic
- **Attempt 1**: Immediate
- **Attempt 2**: Wait 1 second
- **Attempt 3**: Wait 2 seconds
- **Attempt 4**: Wait 4 seconds
- **Maximum**: 30 seconds

### 3. Bulkhead Pattern

Isolates resources to prevent one failing service from affecting others.

#### Configuration
```json
{
  "maxConcurrentCalls": 25,
  "maxWaitDurationSeconds": 5,
  "threadPoolSize": 10,
  "queueCapacity": 100,
  "keepAliveDurationSeconds": 60
}
```

#### Resource Isolation
- **Thread Pool**: Dedicated thread pool per service
- **Queue**: Bounded queue for pending requests
- **Memory**: Isolated memory allocation

### 4. Timeout Pattern

Prevents hanging requests by enforcing time limits.

#### Configuration
```json
{
  "timeoutDurationSeconds": 30,
  "cancelRunningFuture": true,
  "timeoutExceptionMessage": "Service call timed out"
}
```

#### Timeout Behavior
- **Request Timeout**: Configurable per service
- **Cancellation**: Cancel running futures
- **Fallback**: Return fallback response

## Self-Healing Mechanisms

### 1. Health Monitoring

Continuous monitoring of downstream services with configurable health checks.

#### Health Check Configuration
```json
{
  "healthCheckEnabled": true,
  "healthCheckIntervalSeconds": 30,
  "healthCheckTimeoutSeconds": 5,
  "healthCheckEndpoint": "/health",
  "healthCheckMethod": "GET",
  "expectedStatusCodes": "200,201,202"
}
```

#### Health Check Process
1. **Scheduled Checks**: Every 30 seconds (configurable)
2. **Endpoint Validation**: HTTP GET to `/health`
3. **Response Validation**: Check status codes
4. **Cache Results**: 5-minute cache to avoid excessive calls

### 2. Automatic Recovery

When services recover, the system automatically:
1. **Reset Circuit Breakers**: Clear failure state
2. **Reprocess Queued Messages**: Retry failed operations
3. **Update Health Status**: Mark services as healthy
4. **Notify Monitoring**: Update dashboards

### 3. Message Queuing

Failed operations are queued for later processing when services recover.

#### Message States
- **PENDING**: Waiting to be processed
- **PROCESSING**: Currently being processed
- **PROCESSED**: Successfully completed
- **FAILED**: Failed after retries
- **RETRY**: Scheduled for retry
- **EXPIRED**: Expired and no longer valid
- **CANCELLED**: Manually cancelled

#### Retry Logic
- **Exponential Backoff**: 1min, 2min, 4min, 8min, 16min, 32min
- **Max Retries**: Configurable per message type
- **Expiration**: 24-hour default expiry

## Configuration Management

### Database Schema

#### Resiliency Configurations Table
```sql
CREATE TABLE resiliency_configurations (
    id UUID PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    endpoint_pattern VARCHAR(500),
    circuit_breaker_config JSONB,
    retry_config JSONB,
    bulkhead_config JSONB,
    timeout_config JSONB,
    fallback_config JSONB,
    health_check_config JSONB,
    monitoring_config JSONB,
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 1,
    description VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Queued Messages Table
```sql
CREATE TABLE queued_messages (
    id UUID PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE,
    message_type VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    endpoint_url VARCHAR(500),
    http_method VARCHAR(10) DEFAULT 'POST',
    payload JSONB,
    headers JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    priority INTEGER DEFAULT 1,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processing_time_ms BIGINT,
    result JSONB,
    error_message VARCHAR(2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Configuration Examples

#### Fraud API Configuration
```json
{
  "serviceName": "fraud-api-fico",
  "tenantId": "tenant-001",
  "endpointPattern": "/api/v1/fraud/**",
  "circuitBreakerConfig": {
    "failureThreshold": 5,
    "successThreshold": 3,
    "waitDurationSeconds": 60
  },
  "retryConfig": {
    "maxAttempts": 3,
    "waitDurationSeconds": 1,
    "exponentialBackoffMultiplier": 2.0
  },
  "bulkheadConfig": {
    "maxConcurrentCalls": 25,
    "threadPoolSize": 10
  },
  "timeoutConfig": {
    "timeoutDurationSeconds": 30
  },
  "healthCheckConfig": {
    "healthCheckEnabled": true,
    "healthCheckIntervalSeconds": 30,
    "healthCheckEndpoint": "/health"
  }
}
```

#### Core Banking Configuration
```json
{
  "serviceName": "core-banking-debit",
  "tenantId": "tenant-001",
  "endpointPattern": "/api/v1/transactions/debit",
  "circuitBreakerConfig": {
    "failureThreshold": 3,
    "successThreshold": 2,
    "waitDurationSeconds": 30
  },
  "retryConfig": {
    "maxAttempts": 5,
    "waitDurationSeconds": 2,
    "exponentialBackoffMultiplier": 1.5
  },
  "bulkheadConfig": {
    "maxConcurrentCalls": 50,
    "threadPoolSize": 20
  },
  "timeoutConfig": {
    "timeoutDurationSeconds": 60
  }
}
```

## Message Queuing System

### Message Lifecycle

```
┌─────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐
│ PENDING │ -> │PROCESSING│ -> │ PROCESSED │    │  FAILED  │
└─────────┘    └──────────┘    └───────────┘    └────┬─────┘
                                                      │
                                                      v
┌─────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐
│ EXPIRED │ <- │ CANCELLED│    │   RETRY   │ <- │  FAILED  │
└─────────┘    └──────────┘    └───────────┘    └──────────┘
```

### Queue Operations

#### Enqueue Message
```java
String messageId = messageQueueService.enqueueMessage(
    "FRAUD_API_REQUEST",
    "tenant-001",
    "fraud-api-fico",
    "https://fraud-api.example.com/api/v1/assess",
    "POST",
    payload,
    headers,
    metadata
);
```

#### Process Message
```java
CompletableFuture<Map<String, Object>> future = 
    messageQueueService.processMessage(messageId);
Map<String, Object> result = future.get();
```

#### Retry Failed Messages
```java
CompletableFuture<Void> future = 
    messageQueueService.retryFailedMessages();
future.get();
```

### Queue Statistics

The system provides comprehensive statistics:
- **Total Messages**: All messages in the queue
- **Pending Messages**: Waiting to be processed
- **Processing Messages**: Currently being processed
- **Processed Messages**: Successfully completed
- **Failed Messages**: Failed after retries
- **Expired Messages**: Expired and no longer valid
- **Cancelled Messages**: Manually cancelled

## Health Monitoring

### Health Check Implementation

```java
private HealthCheckResult performHealthCheck(ResiliencyConfiguration config) {
    try {
        Map<String, Object> healthCheckConfig = config.getHealthCheckConfig();
        String healthEndpoint = (String) healthCheckConfig.get("healthCheckEndpoint");
        String healthMethod = (String) healthCheckConfig.getOrDefault("healthCheckMethod", "GET");
        Integer timeoutSeconds = (Integer) healthCheckConfig.getOrDefault("healthCheckTimeoutSeconds", 5);

        String baseUrl = extractBaseUrl(config);
        String fullUrl = baseUrl + healthEndpoint;

        long startTime = System.currentTimeMillis();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                fullUrl, HttpMethod.valueOf(healthMethod), entity, String.class);

        long responseTime = System.currentTimeMillis() - startTime;
        boolean isHealthy = isResponseHealthy(response.getStatusCode().value(), 
                                            (String) healthCheckConfig.get("expectedStatusCodes"));

        return new HealthCheckResult(isHealthy, 
                isHealthy ? "OK" : "HTTP " + response.getStatusCode().value(), 
                responseTime, LocalDateTime.now());

    } catch (Exception e) {
        return new HealthCheckResult(false, 
                "Health check failed: " + e.getMessage(), 0, LocalDateTime.now());
    }
}
```

### Monitoring Schedule

- **Health Checks**: Every 30 seconds
- **Queue Processing**: Every 5 minutes
- **Cleanup Tasks**: Every hour
- **Recovery Actions**: On-demand or automatic

## React Frontend

### ResiliencyMonitoring Component

The React frontend provides a dashboard for monitoring resilient services and managing queued messages:

#### Features
- **System Health Overview**: Real-time health status of all services
- **Resilient Services**: Monitor circuit breaker states and metrics
- **Queued Messages**: View and manage queued messages during outages
- **Statistics**: Queue and system metrics
- **Recovery Actions**: Trigger manual recovery and circuit breaker resets

#### Key Components
```typescript
interface ResiliencyService {
  serviceName: string;
  circuitBreakerState: string;
  bulkheadAvailableCalls: number;
  retryMetrics: any;
  timeLimiterMetrics: any;
  lastUpdated: string;
}

interface QueuedMessage {
  id: string;
  messageId: string;
  messageType: string;
  tenantId: string;
  serviceName: string;
  status: 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'FAILED' | 'RETRY' | 'EXPIRED' | 'CANCELLED';
  retryCount: number;
  maxRetries: number;
  nextRetryAt?: string;
  errorMessage?: string;
}
```

#### Usage
```typescript
import ResiliencyMonitoring from './components/ResiliencyMonitoring';

function App() {
  return (
    <div className="App">
      <ResiliencyMonitoring />
    </div>
  );
}
```

## API Reference

### Resilient Services

#### GET /api/resiliency/services
Get status of all resilient services.

**Query Parameters:**
- `tenantId` (optional): Filter by tenant ID

**Response:**
```json
[
  {
    "serviceName": "fraudApi",
    "circuitBreakerState": "CLOSED",
    "bulkheadAvailableCalls": 25,
    "retryMetrics": {
      "numberOfSuccessfulCallsWithRetryAttempt": 100,
      "numberOfFailedCallsWithRetryAttempt": 5
    },
    "timeLimiterMetrics": {
      "numberOfSuccessfulCalls": 95,
      "numberOfFailedCalls": 5
    },
    "lastUpdated": "2024-01-01T00:00:00"
  }
]
```

#### POST /api/resiliency/services/{serviceName}/reset-circuit-breaker
Reset circuit breaker for a specific service.

**Response:**
```json
{
  "serviceName": "fraudApi",
  "status": "CIRCUIT_BREAKER_RESET",
  "message": "Circuit breaker reset successfully",
  "timestamp": "2024-01-01T00:00:00"
}
```

### Queued Messages

#### GET /api/resiliency/queued-messages
Get queued messages with optional filtering.

**Query Parameters:**
- `tenantId` (optional): Filter by tenant ID
- `serviceName` (optional): Filter by service name
- `status` (optional): Filter by status
- `limit` (optional): Maximum number of results (default: 50)

#### POST /api/resiliency/queued-messages/{messageId}/retry
Retry a specific queued message.

#### POST /api/resiliency/queued-messages/{messageId}/cancel
Cancel a specific queued message.

**Request Body:**
```json
{
  "reason": "Cancelled by user"
}
```

#### POST /api/resiliency/queued-messages/reprocess
Reprocess all queued messages for a tenant.

**Query Parameters:**
- `tenantId`: Tenant ID
- `serviceName` (optional): Specific service name

### System Health

#### GET /api/resiliency/health
Get system health status for a tenant.

**Query Parameters:**
- `tenantId`: Tenant ID

**Response:**
```json
{
  "tenantId": "tenant-001",
  "totalServices": 5,
  "healthyServices": 4,
  "unhealthyServices": 1,
  "overallHealth": "DEGRADED",
  "serviceHealth": [
    {
      "serviceName": "fraud-api-fico",
      "healthy": true,
      "responseTimeMs": 150,
      "lastChecked": "2024-01-01T00:00:00",
      "errorMessage": null
    }
  ],
  "timestamp": "2024-01-01T00:00:00"
}
```

#### POST /api/resiliency/monitor
Monitor downstream services for a tenant.

**Query Parameters:**
- `tenantId`: Tenant ID

#### POST /api/resiliency/recovery/trigger
Trigger recovery actions for failed services.

**Query Parameters:**
- `tenantId`: Tenant ID
- `serviceName` (optional): Specific service name

### Queue Statistics

#### GET /api/resiliency/queue-statistics
Get queue statistics.

**Query Parameters:**
- `tenantId` (optional): Filter by tenant ID

**Response:**
```json
{
  "totalMessages": 100,
  "pendingMessages": 10,
  "processingMessages": 5,
  "processedMessages": 80,
  "failedMessages": 3,
  "expiredMessages": 1,
  "cancelledMessages": 1,
  "timestamp": "2024-01-01T00:00:00"
}
```

## Best Practices

### 1. Configuration Management

#### Service-Specific Configurations
- Create separate configurations for each service
- Use meaningful service names (e.g., `fraud-api-fico`, `core-banking-debit`)
- Set appropriate priorities based on business criticality

#### Tenant Isolation
- Always specify tenant ID in configurations
- Use tenant-specific health checks
- Isolate queue processing by tenant

#### Endpoint Patterns
- Use specific endpoint patterns for better matching
- Avoid overly broad patterns that might match unintended endpoints
- Use wildcards appropriately (e.g., `/api/v1/fraud/**`)

### 2. Circuit Breaker Configuration

#### Failure Thresholds
- **Critical Services**: Lower threshold (3-5 failures)
- **Non-Critical Services**: Higher threshold (5-10 failures)
- **External APIs**: Consider rate limiting and quotas

#### Wait Duration
- **Fast Recovery Services**: 30-60 seconds
- **Slow Recovery Services**: 2-5 minutes
- **External APIs**: Consider API rate limits

### 3. Retry Configuration

#### Max Attempts
- **Critical Operations**: 5-7 attempts
- **Non-Critical Operations**: 3-5 attempts
- **External APIs**: Respect rate limits

#### Backoff Strategy
- **Fast Services**: 1.5x multiplier
- **Slow Services**: 2.0x multiplier
- **External APIs**: Consider exponential backoff with jitter

### 4. Timeout Configuration

#### Timeout Duration
- **Fast Services**: 10-30 seconds
- **Slow Services**: 60-120 seconds
- **External APIs**: Consider SLA requirements

#### Cancellation
- Always enable `cancelRunningFuture` for timeouts
- Provide meaningful timeout messages
- Log timeout events for monitoring

### 5. Health Check Configuration

#### Check Frequency
- **Critical Services**: Every 15-30 seconds
- **Non-Critical Services**: Every 60-120 seconds
- **External APIs**: Respect rate limits

#### Endpoint Design
- Use lightweight health check endpoints
- Return minimal data in health responses
- Include service-specific health information

### 6. Message Queuing

#### Message Expiration
- Set appropriate expiration times (24-48 hours)
- Clean up expired messages regularly
- Monitor queue size and growth

#### Retry Logic
- Use exponential backoff for retries
- Set maximum retry limits
- Log retry attempts for debugging

#### Priority Handling
- Use priority queues for critical messages
- Process high-priority messages first
- Monitor queue processing times

### 7. Monitoring and Alerting

#### Metrics Collection
- Monitor circuit breaker states
- Track retry success rates
- Measure response times and timeouts

#### Alerting Rules
- Alert on circuit breaker open states
- Monitor queue size growth
- Track service health degradation

#### Dashboard Design
- Show real-time health status
- Display queue statistics
- Provide recovery action buttons

## Troubleshooting

### Common Issues

#### 1. Circuit Breaker Stuck Open

**Symptoms:**
- Service calls failing immediately
- Circuit breaker state showing OPEN
- No recovery after service is healthy

**Solutions:**
- Check health check configuration
- Verify endpoint accessibility
- Manually reset circuit breaker
- Review failure threshold settings

#### 2. High Queue Growth

**Symptoms:**
- Queue size continuously increasing
- Messages not being processed
- High memory usage

**Solutions:**
- Check service health status
- Review retry configuration
- Increase processing capacity
- Clean up expired messages

#### 3. Slow Recovery

**Symptoms:**
- Services healthy but not processing queued messages
- Long delays in message processing
- Circuit breaker slow to close

**Solutions:**
- Check health check frequency
- Review retry backoff settings
- Verify service endpoint responses
- Check for resource constraints

#### 4. Configuration Not Applied

**Symptoms:**
- New configurations not taking effect
- Old behavior persisting
- Configuration changes ignored

**Solutions:**
- Verify configuration is active
- Check priority settings
- Restart application if needed
- Review configuration validation

### Debugging Tools

#### 1. Health Check Validation
```bash
# Test health check endpoint
curl -v http://service-url/health

# Check expected response codes
curl -I http://service-url/health
```

#### 2. Queue Inspection
```sql
-- Check queue statistics
SELECT status, COUNT(*) FROM queued_messages GROUP BY status;

-- Find stuck messages
SELECT * FROM queued_messages 
WHERE status = 'PROCESSING' 
AND processing_started_at < NOW() - INTERVAL '10 minutes';

-- Check retry patterns
SELECT service_name, retry_count, COUNT(*) 
FROM queued_messages 
WHERE status = 'FAILED' 
GROUP BY service_name, retry_count;
```

#### 3. Configuration Validation
```sql
-- Check active configurations
SELECT service_name, tenant_id, is_active, priority 
FROM resiliency_configurations 
WHERE is_active = true 
ORDER BY priority DESC;

-- Find configuration conflicts
SELECT service_name, tenant_id, COUNT(*) 
FROM resiliency_configurations 
WHERE is_active = true 
GROUP BY service_name, tenant_id 
HAVING COUNT(*) > 1;
```

#### 4. Log Analysis
```bash
# Monitor circuit breaker events
grep "CircuitBreaker" application.log

# Track retry attempts
grep "Retry" application.log

# Monitor health check results
grep "HealthCheck" application.log
```

### Performance Tuning

#### 1. Thread Pool Sizing
- **CPU-bound tasks**: Number of CPU cores
- **I/O-bound tasks**: 2-4x number of CPU cores
- **Mixed workloads**: Start with 2x CPU cores and adjust

#### 2. Queue Capacity
- **High throughput**: 1000+ messages
- **Low latency**: 100-500 messages
- **Memory constrained**: 50-100 messages

#### 3. Health Check Optimization
- **Cache results**: 5-10 minutes
- **Batch checks**: Group multiple services
- **Async processing**: Non-blocking health checks

#### 4. Database Optimization
- **Indexes**: On frequently queried columns
- **Partitioning**: By tenant or date
- **Cleanup**: Regular maintenance tasks

## Conclusion

The resiliency and self-healing system provides comprehensive protection against service failures and automatic recovery mechanisms. By following the best practices and using the provided tools, you can ensure your payment engine remains resilient and self-healing in the face of various failure scenarios.

For additional support or questions, please refer to the API documentation or contact the development team.