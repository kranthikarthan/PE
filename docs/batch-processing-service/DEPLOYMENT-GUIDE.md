# Batch Processing Service - Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Batch Processing Service in various environments, from local development to production.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Production Deployment](#production-deployment)
- [Configuration Management](#configuration-management)
- [Security Configuration](#security-configuration)
- [Monitoring Setup](#monitoring-setup)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Java**: 21 or higher
- **Maven**: 3.8 or higher
- **PostgreSQL**: 14 or higher
- **Redis**: 6 or higher
- **Docker**: 20.10 or higher (for containerized deployment)
- **Kubernetes**: 1.24 or higher (for K8s deployment)

### Network Requirements

- **Ports**: 8080 (HTTP), 5432 (PostgreSQL), 6379 (Redis)
- **Firewall**: Ensure required ports are open
- **DNS**: Configure proper DNS resolution
- **SSL/TLS**: Certificates for HTTPS endpoints

## Local Development

### 1. Environment Setup

```bash
# Clone repository
git clone https://github.com/company/payment-engine.git
cd payment-engine/batch-processing-service

# Set environment variables
export DB_USERNAME=batch_user
export DB_PASSWORD=batch_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 2. Database Setup

```bash
# Create database
createdb payment_engine_batch

# Run migrations
mvn flyway:migrate

# Verify tables
psql -d payment_engine_batch -c "\dt"
```

### 3. Redis Setup

```bash
# Start Redis server
redis-server

# Verify Redis is running
redis-cli ping
```

### 4. Application Startup

```bash
# Start the application
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Verification

```bash
# Check health
curl http://localhost:8080/actuator/health

# Check API documentation
open http://localhost:8080/swagger-ui.html
```

## Docker Deployment

### 1. Build Docker Image

```dockerfile
# Dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "target/batch-processing-service-1.0.0.jar"]
```

```bash
# Build image
docker build -t payment-engine-batch:latest .

# Tag for registry
docker tag payment-engine-batch:latest registry.company.com/payment-engine-batch:1.0.0
```

### 2. Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  batch-processing:
    image: payment-engine-batch:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_USERNAME=batch_user
      - DB_PASSWORD=batch_password
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    volumes:
      - ./logs:/app/logs
      - ./data:/app/data
    restart: unless-stopped

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=payment_engine_batch
      - POSTGRES_USER=batch_user
      - POSTGRES_PASSWORD=batch_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    restart: unless-stopped

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - batch-processing
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
```

### 3. Deploy with Docker Compose

```bash
# Start services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f batch-processing

# Stop services
docker-compose down
```

## Kubernetes Deployment

### 1. Namespace and ConfigMap

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: payment-engine
  labels:
    name: payment-engine
```

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: batch-processing-config
  namespace: payment-engine
data:
  application.yml: |
    spring:
      profiles:
        active: k8s
      datasource:
        url: jdbc:postgresql://postgres-service:5432/payment_engine_batch
        username: batch_user
        password: ${DB_PASSWORD}
      redis:
        host: redis-service
        port: 6379
    batch:
      processing:
        chunk-size: 1000
        timeout-seconds: 3600
```

### 2. Secrets

```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: batch-processing-secrets
  namespace: payment-engine
type: Opaque
data:
  DB_PASSWORD: <base64-encoded-password>
  REDIS_PASSWORD: <base64-encoded-redis-password>
  SFTP_PASSWORD: <base64-encoded-sftp-password>
```

### 3. PostgreSQL Deployment

```yaml
# k8s/postgres.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: payment-engine
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:14
        env:
        - name: POSTGRES_DB
          value: payment_engine_batch
        - name: POSTGRES_USER
          value: batch_user
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: batch-processing-secrets
              key: DB_PASSWORD
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: payment-engine
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: payment-engine
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
```

### 4. Redis Deployment

```yaml
# k8s/redis.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: payment-engine
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:6-alpine
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-storage
          mountPath: /data
      volumes:
      - name: redis-storage
        persistentVolumeClaim:
          claimName: redis-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: payment-engine
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: payment-engine
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

### 5. Batch Processing Service Deployment

```yaml
# k8s/batch-processing.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: batch-processing
  namespace: payment-engine
spec:
  replicas: 3
  selector:
    matchLabels:
      app: batch-processing
  template:
    metadata:
      labels:
        app: batch-processing
    spec:
      containers:
      - name: batch-processing
        image: registry.company.com/payment-engine-batch:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: k8s
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: batch-processing-secrets
              key: DB_PASSWORD
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: batch-processing-secrets
              key: REDIS_PASSWORD
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
      volumes:
      - name: config-volume
        configMap:
          name: batch-processing-config
---
apiVersion: v1
kind: Service
metadata:
  name: batch-processing-service
  namespace: payment-engine
spec:
  selector:
    app: batch-processing
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: batch-processing-hpa
  namespace: payment-engine
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: batch-processing
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 6. Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Create secrets
kubectl apply -f k8s/secrets.yaml

# Create configmap
kubectl apply -f k8s/configmap.yaml

# Deploy PostgreSQL
kubectl apply -f k8s/postgres.yaml

# Deploy Redis
kubectl apply -f k8s/redis.yaml

# Deploy Batch Processing Service
kubectl apply -f k8s/batch-processing.yaml

# Check deployment status
kubectl get pods -n payment-engine
kubectl get services -n payment-engine
```

## Production Deployment

### 1. Infrastructure Requirements

- **Load Balancer**: NGINX or HAProxy
- **Database**: PostgreSQL with replication
- **Cache**: Redis with clustering
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack or similar
- **Security**: SSL/TLS certificates, firewall rules

### 2. High Availability Setup

```yaml
# Production deployment with HA
apiVersion: apps/v1
kind: Deployment
metadata:
  name: batch-processing
  namespace: payment-engine
spec:
  replicas: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 2
  selector:
    matchLabels:
      app: batch-processing
  template:
    metadata:
      labels:
        app: batch-processing
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - batch-processing
              topologyKey: kubernetes.io/hostname
      containers:
      - name: batch-processing
        image: registry.company.com/payment-engine-batch:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 120
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
```

### 3. Database High Availability

```yaml
# PostgreSQL with replication
apiVersion: postgresql.cnpg.io/v1
kind: Cluster
metadata:
  name: postgres-cluster
  namespace: payment-engine
spec:
  instances: 3
  postgresql:
    parameters:
      max_connections: "200"
      shared_buffers: "256MB"
      effective_cache_size: "1GB"
  bootstrap:
    initdb:
      database: payment_engine_batch
      owner: batch_user
      secret:
        name: postgres-credentials
  storage:
    size: 100Gi
    storageClass: fast-ssd
```

### 4. Redis Clustering

```yaml
# Redis cluster
apiVersion: redis.redis.opstreelabs.in/v1beta1
kind: RedisCluster
metadata:
  name: redis-cluster
  namespace: payment-engine
spec:
  clusterSize: 6
  redisExporter:
    enabled: true
  storage:
    volumeClaimTemplate:
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 10Gi
```

## Configuration Management

### 1. Environment-Specific Configuration

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_engine_batch_dev
    username: batch_user
    password: dev_password
  redis:
    host: localhost
    port: 6379
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

batch:
  processing:
    chunk-size: 100
    timeout-seconds: 1800
```

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://postgres-cluster:5432/payment_engine_batch
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  redis:
    host: redis-cluster
    port: 6379
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

batch:
  processing:
    chunk-size: 1000
    timeout-seconds: 7200
    max-concurrent-executions: 10
```

### 2. External Configuration

```yaml
# External config server
spring:
  config:
    import: "configserver:http://config-server:8888"
  cloud:
    config:
      name: batch-processing
      profile: ${SPRING_PROFILES_ACTIVE}
      label: main
```

### 3. Secrets Management

```yaml
# Kubernetes secrets
apiVersion: v1
kind: Secret
metadata:
  name: batch-processing-secrets
  namespace: payment-engine
type: Opaque
data:
  DB_PASSWORD: <base64-encoded>
  REDIS_PASSWORD: <base64-encoded>
  SFTP_PASSWORD: <base64-encoded>
  JWT_SECRET: <base64-encoded>
```

## Security Configuration

### 1. Network Security

```yaml
# Network policies
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: batch-processing-netpol
  namespace: payment-engine
spec:
  podSelector:
    matchLabels:
      app: batch-processing
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
```

### 2. RBAC Configuration

```yaml
# Service account and RBAC
apiVersion: v1
kind: ServiceAccount
metadata:
  name: batch-processing-sa
  namespace: payment-engine
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: batch-processing-role
  namespace: payment-engine
rules:
- apiGroups: [""]
  resources: ["secrets", "configmaps"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: batch-processing-rolebinding
  namespace: payment-engine
subjects:
- kind: ServiceAccount
  name: batch-processing-sa
  namespace: payment-engine
roleRef:
  kind: Role
  name: batch-processing-role
  apiGroup: rbac.authorization.k8s.io
```

### 3. SSL/TLS Configuration

```yaml
# TLS certificate
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: batch-processing-tls
  namespace: payment-engine
spec:
  secretName: batch-processing-tls-secret
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
  - batch-api.company.com
```

## Monitoring Setup

### 1. Prometheus Configuration

```yaml
# Prometheus service monitor
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: batch-processing-monitor
  namespace: payment-engine
spec:
  selector:
    matchLabels:
      app: batch-processing
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

### 2. Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Batch Processing Service",
    "panels": [
      {
        "title": "Job Executions",
        "type": "stat",
        "targets": [
          {
            "expr": "batch_job_executions_total",
            "legendFormat": "Total Executions"
          }
        ]
      },
      {
        "title": "Processing Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(batch_job_records_processed_total[5m])",
            "legendFormat": "Records/Second"
          }
        ]
      }
    ]
  }
}
```

### 3. Alerting Rules

```yaml
# Prometheus alerting rules
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: batch-processing-alerts
  namespace: payment-engine
spec:
  groups:
  - name: batch-processing
    rules:
    - alert: BatchJobFailureRate
      expr: rate(batch_job_executions_failed_total[5m]) > 0.1
      for: 2m
      labels:
        severity: warning
      annotations:
        summary: "High batch job failure rate"
        description: "Batch job failure rate is {{ $value }} failures per second"
    - alert: BatchJobProcessingTime
      expr: batch_job_execution_duration_seconds > 3600
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "Batch job taking too long"
        description: "Batch job has been running for {{ $value }} seconds"
```

## Troubleshooting

### 1. Common Issues

#### Application Won't Start
```bash
# Check logs
kubectl logs -f deployment/batch-processing

# Check configuration
kubectl describe configmap batch-processing-config

# Check secrets
kubectl describe secret batch-processing-secrets
```

#### Database Connection Issues
```bash
# Test database connectivity
kubectl exec -it deployment/batch-processing -- nc -zv postgres-service 5432

# Check database logs
kubectl logs -f deployment/postgres
```

#### Redis Connection Issues
```bash
# Test Redis connectivity
kubectl exec -it deployment/batch-processing -- redis-cli -h redis-service ping

# Check Redis logs
kubectl logs -f deployment/redis
```

### 2. Performance Issues

#### High Memory Usage
```bash
# Check memory usage
kubectl top pods -n payment-engine

# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

#### Slow Job Processing
```bash
# Check job metrics
curl http://localhost:8080/actuator/metrics/batch.job.executions

# Check database performance
kubectl exec -it deployment/postgres -- psql -c "SELECT * FROM pg_stat_activity;"
```

### 3. Scaling Issues

#### Horizontal Scaling
```bash
# Scale deployment
kubectl scale deployment batch-processing --replicas=5

# Check HPA status
kubectl get hpa batch-processing-hpa
```

#### Vertical Scaling
```bash
# Update resource limits
kubectl patch deployment batch-processing -p '{"spec":{"template":{"spec":{"containers":[{"name":"batch-processing","resources":{"limits":{"memory":"4Gi","cpu":"2000m"}}}]}}}}'
```

## Best Practices

### 1. Deployment Best Practices

- Use rolling updates for zero-downtime deployments
- Implement proper health checks and readiness probes
- Use resource limits and requests appropriately
- Implement proper logging and monitoring
- Use secrets for sensitive configuration
- Implement proper backup and disaster recovery

### 2. Security Best Practices

- Use network policies for network segmentation
- Implement proper RBAC for access control
- Use TLS/SSL for secure communication
- Regularly update dependencies and base images
- Implement proper secret management
- Use security scanning tools

### 3. Monitoring Best Practices

- Implement comprehensive metrics collection
- Set up proper alerting rules
- Use distributed tracing for request tracking
- Implement log aggregation and analysis
- Monitor resource usage and performance
- Implement proper dashboards and visualization

## Support

For deployment support and questions:
- Email: devops@company.com
- Documentation: https://docs.company.com/deployment
- Issue Tracker: https://github.com/company/payment-engine/issues
