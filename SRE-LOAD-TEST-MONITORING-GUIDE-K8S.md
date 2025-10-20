# SRE Load Test Monitoring Guide - Kubernetes Edition
## Payment Engine System

### Overview
This guide provides step-by-step instructions for monitoring the Payment Engine system during load tests in a Kubernetes environment, including pod health, resource usage, JVM metrics, and performance indicators.

### Prerequisites
- kubectl configured and connected to cluster
- Access to the Payment Engine Kubernetes namespace
- Basic understanding of Kubernetes monitoring
- Helm charts deployed (if applicable)

---

## 1. Initial System Status Check

### 1.1 Pod Health Overview
```bash
# Check all pods status and health
kubectl get pods -n payments-engine

# Check pod status with more details
kubectl get pods -n payments-engine -o wide

# Expected healthy pods:
# - payment-initiation-service (Running, Ready)
# - validation-service (Running, Ready)
# - routing-service (Running, Ready)
# - account-adapter-service (Running, Ready)
# - transaction-processing-service (Running, Ready)
# - saga-orchestrator (Running, Ready)
# - postgres (Running, Ready)
# - redis (Running, Ready)
# - kafka (Running, Ready)
# - jaeger (Running, Ready)
# - prometheus (Running, Ready)
# - grafana (Running, Ready)
```

### 1.2 Identify Unhealthy Pods
```bash
# Look for pods with issues
kubectl get pods -n payments-engine | grep -E "(Error|CrashLoopBackOff|Pending|Unknown)"

# Check pod events for issues
kubectl get events -n payments-engine --sort-by='.lastTimestamp'

# Common issues:
# - ImagePullBackOff (image pull issues)
# - CrashLoopBackOff (application crashes)
# - Pending (resource constraints)
```

### 1.3 Service and Ingress Status
```bash
# Check services
kubectl get svc -n payments-engine

# Check ingress
kubectl get ingress -n payments-engine

# Check endpoints
kubectl get endpoints -n payments-engine
```

---

## 2. Real-Time Resource Monitoring

### 2.1 Pod Resource Usage
```bash
# Monitor all pods resource usage
kubectl top pods -n payments-engine

# Monitor specific pods
kubectl top pods -n payments-engine | grep -E "(payment-initiation|postgres|kafka)"

# Key metrics to watch:
# - CPU: Should be < 80% of requests/limits
# - MEMORY: Monitor for memory leaks
# - Network: High network activity indicates load
```

### 2.2 Node Resource Usage
```bash
# Monitor node resource usage
kubectl top nodes

# Check node capacity and allocatable resources
kubectl describe nodes | grep -A 5 "Allocatable:"
```

### 2.3 Continuous Monitoring
```bash
# For continuous monitoring during load test
watch -n 5 'kubectl top pods -n payments-engine'

# Monitor specific services
watch -n 5 'kubectl top pods -n payments-engine | grep payment-initiation'
```

---

## 3. Service-Specific Monitoring

### 3.1 Payment Initiation Service (Primary Load Target)
```bash
# Get pod name for payment initiation service
PAYMENT_POD=$(kubectl get pods -n payments-engine -l app=payment-initiation-service -o jsonpath='{.items[0].metadata.name}')

# Check process details
kubectl exec -n payments-engine $PAYMENT_POD -- ps aux

# Monitor memory and thread usage
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "cat /proc/1/status | grep -E 'Threads|VmRSS|VmSize'"

# Expected values:
# - Threads: 80-150 (depends on load)
# - VmRSS: 500-800MB (memory usage)
# - VmSize: 6-8GB (virtual memory)
```

### 3.2 Database Performance (PostgreSQL)
```bash
# Get PostgreSQL pod name
POSTGRES_POD=$(kubectl get pods -n payments-engine -l app=postgres -o jsonpath='{.items[0].metadata.name}')

# Monitor database container
kubectl exec -n payments-engine $POSTGRES_POD -- ps aux

# Check database connections
kubectl exec -n payments-engine $POSTGRES_POD -- sh -c "cat /proc/1/status | grep -E 'Threads|VmRSS'"
```

### 3.3 Message Queue (Kafka)
```bash
# Get Kafka pod name
KAFKA_POD=$(kubectl get pods -n payments-engine -l app=kafka -o jsonpath='{.items[0].metadata.name}')

# Monitor Kafka performance
kubectl exec -n payments-engine $KAFKA_POD -- ps aux

# Check Kafka resource usage
kubectl exec -n payments-engine $KAFKA_POD -- sh -c "cat /proc/1/status | grep -E 'Threads|VmRSS'"
```

---

## 4. Application Logs Monitoring

### 4.1 Payment Initiation Service Logs
```bash
# Get payment initiation pod
PAYMENT_POD=$(kubectl get pods -n payments-engine -l app=payment-initiation-service -o jsonpath='{.items[0].metadata.name}')

# Monitor recent logs for errors and performance issues
kubectl logs -n payments-engine $PAYMENT_POD --tail=50

# Follow logs in real-time
kubectl logs -n payments-engine $PAYMENT_POD -f

# Key log patterns to watch for:
# - "Payment velocity limit exceeded" (business rule limits)
# - "Invalid UUID string" (should be resolved with correlation ID fix)
# - "Duplicate payment request" (idempotency working)
# - Database connection errors
# - Memory-related errors
```

### 4.2 Database Logs
```bash
# Get PostgreSQL pod
POSTGRES_POD=$(kubectl get pods -n payments-engine -l app=postgres -o jsonpath='{.items[0].metadata.name}')

# Monitor database activity
kubectl logs -n payments-engine $POSTGRES_POD --tail=30

# Look for:
# - Connection pool exhaustion
# - Query performance issues
# - Lock contention
```

### 4.3 Kafka Logs
```bash
# Get Kafka pod
KAFKA_POD=$(kubectl get pods -n payments-engine -l app=kafka -o jsonpath='{.items[0].metadata.name}')

# Monitor message queue health
kubectl logs -n payments-engine $KAFKA_POD --tail=30

# Look for:
# - Consumer lag
# - Broker performance issues
# - Network connectivity problems
```

### 4.4 Multi-Container Pod Logs
```bash
# If pods have multiple containers, specify container name
kubectl logs -n payments-engine $PAYMENT_POD -c payment-initiation-service

# List containers in a pod
kubectl describe pod $PAYMENT_POD -n payments-engine | grep -A 10 "Containers:"
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

### 5.3 Resource Requests and Limits
```bash
# Check resource requests and limits
kubectl describe pod $PAYMENT_POD -n payments-engine | grep -A 10 "Requests:"

# Monitor against limits
kubectl top pod $PAYMENT_POD -n payments-engine --containers
```

### 5.4 Thread Count Monitoring
```bash
# Monitor thread count for payment service
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "cat /proc/1/status | grep Threads"

# Normal range: 80-150 threads
# Warning: 150-200 threads
# Critical: > 200 threads
```

---

## 6. Business Logic Monitoring

### 6.1 Velocity Limits
```bash
# Watch for velocity limit messages in logs
kubectl logs -n payments-engine $PAYMENT_POD | grep "velocity limit"

# Expected behavior:
# - "Payment velocity limit exceeded. Count: 101, Limit: 100"
# - This indicates business rules are working correctly
```

### 6.2 Idempotency Checks
```bash
# Monitor for duplicate payment attempts
kubectl logs -n payments-engine $PAYMENT_POD | grep "Duplicate payment request"

# This should be minimal during normal operation
```

### 6.3 Database Transaction Monitoring
```bash
# Monitor database activity
kubectl logs -n payments-engine $POSTGRES_POD | grep -E "(ERROR|WARN|deadlock|timeout)"
```

---

## 7. Network and I/O Monitoring

### 7.1 Network Activity
```bash
# Monitor network I/O for each service
kubectl top pods -n payments-engine | grep -E "(payment-initiation|postgres|kafka)"

# High network activity indicates:
# - Active load testing
# - Event publishing
# - Database operations
```

### 7.2 Service Mesh Monitoring (if using Istio)
```bash
# Check Istio sidecar status
kubectl get pods -n payments-engine -l app=payment-initiation-service -o jsonpath='{.items[0].metadata.name}' | xargs -I {} kubectl exec -n payments-engine {} -c istio-proxy -- pilot-agent request GET stats

# Monitor service mesh metrics
kubectl exec -n payments-engine $PAYMENT_POD -c istio-proxy -- pilot-agent request GET stats | grep -E "(requests_total|response_code)"
```

### 7.3 Ingress Monitoring
```bash
# Check ingress controller logs
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller --tail=50

# Monitor ingress metrics
kubectl get ingress -n payments-engine -o yaml
```

---

## 8. Advanced JVM Monitoring

### 8.1 Thread Dump (Use with Caution)
```bash
# Generate thread dump for analysis (only if needed)
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "kill -3 1"

# This will:
# - Generate thread dump
# - Trigger heap dump
# - Print GC information
# - Use only during performance issues
```

### 8.2 JVM Metrics via JMX (if enabled)
```bash
# If JMX is enabled, you can use jconsole or similar tools
# Port-forward to access JMX
kubectl port-forward -n payments-engine $PAYMENT_POD 9999:9999

# Then connect with jconsole to localhost:9999
```

### 8.3 Process Statistics
```bash
# Get detailed process information
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "cat /proc/1/stat"

# Key fields:
# - Field 14+15: CPU time
# - Field 3: Process state
# - Field 22: Virtual memory size
```

---

## 9. Monitoring Dashboard Access

### 9.1 Grafana Dashboard
```bash
# Port-forward to access Grafana
kubectl port-forward -n payments-engine svc/grafana 3000:80

# Access at: http://localhost:3000
# Default credentials: admin/admin
# Monitor system metrics and performance graphs
```

### 9.2 Prometheus Metrics
```bash
# Port-forward to access Prometheus
kubectl port-forward -n payments-engine svc/prometheus 9090:80

# Access at: http://localhost:9090
# Query metrics for detailed analysis
# Useful for trend analysis
```

### 9.3 Jaeger Tracing
```bash
# Port-forward to access Jaeger
kubectl port-forward -n payments-engine svc/jaeger 16686:80

# Access at: http://localhost:16686
# Monitor request traces
# Identify performance bottlenecks
```

### 9.4 Kubernetes Dashboard
```bash
# Access Kubernetes dashboard
kubectl proxy

# Access at: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

---

## 10. Load Test Completion Checklist

### 10.1 Final System Check
```bash
# Verify all pods are still healthy
kubectl get pods -n payments-engine

# Check final resource usage
kubectl top pods -n payments-engine

# Review final logs for any errors
kubectl logs -n payments-engine $PAYMENT_POD --tail=100
```

### 10.2 Performance Summary
- Record peak CPU usage for each service
- Note any memory leaks or unusual patterns
- Document any business rule violations
- Check for any pod failures or restarts

---

## 11. Troubleshooting Common Issues

### 11.1 High CPU Usage
```bash
# If payment service CPU > 60%
# 1. Check thread count
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "cat /proc/1/status | grep Threads"

# 2. Check for infinite loops in logs
kubectl logs -n payments-engine $PAYMENT_POD | grep -i "loop\|error"

# 3. Check resource limits
kubectl describe pod $PAYMENT_POD -n payments-engine | grep -A 5 "Limits:"

# 4. Consider horizontal pod autoscaling
kubectl get hpa -n payments-engine
```

### 11.2 Memory Issues
```bash
# If memory usage is high
# 1. Check for memory leaks
kubectl top pod $PAYMENT_POD -n payments-engine --containers

# 2. Check memory limits
kubectl describe pod $PAYMENT_POD -n payments-engine | grep -A 5 "Limits:"

# 3. Monitor garbage collection
# (Requires JVM monitoring tools)
```

### 11.3 Pod Restarts
```bash
# Check why pods are restarting
kubectl describe pod $PAYMENT_POD -n payments-engine

# Check restart count
kubectl get pods -n payments-engine -o wide

# Check previous logs
kubectl logs -n payments-engine $PAYMENT_POD --previous
```

### 11.4 Database Performance
```bash
# If PostgreSQL CPU > 40%
# 1. Check connection pool
kubectl logs -n payments-engine $POSTGRES_POD | grep -i "connection"

# 2. Monitor query performance
kubectl logs -n payments-engine $POSTGRES_POD | grep -i "slow"

# 3. Check resource limits
kubectl describe pod $POSTGRES_POD -n payments-engine | grep -A 5 "Limits:"
```

---

## 12. Scaling and Autoscaling

### 12.1 Horizontal Pod Autoscaling
```bash
# Check HPA status
kubectl get hpa -n payments-engine

# Check HPA details
kubectl describe hpa payment-initiation-service -n payments-engine

# Manual scaling if needed
kubectl scale deployment payment-initiation-service --replicas=3 -n payments-engine
```

### 12.2 Vertical Pod Autoscaling
```bash
# Check VPA status (if enabled)
kubectl get vpa -n payments-engine

# Check VPA recommendations
kubectl describe vpa payment-initiation-service -n payments-engine
```

### 12.3 Cluster Autoscaling
```bash
# Check cluster autoscaler status
kubectl get nodes
kubectl describe nodes | grep -A 5 "Allocatable:"
```

---

## 13. Reporting Template

### 13.1 Load Test Monitoring Report
```
Load Test Monitoring Report - Kubernetes
=======================================
Date: [DATE]
Duration: [DURATION]
Load Test Type: [TEST_TYPE]
Namespace: payments-engine

Pod Health:
- Payment Initiation: [STATUS] ([RESTART_COUNT] restarts)
- PostgreSQL: [STATUS] ([RESTART_COUNT] restarts)
- Kafka: [STATUS] ([RESTART_COUNT] restarts)
- Other Services: [STATUS]

Peak Resource Usage:
- Payment Initiation CPU: [%] (Limit: [%])
- Payment Initiation Memory: [MB] (Limit: [MB])
- PostgreSQL CPU: [%] (Limit: [%])
- PostgreSQL Memory: [MB] (Limit: [MB])

Business Logic:
- Velocity Limits Hit: [COUNT]
- Duplicate Requests: [COUNT]
- Database Errors: [COUNT]

Performance Issues:
- [LIST ANY ISSUES]

Scaling Events:
- [LIST ANY AUTOSCALING EVENTS]

Recommendations:
- [LIST RECOMMENDATIONS]
```

---

## 14. Quick Reference Commands

### 14.1 Essential Monitoring Commands
```bash
# Quick health check
kubectl get pods -n payments-engine

# Resource usage
kubectl top pods -n payments-engine

# Payment service logs
kubectl logs -n payments-engine $PAYMENT_POD --tail=50

# Thread count
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "cat /proc/1/status | grep Threads"

# Memory usage
kubectl exec -n payments-engine $PAYMENT_POD -- sh -c "cat /proc/1/status | grep VmRSS"
```

### 14.2 Emergency Commands
```bash
# Restart payment service if needed
kubectl rollout restart deployment/payment-initiation-service -n payments-engine

# Check service health after restart
kubectl get pods -n payments-engine -l app=payment-initiation-service

# Monitor logs after restart
kubectl logs -n payments-engine $PAYMENT_POD -f
```

### 14.3 Useful Aliases
```bash
# Add to your .bashrc or .zshrc
alias kp='kubectl get pods -n payments-engine'
alias kt='kubectl top pods -n payments-engine'
alias kl='kubectl logs -n payments-engine'
alias kd='kubectl describe pod -n payments-engine'
```

---

## 15. Advanced Monitoring with Prometheus

### 15.1 Custom Metrics Queries
```bash
# CPU usage by service
kubectl exec -n payments-engine $PROMETHEUS_POD -- curl -s 'http://localhost:9090/api/v1/query?query=rate(container_cpu_usage_seconds_total[5m])'

# Memory usage by service
kubectl exec -n payments-engine $PROMETHEUS_POD -- curl -s 'http://localhost:9090/api/v1/query?query=container_memory_usage_bytes'

# Request rate
kubectl exec -n payments-engine $PROMETHEUS_POD -- curl -s 'http://localhost:9090/api/v1/query?query=rate(http_requests_total[5m])'
```

### 15.2 Alerting Rules
```bash
# Check if alerting rules are configured
kubectl get prometheusrules -n payments-engine

# Check alert manager
kubectl get pods -n payments-engine -l app=alertmanager
```

---

## 16. Security and RBAC

### 16.1 Service Account Permissions
```bash
# Check service account permissions
kubectl get serviceaccounts -n payments-engine
kubectl describe serviceaccount payment-initiation-service -n payments-engine

# Check RBAC roles
kubectl get roles -n payments-engine
kubectl get rolebindings -n payments-engine
```

### 16.2 Network Policies
```bash
# Check network policies
kubectl get networkpolicies -n payments-engine

# Check pod-to-pod communication
kubectl exec -n payments-engine $PAYMENT_POD -- nslookup postgres-service
```

---

## 17. Notes and Best Practices

### 17.1 Monitoring Best Practices
1. **Start monitoring before load test begins**
2. **Use multiple terminal windows for different monitoring aspects**
3. **Document any anomalies immediately**
4. **Take screenshots of resource usage at peak times**
5. **Monitor business logic compliance, not just technical metrics**
6. **Use kubectl aliases for efficiency**
7. **Monitor both pod and node resources**

### 17.2 Common Pitfalls
1. **Don't rely solely on CPU usage** - check memory and I/O too
2. **Monitor application logs, not just pod stats**
3. **Watch for business rule violations, not just technical errors**
4. **Consider the entire system, not just individual services**
5. **Check resource limits and requests**
6. **Monitor pod restarts and evictions**

### 17.3 Escalation Criteria
- **CPU > 80% of limits for > 5 minutes**
- **Memory usage > 80% of limits**
- **Pod restarts > 3 times**
- **Database errors > 10 per minute**
- **Any pod becomes unhealthy**
- **Business rule violations indicate system issues**
- **Node resource exhaustion**

---

*This guide should be used in conjunction with the Payment Engine load testing procedures and updated as the system evolves. Ensure you have appropriate RBAC permissions to execute these commands.*
