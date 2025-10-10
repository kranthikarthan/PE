# Payment Processing Service - Operational Runbook

## Overview
This runbook provides step-by-step procedures for common operational tasks, troubleshooting, and incident response for the Payment Processing Service.

## Table of Contents
- [Service Health Checks](#service-health-checks)
- [Deployment Procedures](#deployment-procedures)
- [Incident Response](#incident-response)
- [Common Issues](#common-issues)
- [Maintenance Tasks](#maintenance-tasks)
- [Monitoring & Alerts](#monitoring--alerts)

---

## Service Health Checks

### Quick Health Check
```bash
# Check service health
curl http://localhost:8082/payment-processing/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "redis": {"status": "UP"},
    "circuitBreakers": {"status": "UP"}
  }
}
```

### Detailed Health Check
```bash
# Main health endpoint (includes all components)
curl http://localhost:8082/payment-processing/actuator/health

# Check specific metrics
curl http://localhost:8082/payment-processing/actuator/metrics

# Check circuit breaker status
curl http://localhost:8082/payment-processing/actuator/metrics/resilience4j.circuitbreaker.state

# Check Prometheus metrics
curl http://localhost:8082/payment-processing/actuator/prometheus
```

### Kubernetes Health Check
```bash
# Check pod status
kubectl get pods -n payment-engine -l app=payment-processing

# Check pod logs
kubectl logs -f -n payment-engine deployment/payment-processing

# Check events
kubectl get events -n payment-engine --sort-by='.lastTimestamp'
```

---

## Deployment Procedures

### Rolling Deployment (Kubernetes)

#### Pre-Deployment Checklist
- [ ] Code reviewed and approved
- [ ] All tests passing (unit, integration, e2e)
- [ ] Security scans passed (SonarQube, Trivy)
- [ ] Database migrations tested
- [ ] Rollback plan prepared
- [ ] Monitoring dashboards ready

#### Deployment Steps
```bash
# 1. Build and push Docker image
make docker-build docker-push

# 2. Update Helm values with new image tag
# Edit helm/payment-processing/values.yaml
# image.tag: "v1.2.3"

# 3. Deploy to staging first
helm upgrade --install payment-processing ./helm/payment-processing \
  --namespace payment-engine-staging \
  --values values-staging.yaml \
  --wait --timeout 5m

# 4. Run smoke tests
./scripts/smoke-tests.sh staging

# 5. Deploy to production
helm upgrade --install payment-processing ./helm/payment-processing \
  --namespace payment-engine \
  --values values-prod.yaml \
  --wait --timeout 5m

# 6. Verify deployment
kubectl rollout status deployment/payment-processing -n payment-engine

# 7. Monitor metrics
# Watch Grafana dashboard for 15 minutes
```

#### Post-Deployment Verification
```bash
# Check pod health
kubectl get pods -n payment-engine -l app=payment-processing

# Verify no errors in logs
kubectl logs -n payment-engine deployment/payment-processing --tail=100

# Check metrics
curl https://api.paymentengine.com/payment-processing/actuator/health

# Verify message processing
# Check Kafka consumer lag
kafka-consumer-groups --bootstrap-server kafka:9092 \
  --describe --group payment-processing-service
```

### Rollback Procedure
```bash
# 1. Rollback Helm release
helm rollback payment-processing -n payment-engine

# 2. Verify rollback
kubectl rollout status deployment/payment-processing -n payment-engine

# 3. Check logs
kubectl logs -n payment-engine deployment/payment-processing --tail=100

# 4. Verify service health
curl https://api.paymentengine.com/actuator/health
```

---

## Incident Response

### Severity Levels

| Severity | Description | Response Time | Escalation |
|----------|-------------|---------------|------------|
| P0 (Critical) | Complete service outage | < 15 min | Immediate |
| P1 (High) | Major functionality degraded | < 30 min | 1 hour |
| P2 (Medium) | Minor functionality impacted | < 2 hours | 4 hours |
| P3 (Low) | Cosmetic issues | < 1 day | Next day |

### P0: Service Down

#### Symptoms
- Health check failing
- 5xx errors > 10%
- No messages being processed

#### Response Steps
```bash
# 1. Check pod status
kubectl get pods -n payment-engine -l app=payment-processing

# 2. Check pod logs for errors
kubectl logs -n payment-engine deployment/payment-processing --tail=200

# 3. Check recent deployments
kubectl rollout history deployment/payment-processing -n payment-engine

# 4. Check dependencies
## PostgreSQL
kubectl exec -it postgres-0 -n payment-engine -- psql -U payment_user -c "SELECT 1;"

## Kafka
kubectl exec -it kafka-0 -n payment-engine -- kafka-broker-api-versions \
  --bootstrap-server localhost:9092

## Redis
kubectl exec -it redis-0 -n payment-engine -- redis-cli ping

# 5. If recent deployment caused issue, rollback
helm rollback payment-processing -n payment-engine

# 6. If dependency issue, restart pod
kubectl rollout restart deployment/payment-processing -n payment-engine
```

### P1: High Error Rate

#### Symptoms
- Error rate > 5%
- Increased latency (p95 > 2s)
- Circuit breakers opening

#### Response Steps
```bash
# 1. Check error logs
kubectl logs -n payment-engine deployment/payment-processing \
  --tail=500 | grep ERROR

# 2. Check Grafana dashboard for patterns
# Look for:
# - Specific endpoints with errors
# - Time correlation
# - Resource exhaustion

# 3. Check database connection pool
curl http://localhost:8082/payment-processing/actuator/metrics/hikaricp.connections.active

# 4. Check Kafka consumer lag
kafka-consumer-groups --bootstrap-server kafka:9092 \
  --describe --group payment-processing-service

# 5. Scale up if needed
kubectl scale deployment payment-processing --replicas=5 -n payment-engine

# 6. Check circuit breaker status
curl http://localhost:8081/actuator/health | jq '.components.circuitBreakers'
```

### P2: Messages in DLQ

#### Symptoms
- Messages accumulating in Dead Letter Queue
- Alert from monitoring system

#### Response Steps
```bash
# 1. Check DLQ topic
kafka-console-consumer --bootstrap-server kafka:9092 \
  --topic iso20022.dlq.v1 \
  --from-beginning --max-messages 10

# 2. Analyze error patterns
kubectl logs -n payment-engine deployment/payment-processing \
  | grep DLQ | tail -50

# 3. Check if validation error
# Review message format

# 4. Check if external service error
# Verify clearing system connectivity

# 5. Replay messages if issue resolved
./scripts/replay-dlq-messages.sh
```

---

## Common Issues

### Issue: High CPU Usage

**Symptoms**: CPU usage > 80%

**Diagnosis**:
```bash
# 1. Check pod resources
kubectl top pods -n payment-engine -l app=payment-processing

# 2. Get thread dump
kubectl exec -it <pod-name> -n payment-engine -- \
  jcmd 1 Thread.print > thread-dump.txt

# 3. Check GC logs
kubectl logs -n payment-engine <pod-name> | grep GC
```

**Resolution**:
```bash
# 1. Scale horizontally
kubectl scale deployment payment-processing --replicas=5 -n payment-engine

# 2. Increase CPU limits
# Edit helm/payment-processing/values.yaml
# resources.limits.cpu: "2000m" → "3000m"

# 3. Redeploy
helm upgrade payment-processing ./helm/payment-processing -n payment-engine
```

### Issue: Database Connection Pool Exhausted

**Symptoms**: "Connection pool exhausted" errors

**Diagnosis**:
```bash
# Check active connections
curl http://localhost:8082/payment-processing/actuator/metrics/hikaricp.connections.active

# Check pool configuration
kubectl describe configmap payment-processing -n payment-engine
```

**Resolution**:
```bash
# 1. Increase pool size
# Edit application.yml or application-prod.yml
# Current default: maximum-pool-size: 20, minimum-idle: 5
# Increase to: maximum-pool-size: 50, minimum-idle: 10

# 2. Check for connection leaks
kubectl logs -n payment-engine deployment/payment-processing \
  | grep "Connection leak"

# 3. Restart pods
kubectl rollout restart deployment/payment-processing -n payment-engine
```

### Issue: Kafka Consumer Lag

**Symptoms**: Consumer lag > 1000 messages

**Diagnosis**:
```bash
# Check consumer lag (use actual consumer group name)
kafka-consumer-groups --bootstrap-server kafka:9092 \
  --describe --group payment-processing-service
```

**Resolution**:
```bash
# 1. Scale consumers
# Edit helm/payment-processing/values.yaml
# replicaCount: 3 → 5

# 2. Increase partition count (if needed)
kafka-topics --bootstrap-server kafka:9092 \
  --alter --topic iso20022.pain001.v1 --partitions 6

# 3. Monitor lag reduction
watch 'kafka-consumer-groups --bootstrap-server kafka:9092 \
  --describe --group payment-processing-service'
```

---

## Maintenance Tasks

### Database Backup
```bash
# Daily backup (automated via CronJob if configured)
kubectl create job manual-backup-$(date +%Y%m%d) \
  --from=cronjob/postgres-backup -n payment-engine

# Or use manual pg_dump
kubectl exec -it postgres-0 -n payment-engine -- \
  pg_dump -U postgres payment_engine > backup-$(date +%Y%m%d).sql

# Verify backup
kubectl logs -n payment-engine job/manual-backup-20251010
```

### Log Rotation
```bash
# Logs are configured in application.yml
# Default location: logs/payment-processing.log
# No automatic rotation in application.yml - add if needed

# For production with logback-spring.xml:
# - Max file size: 100MB
# - Max history: 30 days (when configured)
# - Total size cap: 3GB (when configured)

# Manual cleanup if needed (when logs are in default location)
find logs/ -name "*.log.*" -mtime +30 -delete
```

### Certificate Renewal
```bash
# Certificates are managed by cert-manager
# Auto-renewal 30 days before expiry

# Check certificate expiry
kubectl get certificate -n payment-engine

# Manual renewal if needed
kubectl delete certificate paymentengine-tls -n payment-engine
# cert-manager will automatically recreate
```

---

## Monitoring & Alerts

### Key Metrics to Monitor

| Metric | Threshold | Alert Level |
|--------|-----------|-------------|
| Error rate | > 5% | P1 |
| P95 latency | > 2s | P1 |
| CPU usage | > 80% | P2 |
| Memory usage | > 85% | P2 |
| Consumer lag | > 1000 | P2 |
| Pod restarts | > 3 in 10min | P1 |
| DB connection pool | > 90% | P2 |

### Alert Channels
- **PagerDuty**: P0, P1 incidents
- **Slack**: All incidents (#payment-engine-alerts)
- **Email**: P2, P3 incidents

### Grafana Dashboards
- **Service Overview**: https://grafana.paymentengine.com/d/payment-processing
- **Kafka Metrics**: https://grafana.paymentengine.com/d/kafka-overview
- **Database Metrics**: https://grafana.paymentengine.com/d/postgres-overview

---

## Contacts

### On-Call Rotation
- **Primary**: Check PagerDuty schedule
- **Secondary**: Check PagerDuty schedule
- **Escalation**: Engineering Manager

### Support Channels
- **Slack**: #payment-engine-oncall
- **Email**: oncall@paymentengine.com
- **Phone**: +1-XXX-XXX-XXXX (Emergency only)

### Vendor Support
- **AWS**: Support case via console
- **Confluent (Kafka)**: support@confluent.io
- **Datadog**: support@datadoghq.com

---

## References
- [Service Architecture](../architecture/payment-processing.md)
- [API Documentation](../api/iso20022-api.md)
- [Deployment Guide](../deployment/kubernetes-deployment.md)
- [Security Policies](../security/security-policies.md)

---

**Last Updated**: 2025-10-10  
**Version**: 1.0  
**Owner**: Payment Engine SRE Team
