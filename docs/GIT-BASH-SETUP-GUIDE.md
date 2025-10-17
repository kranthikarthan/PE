# Git Bash Setup Guide for Payments Engine

## 🎯 Overview

This guide provides step-by-step instructions for setting up and running the Payments Engine with Istio Service Mesh using Git Bash on Windows.

## 📋 Prerequisites

- **Git for Windows** (includes Git Bash)
- **Docker Desktop** for Windows
- **kubectl** configured for AKS
- **Azure CLI** (for AKS management)
- **PowerShell** (for Azure CLI commands)

## 🚀 Quick Start with Git Bash

### 1. Open Git Bash
```bash
# Right-click in project folder → "Git Bash Here"
# OR
# Open Git Bash and navigate to project
cd /c/git/clone/PE
```

### 2. Verify Environment
```bash
# Check Git Bash version
git --version

# Check Docker
docker --version

# Check kubectl
kubectl version --client

# Check Azure CLI (run in PowerShell)
az --version
```

### 3. Install Istio Service Mesh
```bash
# Make scripts executable
chmod +x scripts/install-istio.sh
chmod +x scripts/deploy-with-istio.sh

# Run Istio installation
./scripts/install-istio.sh
```

### 4. Deploy Payments Engine
```bash
# Deploy all services with Istio
./scripts/deploy-with-istio.sh
```

## 🔧 Detailed Setup Instructions

### Step 1: Environment Preparation

#### Install Git for Windows
1. Download from: https://git-scm.com/download/win
2. Install with default settings
3. Ensure "Git Bash Here" is enabled in context menu

#### Install Docker Desktop
1. Download from: https://www.docker.com/products/docker-desktop
2. Enable WSL 2 backend
3. Start Docker Desktop

#### Install kubectl
```bash
# Download kubectl for Windows
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/windows/amd64/kubectl.exe"

# Move to PATH
mv kubectl.exe /c/Program\ Files/Git/usr/bin/

# Verify installation
kubectl version --client
```

#### Install Azure CLI
```powershell
# Run in PowerShell as Administrator
Invoke-WebRequest -Uri https://aka.ms/installazurecliwindows -OutFile .\AzureCLI.msi
Start-Process msiexec.exe -Wait -ArgumentList '/I AzureCLI.msi /quiet'
```

### Step 2: AKS Cluster Setup

#### Create AKS Cluster (if not exists)
```powershell
# Login to Azure
az login

# Create resource group
az group create --name payments-rg --location eastus

# Create AKS cluster
az aks create \
  --resource-group payments-rg \
  --name payments-aks \
  --node-count 3 \
  --node-vm-size Standard_D4s_v3 \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group payments-rg --name payments-aks
```

#### Verify AKS Access
```bash
# In Git Bash
kubectl get nodes
kubectl get namespaces
```

### Step 3: Istio Installation

#### Automated Installation
```bash
# Navigate to project directory
cd /c/git/clone/PE

# Run Istio installation script
./scripts/install-istio.sh
```

#### Manual Installation (if needed)
```bash
# Download Istio
curl -L https://istio.io/downloadIstio | sh -
cd istio-*
export PATH=$PWD/bin:$PATH

# Install Istio
istioctl install --set values.defaultRevision=default -y

# Create namespace
kubectl apply -f k8s/istio/namespace.yaml

# Apply configurations
kubectl apply -f k8s/istio/peer-authentication.yaml
kubectl apply -f k8s/istio/destination-rules.yaml
kubectl apply -f k8s/istio/virtual-services.yaml
kubectl apply -f k8s/istio/authorization-policies.yaml

# Install observability
kubectl apply -f samples/addons/kiali.yaml
kubectl apply -f samples/addons/prometheus.yaml
kubectl apply -f samples/addons/grafana.yaml
kubectl apply -f samples/addons/jaeger.yaml
```

### Step 4: Build and Deploy Services

#### Build Docker Images
```bash
# Build all microservices
docker build -f docker/payment-initiation-service/Dockerfile -t payments/payment-service:latest .
docker build -f docker/account-adapter-service/Dockerfile -t payments/account-service:latest .
docker build -f docker/validation-service/Dockerfile -t payments/validation-service:latest .
docker build -f docker/saga-orchestrator/Dockerfile -t payments/saga-orchestrator:latest .
```

#### Deploy Infrastructure
```bash
# Deploy PostgreSQL, Redis, Kafka
kubectl apply -f k8s/infrastructure/postgres.yaml
kubectl apply -f k8s/infrastructure/redis.yaml
kubectl apply -f k8s/infrastructure/kafka.yaml
```

#### Deploy Microservices
```bash
# Deploy all microservices with Istio
kubectl apply -f k8s/deployments/payment-service.yaml
kubectl apply -f k8s/deployments/account-service.yaml
kubectl apply -f k8s/deployments/validation-service.yaml
kubectl apply -f k8s/deployments/saga-orchestrator.yaml
```

## 🔍 Verification and Testing

### 1. Check Deployment Status
```bash
# Check all pods
kubectl get pods -n payments

# Check services
kubectl get services -n payments

# Check Istio sidecars
kubectl get pods -n payments -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.containers[*].name}{"\n"}{end}'
```

### 2. Verify mTLS
```bash
# Check mTLS status
istioctl authn tls-check

# Expected: All services should show "mTLS: STRICT"
```

### 3. Test Service Communication
```bash
# Test payment service
kubectl port-forward -n payments svc/payment-service 8081:8080 &
curl http://localhost:8081/actuator/health

# Test account service
kubectl port-forward -n payments svc/account-service 8083:8083 &
curl http://localhost:8083/actuator/health
```

### 4. Access Observability Tools
```bash
# Kiali (Service Mesh Dashboard)
kubectl port-forward -n istio-system svc/kiali 20001:20001 &
# Open: http://localhost:20001

# Grafana (Metrics)
kubectl port-forward -n istio-system svc/grafana 3000:3000 &
# Open: http://localhost:3000 (admin/admin)

# Prometheus (Metrics)
kubectl port-forward -n istio-system svc/prometheus 9090:9090 &
# Open: http://localhost:9090

# Jaeger (Tracing)
kubectl port-forward -n istio-system svc/tracing 16686:80 &
# Open: http://localhost:16686
```

## 🧪 Testing Scenarios

### 1. Circuit Breaker Testing
```bash
# Apply fault injection
kubectl apply -f k8s/istio/fault-injection.yaml

# Monitor in Kiali
# Check circuit breaker behavior
```

### 2. Canary Deployment Testing
```bash
# Deploy new version
kubectl set image deployment/payment-service payment-service=payments/payment-service:v2 -n payments

# Update traffic routing (90% v1, 10% v2)
kubectl apply -f k8s/istio/virtual-services.yaml

# Monitor metrics and rollback if needed
```

### 3. Multi-Tenant Routing
```bash
# Test tenant-specific routing
curl -H "x-tenant-id: STD-001" http://localhost:8081/api/v1/payments
curl -H "x-tenant-id: NED-001" http://localhost:8081/api/v1/payments
```

## 🔧 Troubleshooting

### Common Git Bash Issues

#### 1. Path Issues
```bash
# Add to ~/.bashrc
export PATH="/c/Program Files/Git/usr/bin:$PATH"
export PATH="/c/Program Files/Docker/Docker/resources/bin:$PATH"
```

#### 2. Docker Permission Issues
```bash
# Add user to docker group (if needed)
# Or run Docker Desktop as administrator
```

#### 3. kubectl Not Found
```bash
# Download kubectl to Git Bash bin directory
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/windows/amd64/kubectl.exe"
mv kubectl.exe /c/Program\ Files/Git/usr/bin/
```

#### 4. Azure CLI Not Available in Git Bash
```bash
# Use PowerShell for Azure CLI commands
# Or install Azure CLI for Git Bash
```

### Istio Issues

#### 1. Sidecar Not Injected
```bash
# Check namespace label
kubectl get namespace payments -o yaml

# Verify injection
kubectl label namespace payments istio-injection=enabled --overwrite
```

#### 2. mTLS Not Working
```bash
# Check PeerAuthentication
kubectl get peerauthentication -n payments

# Verify configuration
kubectl describe peerauthentication default -n payments
```

#### 3. Services Not Communicating
```bash
# Check DestinationRule
kubectl get destinationrule -n payments

# Check VirtualService
kubectl get virtualservice -n payments

# Check AuthorizationPolicy
kubectl get authorizationpolicy -n payments
```

## 📊 Monitoring and Maintenance

### Daily Operations
```bash
# Check pod status
kubectl get pods -n payments

# Check service health
kubectl get endpoints -n payments

# Monitor Istio metrics
kubectl top pods -n payments
```

### Weekly Maintenance
```bash
# Update Istio
istioctl upgrade

# Check certificate expiration
istioctl authn tls-check

# Review logs
kubectl logs -l app=payment-service -c istio-proxy -n payments
```

## 🚀 Next Steps

### Phase 2 Completion
1. **Deploy All Services**: Complete microservices deployment
2. **Verify mTLS**: Ensure all communication is encrypted
3. **Test Circuit Breakers**: Validate resilience patterns
4. **Monitor Metrics**: Set up dashboards and alerts
5. **Document Procedures**: Create operational runbooks

### Advanced Features
1. **Multi-Cluster**: Extend to multiple AKS clusters
2. **External Services**: Integrate with external APIs
3. **Custom Metrics**: Add business-specific metrics
4. **Security Policies**: Implement advanced authorization rules

## 📚 Additional Resources

- [Git for Windows Documentation](https://git-scm.com/docs)
- [Docker Desktop for Windows](https://docs.docker.com/desktop/windows/)
- [Istio Documentation](https://istio.io/latest/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

---

**Last Updated**: 2025-01-27  
**Version**: 1.0  
**Environment**: Git Bash on Windows  
**Status**: ✅ **READY FOR DEPLOYMENT**
