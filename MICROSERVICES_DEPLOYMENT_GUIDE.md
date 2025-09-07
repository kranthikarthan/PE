# ISO 20022 Payment Engine - Microservices Deployment Guide

## 📋 **Overview**

This guide provides step-by-step instructions for deploying the ISO 20022 Payment Engine microservices architecture to a Kubernetes cluster with Istio service mesh.

## 🏗️ **Prerequisites**

### **Infrastructure Requirements**

- **Kubernetes Cluster** (v1.25+)
- **Istio Service Mesh** (v1.19+)
- **Helm** (v3.10+)
- **kubectl** (v1.25+)
- **Docker** (v20.10+)
- **PostgreSQL** (v15+)
- **Redis** (v7+)
- **Kafka** (v3.5+)

### **Resource Requirements**

**Minimum Cluster Resources**:
- **CPU**: 8 cores
- **Memory**: 32 GB
- **Storage**: 100 GB
- **Nodes**: 3 worker nodes

**Recommended Cluster Resources**:
- **CPU**: 16 cores
- **Memory**: 64 GB
- **Storage**: 500 GB
- **Nodes**: 5 worker nodes

## 🚀 **Deployment Steps**

### **Step 1: Cluster Setup**

1. **Create Kubernetes Cluster**
   ```bash
   # Using kind for local development
   kind create cluster --name payment-engine --config cluster-config.yaml
   
   # Using kops for AWS
   kops create cluster --name payment-engine.k8s.local --state s3://payment-engine-state
   
   # Using GKE for Google Cloud
   gcloud container clusters create payment-engine --zone us-central1-a --num-nodes 3
   ```

2. **Install Istio Service Mesh**
   ```bash
   # Download Istio
   curl -L https://istio.io/downloadIstio | sh -
   cd istio-1.19.0
   
   # Install Istio
   ./bin/istioctl install --set values.defaultRevision=default
   
   # Enable sidecar injection
   kubectl label namespace default istio-injection=enabled
   ```

3. **Install Helm**
   ```bash
   # Install Helm
   curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
   
   # Add required repositories
   helm repo add bitnami https://charts.bitnami.com/bitnami
   helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
   helm repo update
   ```

### **Step 2: Database Setup**

1. **Deploy PostgreSQL**
   ```bash
   # Create namespace
   kubectl create namespace payment-engine
   
   # Deploy PostgreSQL
   helm install postgresql bitnami/postgresql \
     --namespace payment-engine \
     --set auth.database=payment_engine \
     --set auth.username=payment_engine \
     --set auth.password=secure_password \
     --set primary.persistence.size=50Gi \
     --set primary.resources.requests.memory=1Gi \
     --set primary.resources.requests.cpu=500m
   ```

2. **Deploy Redis**
   ```bash
   # Deploy Redis
   helm install redis bitnami/redis \
     --namespace payment-engine \
     --set auth.enabled=false \
     --set master.persistence.size=10Gi \
     --set master.resources.requests.memory=512Mi \
     --set master.resources.requests.cpu=250m
   ```

3. **Deploy Kafka**
   ```bash
   # Deploy Kafka
   helm install kafka bitnami/kafka \
     --namespace payment-engine \
     --set persistence.size=20Gi \
     --set zookeeper.persistence.size=10Gi \
     --set resources.requests.memory=1Gi \
     --set resources.requests.cpu=500m
   ```

### **Step 3: Service Mesh Configuration**

1. **Deploy Istio Configuration**
   ```bash
   # Apply Istio configuration
   kubectl apply -f k8s/istio/namespace.yaml
   kubectl apply -f k8s/istio/gateway.yaml
   kubectl apply -f k8s/istio/destination-rules.yaml
   kubectl apply -f k8s/istio/security-policies.yaml
   ```

2. **Verify Istio Installation**
   ```bash
   # Check Istio status
   ./bin/istioctl verify-install
   
   # Check sidecar injection
   kubectl get pods -n payment-engine
   ```

### **Step 4: Build and Push Docker Images**

1. **Build Service Images**
   ```bash
   # Build auth-service
   docker build -t payment-engine/auth-service:latest services/auth-service/
   docker tag payment-engine/auth-service:latest your-registry/payment-engine/auth-service:latest
   docker push your-registry/payment-engine/auth-service:latest
   
   # Build config-service
   docker build -t payment-engine/config-service:latest services/config-service/
   docker tag payment-engine/config-service:latest your-registry/payment-engine/config-service:latest
   docker push your-registry/payment-engine/config-service:latest
   
   # Build payment-processing-service
   docker build -t payment-engine/payment-processing-service:latest services/payment-processing/
   docker tag payment-engine/payment-processing-service:latest your-registry/payment-engine/payment-processing-service:latest
   docker push your-registry/payment-engine/payment-processing-service:latest
   
   # Build core-banking-service
   docker build -t payment-engine/core-banking-service:latest services/core-banking/
   docker tag payment-engine/core-banking-service:latest your-registry/payment-engine/core-banking-service:latest
   docker push your-registry/payment-engine/core-banking-service:latest
   ```

### **Step 5: Deploy Microservices**

1. **Deploy Authentication Service**
   ```bash
   # Create secrets
   kubectl create secret generic auth-secrets \
     --namespace payment-engine \
     --from-literal=jwt-secret=your-jwt-secret \
     --from-literal=oauth-client-secret=your-oauth-secret
   
   # Deploy auth-service
   kubectl apply -f k8s/services/auth-service/
   ```

2. **Deploy Configuration Service**
   ```bash
   # Deploy config-service
   kubectl apply -f k8s/services/config-service/
   ```

3. **Deploy Payment Processing Service**
   ```bash
   # Deploy payment-processing-service
   kubectl apply -f k8s/services/payment-processing-service/
   ```

4. **Deploy Core Banking Service**
   ```bash
   # Deploy core-banking-service
   kubectl apply -f k8s/services/core-banking-service/
   ```

5. **Deploy API Gateway**
   ```bash
   # Deploy API Gateway
   kubectl apply -f k8s/services/api-gateway/
   ```

### **Step 6: Deploy Frontend**

1. **Build Frontend**
   ```bash
   # Build React frontend
   cd frontend
   npm install
   npm run build
   
   # Build Docker image
   docker build -t payment-engine/frontend:latest .
   docker tag payment-engine/frontend:latest your-registry/payment-engine/frontend:latest
   docker push your-registry/payment-engine/frontend:latest
   ```

2. **Deploy Frontend**
   ```bash
   # Deploy frontend
   kubectl apply -f k8s/services/frontend/
   ```

### **Step 7: Deploy Monitoring Stack**

1. **Deploy Prometheus**
   ```bash
   # Deploy Prometheus
   helm install prometheus prometheus-community/kube-prometheus-stack \
     --namespace monitoring \
     --create-namespace \
     --set grafana.adminPassword=admin \
     --set prometheus.prometheusSpec.retention=30d
   ```

2. **Deploy Jaeger**
   ```bash
   # Deploy Jaeger
   kubectl apply -f k8s/monitoring/jaeger/
   ```

3. **Deploy ELK Stack**
   ```bash
   # Deploy Elasticsearch
   helm install elasticsearch bitnami/elasticsearch \
     --namespace logging \
     --create-namespace \
     --set master.replicaCount=1 \
     --set data.replicaCount=1 \
     --set coordinating.replicaCount=1
   
   # Deploy Kibana
   helm install kibana bitnami/kibana \
     --namespace logging \
     --set elasticsearch.hosts[0]=elasticsearch-coordinating-only
   
   # Deploy Logstash
   helm install logstash bitnami/logstash \
     --namespace logging \
     --set elasticsearch.hosts[0]=elasticsearch-coordinating-only
   ```

### **Step 8: Configure Ingress**

1. **Deploy NGINX Ingress**
   ```bash
   # Deploy NGINX Ingress
   helm install ingress-nginx ingress-nginx/ingress-nginx \
     --namespace ingress-nginx \
     --create-namespace \
     --set controller.service.type=LoadBalancer
   ```

2. **Configure Ingress Rules**
   ```bash
   # Apply ingress configuration
   kubectl apply -f k8s/ingress/
   ```

### **Step 9: Database Migration**

1. **Run Database Migrations**
   ```bash
   # Get PostgreSQL connection details
   kubectl get secret postgresql -n payment-engine -o jsonpath="{.data.postgres-password}" | base64 --decode
   
   # Run migrations for each service
   kubectl run migration-auth --image=payment-engine/auth-service:latest \
     --namespace payment-engine \
     --command -- /bin/sh -c "java -jar app.jar --spring.profiles.active=migration"
   
   kubectl run migration-config --image=payment-engine/config-service:latest \
     --namespace payment-engine \
     --command -- /bin/sh -c "java -jar app.jar --spring.profiles.active=migration"
   
   kubectl run migration-payment-processing --image=payment-engine/payment-processing-service:latest \
     --namespace payment-engine \
     --command -- /bin/sh -c "java -jar app.jar --spring.profiles.active=migration"
   
   kubectl run migration-core-banking --image=payment-engine/core-banking-service:latest \
     --namespace payment-engine \
     --command -- /bin/sh -c "java -jar app.jar --spring.profiles.active=migration"
   ```

### **Step 10: Verification**

1. **Check Service Status**
   ```bash
   # Check all pods
   kubectl get pods -n payment-engine
   
   # Check services
   kubectl get services -n payment-engine
   
   # Check ingress
   kubectl get ingress -n payment-engine
   ```

2. **Test Service Connectivity**
   ```bash
   # Test auth-service
   kubectl port-forward svc/auth-service 8080:8080 -n payment-engine
   curl http://localhost:8080/api/v1/auth/health
   
   # Test config-service
   kubectl port-forward svc/config-service 8081:8080 -n payment-engine
   curl http://localhost:8081/api/v1/config/health
   
   # Test payment-processing-service
   kubectl port-forward svc/payment-processing-service 8082:8080 -n payment-engine
   curl http://localhost:8082/api/v1/iso20022/comprehensive/health
   
   # Test core-banking-service
   kubectl port-forward svc/core-banking-service 8083:8080 -n payment-engine
   curl http://localhost:8083/api/v1/banking/health
   ```

3. **Test Frontend**
   ```bash
   # Test frontend
   kubectl port-forward svc/frontend 3000:80 -n payment-engine
   # Open http://localhost:3000 in browser
   ```

## 🔧 **Configuration Management**

### **Environment Variables**

Create a `configmap.yaml` for environment-specific configuration:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-engine-config
  namespace: payment-engine
data:
  # Database configuration
  DB_HOST: "postgresql-service"
  DB_PORT: "5432"
  DB_NAME: "payment_engine"
  DB_USERNAME: "payment_engine"
  
  # Redis configuration
  REDIS_HOST: "redis-master"
  REDIS_PORT: "6379"
  
  # Kafka configuration
  KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
  
  # Service URLs
  AUTH_SERVICE_URL: "http://auth-service:8080"
  CONFIG_SERVICE_URL: "http://config-service:8080"
  PAYMENT_PROCESSING_SERVICE_URL: "http://payment-processing-service:8080"
  CORE_BANKING_SERVICE_URL: "http://core-banking-service:8080"
  
  # External service URLs
  CLEARING_SYSTEM_URL: "https://clearing-system.example.com"
  WEBHOOK_URL: "https://webhook.example.com"
```

### **Secrets Management**

Create a `secrets.yaml` for sensitive configuration:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: payment-engine-secrets
  namespace: payment-engine
type: Opaque
data:
  # Database password
  DB_PASSWORD: <base64-encoded-password>
  
  # JWT secret
  JWT_SECRET: <base64-encoded-jwt-secret>
  
  # OAuth client secret
  OAUTH_CLIENT_SECRET: <base64-encoded-oauth-secret>
  
  # Encryption key
  ENCRYPTION_KEY: <base64-encoded-encryption-key>
  
  # Signature keys
  SIGNATURE_PRIVATE_KEY: <base64-encoded-private-key>
  SIGNATURE_PUBLIC_KEY: <base64-encoded-public-key>
  
  # External service credentials
  CLEARING_SYSTEM_API_KEY: <base64-encoded-api-key>
  WEBHOOK_SECRET: <base64-encoded-webhook-secret>
```

## 📊 **Monitoring Setup**

### **Prometheus Configuration**

Create a `prometheus-config.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
    
    scrape_configs:
    - job_name: 'kubernetes-pods'
      kubernetes_sd_configs:
      - role: pod
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__
      - action: labelmap
        regex: __meta_kubernetes_pod_label_(.+)
      - source_labels: [__meta_kubernetes_namespace]
        action: replace
        target_label: kubernetes_namespace
      - source_labels: [__meta_kubernetes_pod_name]
        action: replace
        target_label: kubernetes_pod_name
```

### **Grafana Dashboards**

Create dashboard configurations for each service:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboards
  namespace: monitoring
data:
  auth-service-dashboard.json: |
    {
      "dashboard": {
        "title": "Auth Service Dashboard",
        "panels": [
          {
            "title": "Request Rate",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(http_requests_total{service=\"auth-service\"}[5m])",
                "legendFormat": "{{method}} {{endpoint}}"
              }
            ]
          },
          {
            "title": "Response Time",
            "type": "graph",
            "targets": [
              {
                "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{service=\"auth-service\"}[5m]))",
                "legendFormat": "95th percentile"
              }
            ]
          }
        ]
      }
    }
```

## 🔒 **Security Configuration**

### **Network Policies**

Create network policies to restrict traffic:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: payment-engine-network-policy
  namespace: payment-engine
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: istio-system
    - namespaceSelector:
        matchLabels:
          name: payment-engine
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: payment-engine
    - namespaceSelector:
        matchLabels:
          name: istio-system
```

### **Pod Security Policies**

Create pod security policies:

```yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: payment-engine-psp
  namespace: payment-engine
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
```

## 🚨 **Troubleshooting**

### **Common Issues**

1. **Pod Startup Issues**
   ```bash
   # Check pod logs
   kubectl logs -f deployment/auth-service -n payment-engine
   
   # Check pod events
   kubectl describe pod <pod-name> -n payment-engine
   
   # Check resource usage
   kubectl top pods -n payment-engine
   ```

2. **Service Discovery Issues**
   ```bash
   # Check service endpoints
   kubectl get endpoints -n payment-engine
   
   # Check DNS resolution
   kubectl run test-pod --image=busybox --rm -it -- nslookup auth-service
   
   # Check Istio sidecar
   kubectl get pods -n payment-engine -o wide
   ```

3. **Database Connection Issues**
   ```bash
   # Check database status
   kubectl get pods -n payment-engine | grep postgresql
   
   # Check database logs
   kubectl logs -f deployment/postgresql -n payment-engine
   
   # Test database connection
   kubectl run test-db --image=postgres:15 --rm -it -- psql -h postgresql-service -U payment_engine -d payment_engine
   ```

4. **Istio Service Mesh Issues**
   ```bash
   # Check Istio status
   ./bin/istioctl proxy-status
   
   # Check Istio configuration
   ./bin/istioctl analyze
   
   # Check sidecar injection
   kubectl get pods -n payment-engine -o jsonpath='{.items[*].metadata.annotations.sidecar\.istio\.io/status}'
   ```

### **Debugging Commands**

```bash
# Check all resources
kubectl get all -n payment-engine

# Check service mesh
./bin/istioctl proxy-status

# Check logs
kubectl logs -f deployment/auth-service -n payment-engine

# Check events
kubectl get events -n payment-engine --sort-by='.lastTimestamp'

# Check resource usage
kubectl top nodes
kubectl top pods -n payment-engine

# Check network policies
kubectl get networkpolicies -n payment-engine

# Check secrets
kubectl get secrets -n payment-engine

# Check configmaps
kubectl get configmaps -n payment-engine
```

## 📈 **Scaling**

### **Horizontal Pod Autoscaling**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: auth-service-hpa
  namespace: payment-engine
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: auth-service
  minReplicas: 2
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

### **Vertical Pod Autoscaling**

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: auth-service-vpa
  namespace: payment-engine
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: auth-service
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: auth-service
      minAllowed:
        cpu: 100m
        memory: 128Mi
      maxAllowed:
        cpu: 1
        memory: 1Gi
```

## 🔄 **Backup & Recovery**

### **Database Backup**

```bash
# Create backup job
kubectl create job postgresql-backup --from=cronjob/postgresql-backup -n payment-engine

# Manual backup
kubectl exec -it deployment/postgresql -n payment-engine -- pg_dump -U payment_engine payment_engine > backup.sql
```

### **Configuration Backup**

```bash
# Backup all configurations
kubectl get all -n payment-engine -o yaml > payment-engine-backup.yaml
kubectl get configmaps -n payment-engine -o yaml > configmaps-backup.yaml
kubectl get secrets -n payment-engine -o yaml > secrets-backup.yaml
```

## 📚 **Additional Resources**

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Istio Documentation](https://istio.io/latest/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

---

**Last Updated**: December 2024  
**Version**: 1.0.0  
**Maintainer**: Payment Engine Team