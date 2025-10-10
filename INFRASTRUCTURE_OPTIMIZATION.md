# Infrastructure & 3rd Party Component Optimization
## High TPS Payment Engine Configuration

### PostgreSQL Optimization

#### postgresql.conf for 2000 TPS
```conf
# Memory Configuration
shared_buffers = 8GB                    # 25% of RAM
effective_cache_size = 24GB             # 75% of RAM
work_mem = 256MB                        # For complex queries
maintenance_work_mem = 2GB              # For maintenance

# Connection Settings
max_connections = 500                   # Maximum connections
shared_preload_libraries = 'pg_stat_statements'

# WAL Configuration
wal_buffers = 64MB                      # WAL buffer size
checkpoint_completion_target = 0.9      # Spread checkpoints
wal_level = replica                     # For replication
max_wal_size = 4GB                      # Maximum WAL size
min_wal_size = 1GB                      # Minimum WAL size

# Query Planning
random_page_cost = 1.1                  # SSD optimization
effective_io_concurrency = 200          # SSD optimization
```

### Redis Cluster Configuration

#### redis.conf for High TPS
```conf
# Memory Management
maxmemory 8gb
maxmemory-policy allkeys-lru

# Network Optimization
tcp-keepalive 60
timeout 0
tcp-backlog 511

# Persistence Optimization
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes

# Performance Tuning
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
set-max-intset-entries 512
zset-max-ziplist-entries 128
```

### Kafka Cluster Configuration

#### server.properties for High Throughput
```properties
# Broker Configuration
num.network.threads=8
num.io.threads=16
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400

# Log Configuration
log.dirs=/kafka-logs
num.partitions=12
num.recovery.threads.per.data.dir=1

# Replication
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=2

# Performance
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
```

#### Producer Configuration
```properties
bootstrap.servers=kafka-1:9092,kafka-2:9092,kafka-3:9092
acks=1
retries=0
batch.size=131072
linger.ms=50
buffer.memory=268435456
compression.type=lz4
max.in.flight.requests.per.connection=20
enable.idempotence=false
```

#### Consumer Configuration
```properties
bootstrap.servers=kafka-1:9092,kafka-2:9092,kafka-3:9092
group.id=payment-processor
auto.offset.reset=earliest
enable.auto.commit=false
max.poll.records=2000
fetch.min.bytes=4096
fetch.max.wait.ms=50
session.timeout.ms=30000
heartbeat.interval.ms=10000
```

### NGINX Load Balancer

#### nginx.conf for High TPS
```nginx
worker_processes auto;
worker_cpu_affinity auto;
worker_rlimit_nofile 65535;

events {
    worker_connections 4096;
    use epoll;
    multi_accept on;
}

http {
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    keepalive_requests 1000;
    
    upstream payment_engine_backend {
        least_conn;
        server payment-engine-1:8080 max_fails=3 fail_timeout=30s;
        server payment-engine-2:8080 max_fails=3 fail_timeout=30s;
        server payment-engine-3:8080 max_fails=3 fail_timeout=30s;
        server payment-engine-4:8080 max_fails=3 fail_timeout=30s;
        
        keepalive 32;
        keepalive_requests 1000;
        keepalive_timeout 60s;
    }
    
    server {
        listen 80;
        server_name payment-engine.com;
        
        location / {
            proxy_pass http://payment_engine_backend;
            proxy_http_version 1.1;
            proxy_set_header Connection "";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            
            proxy_buffering on;
            proxy_buffer_size 4k;
            proxy_buffers 8 4k;
            proxy_busy_buffers_size 8k;
            
            proxy_connect_timeout 5s;
            proxy_send_timeout 10s;
            proxy_read_timeout 10s;
        }
    }
}
```

### Kubernetes Deployment

#### payment-engine-deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-engine
spec:
  replicas: 10
  selector:
    matchLabels:
      app: payment-engine
  template:
    metadata:
      labels:
        app: payment-engine
    spec:
      containers:
      - name: payment-engine
        image: payment-engine:latest
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_POOL_SIZE
          value: "200"
        - name: REDIS_POOL_SIZE
          value: "100"
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: payment-engine-service
spec:
  selector:
    app: payment-engine
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Monitoring Configuration

#### Prometheus Configuration
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'payment-engine'
    static_configs:
      - targets: ['payment-engine:8080']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
      
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
      
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka-exporter:9308']
```

#### Grafana Dashboard Queries
```promql
# TPS Query
rate(payments_processed_total[5m])

# Response Time P95
histogram_quantile(0.95, rate(payment_response_time_seconds_bucket[5m]))

# Error Rate
rate(payment_errors_total[5m]) / rate(payments_processed_total[5m])

# Database Connections
database_connections_active

# Cache Hit Rate
redis_keyspace_hits / (redis_keyspace_hits + redis_keyspace_misses)
```

### Performance Testing Scripts

#### Load Test with k6
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },   // Ramp up to 100 TPS
    { duration: '5m', target: 100 },   // Stay at 100 TPS
    { duration: '2m', target: 500 },   // Ramp up to 500 TPS
    { duration: '5m', target: 500 },   // Stay at 500 TPS
    { duration: '2m', target: 1000 },  // Ramp up to 1000 TPS
    { duration: '5m', target: 1000 },  // Stay at 1000 TPS
    { duration: '2m', target: 2000 },  // Ramp up to 2000 TPS
    { duration: '10m', target: 2000 }, // Stay at 2000 TPS
    { duration: '2m', target: 0 },     // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests under 2s
    http_req_failed: ['rate<0.01'],    // Error rate under 1%
  },
};

export default function() {
  let payload = JSON.stringify({
    fromAccountId: 'test-account-1',
    toAccountId: 'test-account-2',
    paymentTypeId: 'test-payment-type',
    amount: 100.00,
    currencyCode: 'USD',
    description: 'Load test transaction'
  });

  let params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer test-token',
    },
  };

  let response = http.post('http://payment-engine/api/v1/transactions', payload, params);
  
  check(response, {
    'status is 201': (r) => r.status === 201,
    'response time < 2s': (r) => r.timings.duration < 2000,
  });
  
  sleep(1);
}
```

### Performance Monitoring Alerts

#### Alert Rules
```yaml
groups:
  - name: payment-engine
    rules:
      - alert: HighTPS
        expr: rate(payments_processed_total[5m]) > 1800
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High TPS detected"
          description: "TPS is {{ $value }} per second"
          
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(payment_response_time_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High response time"
          description: "95th percentile response time is {{ $value }} seconds"
          
      - alert: DatabaseConnectionsHigh
        expr: database_connections_active > 400
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "High database connections"
          description: "Active connections: {{ $value }}"
          
      - alert: HighErrorRate
        expr: rate(payment_errors_total[5m]) / rate(payments_processed_total[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate"
          description: "Error rate is {{ $value | humanizePercentage }}"
```

### Performance Optimization Checklist

#### Database Layer
- [ ] Connection pool size optimized (200+ connections)
- [ ] Query indexes created and optimized
- [ ] Batch operations implemented
- [ ] Read replicas configured
- [ ] Partitioning strategy implemented

#### Application Layer
- [ ] Async processing enabled
- [ ] Thread pools configured
- [ ] Caching strategy implemented
- [ ] Circuit breakers configured
- [ ] Batch processing enabled

#### Infrastructure Layer
- [ ] Load balancer configured
- [ ] Auto-scaling enabled
- [ ] Monitoring configured
- [ ] Alerting rules set up
- [ ] Performance testing completed

#### 3rd Party Components
- [ ] Redis cluster configured
- [ ] Kafka cluster optimized
- [ ] PostgreSQL tuned
- [ ] NGINX optimized
- [ ] Kubernetes resources allocated

### Expected Performance Gains

| Component | Before | After | Improvement |
|-----------|--------|-------|-------------|
| Database | 50 TPS | 2000 TPS | 40x |
| Application | 100 TPS | 2000 TPS | 20x |
| Infrastructure | 200 TPS | 2000 TPS | 10x |
| Overall | 50 TPS | 2000 TPS | 40x |

This configuration provides a solid foundation for achieving 2000 TPS with proper monitoring and alerting in place.