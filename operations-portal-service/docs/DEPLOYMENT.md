# Deployment Guide

## Overview

This guide covers the complete deployment process for the React Operations Portal, including local development, Docker containerization, and Kubernetes production deployment.

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- Docker 20+
- Kubernetes 1.28+
- kubectl 1.28+

### Local Development

```bash
# Clone repository
git clone <repository-url>
cd payment-initiation-service/operations-portal

# Install dependencies
npm install

# Start development server
npm start
```

## 🐳 Docker Deployment

### Building Docker Image

```bash
# Build image
docker build -t operations-portal:latest .

# Build with specific tag
docker build -t operations-portal:v1.0.0 .

# Build for specific registry
docker build -t registry.example.com/operations-portal:latest .
```

### Running Container

```bash
# Run container
docker run -p 8080:8080 operations-portal:latest

# Run with environment variables
docker run -p 8080:8080 \
  -e REACT_APP_API_BASE_URL=http://localhost:8080 \
  -e REACT_APP_AUTH_SERVICE_URL=http://localhost:8081 \
  operations-portal:latest

# Run with volume mounts
docker run -p 8080:8080 \
  -v $(pwd)/nginx.conf:/etc/nginx/nginx.conf:ro \
  operations-portal:latest
```

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Start with build
docker-compose up --build

# View logs
docker-compose logs -f operations-portal

# Stop services
docker-compose down
```

## ☸️ Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.28+)
- kubectl configured
- Ingress controller installed
- Cert-manager (for TLS)

### Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace operations-portal

# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n operations-portal
kubectl get services -n operations-portal
kubectl get ingress -n operations-portal
```

### Using Deployment Scripts

**Linux/macOS:**
```bash
# Deploy with default settings
./scripts/deploy.sh

# Deploy with specific image tag
./scripts/deploy.sh v1.0.0

# Deploy to specific environment
./scripts/deploy.sh v1.0.0 production
```

**Windows:**
```powershell
# Deploy with default settings
.\scripts\deploy.ps1

# Deploy with specific image tag
.\scripts\deploy.ps1 -ImageTag v1.0.0

# Deploy to specific environment
.\scripts\deploy.ps1 -ImageTag v1.0.0 -Environment production
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `NODE_ENV` | Environment | `production` | Yes |
| `REACT_APP_API_BASE_URL` | API base URL | `http://localhost:8080` | Yes |
| `REACT_APP_AUTH_SERVICE_URL` | Auth service URL | `http://localhost:8081` | Yes |
| `REACT_APP_OPERATIONS_SERVICE_URL` | Operations service URL | `http://localhost:8082` | Yes |
| `REACT_APP_PAYMENT_SERVICE_URL` | Payment service URL | `http://localhost:8083` | Yes |
| `REACT_APP_TRANSACTION_SERVICE_URL` | Transaction service URL | `http://localhost:8084` | Yes |
| `REACT_APP_RECONCILIATION_SERVICE_URL` | Reconciliation service URL | `http://localhost:8085` | Yes |
| `REACT_APP_ENABLE_DEBUG` | Debug mode | `false` | No |
| `REACT_APP_ENABLE_MOCK_DATA` | Mock data | `false` | No |
| `REACT_APP_ENABLE_ANALYTICS` | Analytics | `true` | No |

### ConfigMap Configuration

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: operations-portal-config
  namespace: operations-portal
data:
  NODE_ENV: "production"
  REACT_APP_API_BASE_URL: "http://api-gateway:8080"
  REACT_APP_AUTH_SERVICE_URL: "http://auth-service:8080"
  # ... other environment variables
```

### Secret Configuration

```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: operations-portal-secret
  namespace: operations-portal
type: Opaque
data:
  JWT_SECRET: <base64-encoded-secret>
  API_KEY: <base64-encoded-key>
  ENCRYPTION_KEY: <base64-encoded-key>
```

## 🌐 Ingress Configuration

### Basic Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: operations-portal-ingress
  namespace: operations-portal
spec:
  rules:
  - host: operations-portal.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: operations-portal-service
            port:
              number: 80
```

### TLS Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: operations-portal-ingress
  namespace: operations-portal
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - operations-portal.example.com
    secretName: operations-portal-tls
  rules:
  - host: operations-portal.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: operations-portal-service
            port:
              number: 80
```

## 📊 Monitoring and Health Checks

### Health Check Endpoint

```bash
# Check application health
curl http://localhost:8080/health

# Check Kubernetes health
kubectl get pods -n operations-portal
kubectl describe pod <pod-name> -n operations-portal
```

### Monitoring Configuration

```yaml
# k8s/deployment.yaml
spec:
  template:
    spec:
      containers:
      - name: operations-portal
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
```

## 🔒 Security Configuration

### Network Policies

```yaml
# k8s/networkpolicy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: operations-portal-network-policy
  namespace: operations-portal
spec:
  podSelector:
    matchLabels:
      app: operations-portal
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
```

### Security Context

```yaml
# k8s/deployment.yaml
spec:
  template:
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        runAsGroup: 1001
        fsGroup: 1001
      containers:
      - name: operations-portal
        securityContext:
          runAsNonRoot: true
          runAsUser: 1001
          runAsGroup: 1001
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
```

## 📈 Scaling Configuration

### Horizontal Pod Autoscaler

```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: operations-portal-hpa
  namespace: operations-portal
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: operations-portal
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

### Pod Disruption Budget

```yaml
# k8s/pdb.yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: operations-portal-pdb
  namespace: operations-portal
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: operations-portal
```

## 🔄 CI/CD Pipeline

### GitHub Actions

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '18'
          cache: 'npm'
      - run: npm ci
      - run: npm run test:ci
      - run: npm run test:e2e

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Docker image
        run: docker build -t operations-portal:latest .
      - name: Push to registry
        run: docker push registry.example.com/operations-portal:latest

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s/
```

## 🛠️ Troubleshooting

### Common Issues

#### 1. **Pod Not Starting**
```bash
# Check pod status
kubectl get pods -n operations-portal

# Check pod logs
kubectl logs <pod-name> -n operations-portal

# Check pod events
kubectl describe pod <pod-name> -n operations-portal
```

#### 2. **Service Not Accessible**
```bash
# Check service status
kubectl get services -n operations-portal

# Check service endpoints
kubectl get endpoints -n operations-portal

# Test service connectivity
kubectl run test-pod --image=busybox -it --rm -- wget -qO- http://operations-portal-service:80
```

#### 3. **Ingress Not Working**
```bash
# Check ingress status
kubectl get ingress -n operations-portal

# Check ingress controller
kubectl get pods -n ingress-nginx

# Test ingress connectivity
curl -H "Host: operations-portal.example.com" http://<ingress-ip>/
```

#### 4. **Health Check Failures**
```bash
# Check health endpoint
kubectl port-forward service/operations-portal-service 8080:80 -n operations-portal
curl http://localhost:8080/health

# Check container logs
kubectl logs <pod-name> -n operations-portal --tail=100
```

### Debug Commands

```bash
# Get all resources
kubectl get all -n operations-portal

# Check resource usage
kubectl top pods -n operations-portal
kubectl top nodes

# Check events
kubectl get events -n operations-portal --sort-by='.lastTimestamp'

# Check configuration
kubectl get configmap operations-portal-config -n operations-portal -o yaml
kubectl get secret operations-portal-secret -n operations-portal -o yaml
```

## 📋 Deployment Checklist

### Pre-deployment
- [ ] Code reviewed and approved
- [ ] Tests passing (unit, integration, E2E)
- [ ] Security scan completed
- [ ] Performance tests passed
- [ ] Documentation updated

### Deployment
- [ ] Environment variables configured
- [ ] Secrets created and secured
- [ ] Kubernetes manifests applied
- [ ] Health checks passing
- [ ] Monitoring configured
- [ ] Backup strategy in place

### Post-deployment
- [ ] Smoke tests passed
- [ ] Performance metrics within limits
- [ ] Security scan completed
- [ ] User acceptance testing
- [ ] Rollback plan tested
- [ ] Documentation updated

## 🔄 Rollback Procedures

### Rollback to Previous Version

```bash
# Rollback deployment
kubectl rollout undo deployment/operations-portal -n operations-portal

# Check rollback status
kubectl rollout status deployment/operations-portal -n operations-portal

# Rollback to specific revision
kubectl rollout undo deployment/operations-portal --to-revision=2 -n operations-portal
```

### Emergency Rollback

```bash
# Scale down current deployment
kubectl scale deployment operations-portal --replicas=0 -n operations-portal

# Deploy previous version
kubectl apply -f k8s/previous-version/

# Scale up previous version
kubectl scale deployment operations-portal --replicas=3 -n operations-portal
```

## 📊 Performance Optimization

### Resource Limits

```yaml
# k8s/deployment.yaml
spec:
  template:
    spec:
      containers:
      - name: operations-portal
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
```

### Caching Configuration

```yaml
# nginx.conf
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

This comprehensive deployment guide ensures the React Operations Portal is deployed securely, efficiently, and with proper monitoring in place.
