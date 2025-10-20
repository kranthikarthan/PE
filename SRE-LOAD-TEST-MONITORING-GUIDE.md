# SRE Load Test Monitoring Guide
## Payment Engine System

### Overview
This guide provides step-by-step instructions for monitoring the Payment Engine system during load tests, including container health, resource usage, JVM metrics, and performance indicators.

### Prerequisites
- Docker and Docker Compose running
- Access to the Payment Engine environment
- Basic understanding of container monitoring

---

## 1. Initial System Status Check

### 1.1 Container Health Overview
```bash
# Check all container status and health
docker ps -a

# Expected healthy containers:
# - payments-initiation-service (healthy)
# - payments-validation-service (healthy) 
# - payments-routing-service (healthy)
# - payments-account-adapter-service (healthy)
# - payments-transaction-processing-service (healthy)
# - payments-postgres (healthy)
# - payments-redis (healthy)
# - payments-kafka (healthy)
# - payments-jaeger (healthy)
# - payments-prometheus (healthy)
# - payments-grafana (healthy)
```

### 1.2 Identify Unhealthy Containers
```bash
# Look for containers with "unhealthy" status
docker ps -a | grep unhealthy

# Common issues:
# - payments-saga-orchestrator (may show unhealthy but still functional)
# - payments-migrations (exits after completion - this is normal)
```

---

## 2. Real-Time Resource Monitoring

### 2.1 Container Resource Usage
```bash
# Monitor all containers in real-time
docker stats --no-stream

# Key metrics to watch:
# - CPU %: Should be < 80% for stable performance
# - MEM USAGE: Monitor for memory leaks
# - NET I/O: High network activity indicates load
# - BLOCK I/O: Database and disk activity
```

### 2.2 Formatted Resource Report
```bash
# Detailed resource monitoring with formatting
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
```

### 2.3 Continuous Monitoring (Optional)
```bash
# For continuous monitoring during load test
watch -n 5 'docker stats --no-stream'
```

---

## 3. Service-Specific Monitoring

### 3.1 Payment Initiation Service (Primary Load Target)
```bash
# Check process details
docker exec payments-initiation-service ps aux

# Monitor memory and thread usage
docker exec payments-initiation-service sh -c "cat /proc/1/status | grep -E 'Threads|VmRSS|VmSize'"

# Expected values:
# - Threads: 80-150 (depends on load)
# - VmRSS: 500-800MB (memory usage)
# - VmSize: 6-8GB (virtual memory)
```

### 3.2 Database Performance (PostgreSQL)
```bash
# Monitor database container
docker exec payments-postgres ps aux

# Check database connections
docker exec payments-postgres sh -c "cat /proc/1/status | grep -E 'Threads|VmRSS'"
```

### 3.3 Message Queue (Kafka)
```bash
# Monitor Kafka performance
docker exec payments-kafka ps aux

# Check Kafka resource usage
docker exec payments-kafka sh -c "cat /proc/1/status | grep -E 'Threads|VmRSS'"
```

---

## 4. Application Logs Monitoring

### 4.1 Payment Initiation Service Logs
```bash
# Monitor recent logs for errors and performance issues
docker logs --tail 50 payments-initiation-service

# Key log patterns to watch for:
# - "Payment velocity limit exceeded" (business rule limits)
# - "Invalid UUID string" (should be resolved with correlation ID fix)
# - "Duplicate payment request" (idempotency working)
# - Database connection errors
# - Memory-related errors
```

### 4.2 Database Logs
```bash
# Monitor database activity
docker logs --tail 30 payments-postgres

# Look for:
# - Connection pool exhaustion
# - Query performance issues
# - Lock contention
```

### 4.3 Kafka Logs
```bash
# Monitor message queue health
docker logs --tail 30 payments-kafka

# Look for:
# - Consumer lag
# - Broker performance issues
# - Network connectivity problems
```

---

## 5. Performance Thresholds and Alerts

### 5.1 CPU Usage Thresholds
| Service | Normal | Warning | Critical |
|---------|--------|---------|----------|
| Payment Initiation | < 30% | 30-60% | > 60% |
| PostgreSQL | < 20% | 20-40% | > 40% |
| Kafka | < 10% | 10-20% | > 20% |
| Other Services | < 10% | 10-30% | > 30% |

### 5.2 Memory Usage Thresholds
| Service | Normal | Warning | Critical |
|---------|--------|---------|----------|
| Payment Initiation | < 600MB | 600-800MB | > 800MB |
| PostgreSQL | < 200MB | 200-300MB | > 300MB |
| Kafka | < 500MB | 500-700MB | > 700MB |

### 5.3 Thread Count Monitoring
```bash
# Monitor thread count for payment service
docker exec payments-initiation-service sh -c "cat /proc/1/status | grep Threads"

# Normal range: 80-150 threads
# Warning: 150-200 threads
# Critical: > 200 threads
```

---

## 6. Business Logic Monitoring

### 6.1 Velocity Limits
```bash
# Watch for velocity limit messages in logs
docker logs payments-initiation-service | grep "velocity limit"

# Expected behavior:
# - "Payment velocity limit exceeded. Count: 101, Limit: 100"
# - This indicates business rules are working correctly
```

### 6.2 Idempotency Checks
```bash
# Monitor for duplicate payment attempts
docker logs payments-initiation-service | grep "Duplicate payment request"

# This should be minimal during normal operation
```

### 6.3 Database Transaction Monitoring
```bash
# Monitor database activity
docker logs payments-postgres | grep -E "(ERROR|WARN|deadlock|timeout)"
```

---

## 7. Network and I/O Monitoring

### 7.1 Network Activity
```bash
# Monitor network I/O for each service
docker stats --no-stream | grep -E "(payments-initiation|payments-postgres|payments-kafka)"

# High network activity indicates:
# - Active load testing
# - Event publishing
# - Database operations
```

### 7.2 Block I/O Monitoring
```bash
# Monitor disk I/O
docker stats --no-stream | grep -E "BLOCK I/O"

# High block I/O indicates:
# - Database writes
# - Log file writes
# - Potential disk performance issues
```

---

## 8. Advanced JVM Monitoring (Optional)

### 8.1 Thread Dump (Use with Caution)
```bash
# Generate thread dump for analysis (only if needed)
docker exec payments-initiation-service sh -c "kill -3 1"

# This will:
# - Generate thread dump
# - Trigger heap dump
# - Print GC information
# - Use only during performance issues
```

### 8.2 Process Statistics
```bash
# Get detailed process information
docker exec payments-initiation-service sh -c "cat /proc/1/stat"

# Key fields:
# - Field 14+15: CPU time
# - Field 3: Process state
# - Field 22: Virtual memory size
```

---

## 9. Monitoring Dashboard Access

### 9.1 Grafana Dashboard
- URL: http://localhost:3000
- Default credentials: admin/admin
- Monitor system metrics and performance graphs

### 9.2 Prometheus Metrics
- URL: http://localhost:9090
- Query metrics for detailed analysis
- Useful for trend analysis

### 9.3 Jaeger Tracing
- URL: http://localhost:16686
- Monitor request traces
- Identify performance bottlenecks

---

## 10. Load Test Completion Checklist

### 10.1 Final System Check
```bash
# Verify all services are still healthy
docker ps -a | grep -v "Exited"

# Check final resource usage
docker stats --no-stream

# Review final logs for any errors
docker logs --tail 100 payments-initiation-service
```

### 10.2 Performance Summary
- Record peak CPU usage for each service
- Note any memory leaks or unusual patterns
- Document any business rule violations
- Check for any service failures or restarts

---

## 11. Troubleshooting Common Issues

### 11.1 High CPU Usage
```bash
# If payment service CPU > 60%
# 1. Check thread count
docker exec payments-initiation-service sh -c "cat /proc/1/status | grep Threads"

# 2. Check for infinite loops in logs
docker logs payments-initiation-service | grep -i "loop\|error"

# 3. Consider scaling or optimization
```

### 11.2 Memory Issues
```bash
# If memory usage is high
# 1. Check for memory leaks
docker stats --no-stream | grep payments-initiation

# 2. Monitor garbage collection
# (Requires JVM monitoring tools)
```

### 11.3 Database Performance
```bash
# If PostgreSQL CPU > 40%
# 1. Check connection pool
# 2. Monitor query performance
# 3. Consider database optimization
```

---

## 12. Reporting Template

### 12.1 Load Test Monitoring Report
```
Load Test Monitoring Report
==========================
Date: [DATE]
Duration: [DURATION]
Load Test Type: [TEST_TYPE]

Container Health:
- Payment Initiation: [STATUS]
- PostgreSQL: [STATUS]
- Kafka: [STATUS]
- Other Services: [STATUS]

Peak Resource Usage:
- Payment Initiation CPU: [%]
- Payment Initiation Memory: [MB]
- PostgreSQL CPU: [%]
- PostgreSQL Memory: [MB]

Business Logic:
- Velocity Limits Hit: [COUNT]
- Duplicate Requests: [COUNT]
- Database Errors: [COUNT]

Performance Issues:
- [LIST ANY ISSUES]

Recommendations:
- [LIST RECOMMENDATIONS]
```

---

## 13. Quick Reference Commands

### 13.1 Essential Monitoring Commands
```bash
# Quick health check
docker ps -a

# Resource usage
docker stats --no-stream

# Payment service logs
docker logs --tail 50 payments-initiation-service

# Thread count
docker exec payments-initiation-service sh -c "cat /proc/1/status | grep Threads"

# Memory usage
docker exec payments-initiation-service sh -c "cat /proc/1/status | grep VmRSS"
```

### 13.2 Emergency Commands
```bash
# Restart payment service if needed
docker restart payments-initiation-service

# Check service health after restart
docker ps | grep payments-initiation-service

# Monitor logs after restart
docker logs -f payments-initiation-service
```

---

## 14. Notes and Best Practices

### 14.1 Monitoring Best Practices
1. **Start monitoring before load test begins**
2. **Use multiple terminal windows for different monitoring aspects**
3. **Document any anomalies immediately**
4. **Take screenshots of resource usage at peak times**
5. **Monitor business logic compliance, not just technical metrics**

### 14.2 Common Pitfalls
1. **Don't rely solely on CPU usage** - check memory and I/O too
2. **Monitor application logs, not just container stats**
3. **Watch for business rule violations, not just technical errors**
4. **Consider the entire system, not just individual services**

### 14.3 Escalation Criteria
- **CPU > 80% for > 5 minutes**
- **Memory usage > 1GB for payment service**
- **Database errors > 10 per minute**
- **Any service becomes unhealthy**
- **Business rule violations indicate system issues**

---

*This guide should be used in conjunction with the Payment Engine load testing procedures and updated as the system evolves.*
