# Complete Payment Engine Deployment Guide

## Overview

This comprehensive guide covers deploying the **multi-tenant, highly configurable Payment Engine** to production environments. The deployment includes complete multi-tenancy support, runtime configuration management, ISO 20022 compliance, and enterprise monitoring.

## 🏗️ **Architecture Overview**

The Payment Engine is deployed as a **multi-tenant microservices architecture** on Azure Kubernetes Service (AKS) with:

- **Multi-tenant data isolation** using Row-Level Security (RLS)
- **Runtime configuration management** without service restarts
- **Dynamic payment type onboarding** via APIs
- **Feature flag management** with gradual rollouts
- **Comprehensive monitoring** with tenant-specific dashboards
- **Enterprise security** with OAuth2/JWT and Azure Key Vault

## 📋 **Prerequisites**

### **Required Tools**
- **Azure CLI** (v2.50+)
- **kubectl** (v1.28+)
- **Docker** (v24.0+)
- **Maven** (v3.9+)
- **Node.js** (v18+)
- **Helm** (v3.12+)

### **Required Azure Resources**
- **Azure Subscription** with Contributor access
- **Azure DevOps Organization** (for CI/CD)
- **Azure Container Registry** (ACR)
- **Azure Key Vault** (for secrets management)

### **Required Permissions**
- `Contributor` role on Azure subscription
- `AcrPush` role on Azure Container Registry
- `Key Vault Administrator` role on Azure Key Vault

## 🔧 **Environment Setup**

### **1. Clone Repository**
```bash
git clone https://github.com/your-org/payment-engine.git
cd payment-engine
```

### **2. Environment Configuration**
```bash
# Copy environment template
cp deployment/environments/production.env.template deployment/environments/production.env

# Edit production environment variables
nano deployment/environments/production.env
```

### **3. Azure Login and Context**
```bash
# Login to Azure
az login

# Set subscription
az account set --subscription "your-subscription-id"

# Create resource group
az group create --name "payment-engine-prod" --location "East US 2"
```

## 🗄️ **Database Deployment**

### **1. Deploy Azure Database for PostgreSQL**
```bash
# Deploy PostgreSQL with multi-tenant configuration
az postgres flexible-server create \
  --resource-group payment-engine-prod \
  --name payment-engine-db-prod \
  --location "East US 2" \
  --admin-user payment_admin \
  --admin-password "SecurePassword123!" \
  --sku-name Standard_D2s_v3 \
  --tier GeneralPurpose \
  --storage-size 512 \
  --version 15 \
  --high-availability Enabled \
  --zone 1 \
  --standby-zone 2
```

### **2. Configure Database Security**
```bash
# Allow Azure services
az postgres flexible-server firewall-rule create \
  --resource-group payment-engine-prod \
  --name payment-engine-db-prod \
  --rule-name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# Create database and schemas
psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d postgres \
     -c "CREATE DATABASE payment_engine;"

# Run initialization scripts
psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d payment_engine \
     -f database/init/01-init-schema.sql

psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d payment_engine \
     -f database/init/02-seed-data.sql

# Run multi-tenancy migrations
psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d payment_engine \
     -f database/migrations/002-add-iso20022-support.sql

psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d payment_engine \
     -f database/migrations/003-add-tenancy-and-configurability.sql
```

### **3. Enable Row-Level Security**
```sql
-- Connect to database and enable RLS
psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d payment_engine

-- Enable RLS on all tenant-aware tables
ALTER TABLE payment_engine.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_engine.accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_engine.transactions ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY tenant_isolation_customers ON payment_engine.customers
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));

CREATE POLICY tenant_isolation_accounts ON payment_engine.accounts
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));

CREATE POLICY tenant_isolation_transactions ON payment_engine.transactions
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));
```

## ☁️ **Infrastructure Deployment**

### **1. Deploy Core Azure Resources**
```bash
# Deploy using ARM template
az deployment group create \
  --resource-group payment-engine-prod \
  --template-file deployment/azure-arm/main.json \
  --parameters \
    environment=production \
    location="East US 2" \
    aksNodeCount=3 \
    aksNodeSize=Standard_D4s_v3 \
    postgresqlSkuName=GP_Gen5_4 \
    redisSkuName=Premium_P1
```

### **2. Configure Azure Key Vault**
```bash
# Create Key Vault
az keyvault create \
  --resource-group payment-engine-prod \
  --name payment-engine-kv-prod \
  --location "East US 2" \
  --enable-rbac-authorization true

# Store secrets
az keyvault secret set \
  --vault-name payment-engine-kv-prod \
  --name "database-password" \
  --value "SecurePassword123!"

az keyvault secret set \
  --vault-name payment-engine-kv-prod \
  --name "jwt-secret" \
  --value "your-jwt-secret-key-here"

az keyvault secret set \
  --vault-name payment-engine-kv-prod \
  --name "redis-password" \
  --value "your-redis-password"
```

### **3. Create AKS Cluster with Multi-Tenant Support**
```bash
# Create AKS cluster
az aks create \
  --resource-group payment-engine-prod \
  --name payment-engine-aks-prod \
  --location "East US 2" \
  --node-count 3 \
  --node-vm-size Standard_D4s_v3 \
  --enable-addons monitoring,azure-keyvault-secrets-provider \
  --enable-managed-identity \
  --enable-cluster-autoscaler \
  --min-count 3 \
  --max-count 10 \
  --zones 1 2 3

# Get AKS credentials
az aks get-credentials \
  --resource-group payment-engine-prod \
  --name payment-engine-aks-prod
```

### **4. Configure AKS for Multi-Tenancy**
```bash
# Create namespace
kubectl create namespace payment-engine

# Apply multi-tenant configuration
kubectl apply -f deployment/kubernetes/namespace.yaml
kubectl apply -f deployment/kubernetes/tenant-config.yaml
kubectl apply -f deployment/kubernetes/secrets.yaml
kubectl apply -f deployment/kubernetes/configmaps.yaml

# Set up RBAC for tenant operations
kubectl apply -f deployment/kubernetes/rbac.yaml
```

## 🐳 **Container Deployment**

### **1. Build and Push Images**
```bash
# Login to Azure Container Registry
az acr login --name paymentengineacr

# Build all services
./build-all.sh --production

# Tag and push images
docker tag payment-engine/api-gateway:latest \
  paymentengineacr.azurecr.io/api-gateway:v1.0.0

docker tag payment-engine/core-banking:latest \
  paymentengineacr.azurecr.io/core-banking:v1.0.0

docker tag payment-engine/middleware:latest \
  paymentengineacr.azurecr.io/middleware:v1.0.0

docker tag payment-engine/frontend:latest \
  paymentengineacr.azurecr.io/frontend:v1.0.0

# Push all images
docker push paymentengineacr.azurecr.io/api-gateway:v1.0.0
docker push paymentengineacr.azurecr.io/core-banking:v1.0.0
docker push paymentengineacr.azurecr.io/middleware:v1.0.0
docker push paymentengineacr.azurecr.io/frontend:v1.0.0
```

### **2. Deploy Services to AKS**
```bash
# Deploy in order (dependencies first)
kubectl apply -f deployment/kubernetes/secrets.yaml
kubectl apply -f deployment/kubernetes/configmaps.yaml
kubectl apply -f deployment/kubernetes/tenant-config.yaml

# Deploy stateful services
kubectl apply -f deployment/kubernetes/statefulsets.yaml

# Deploy applications
kubectl apply -f deployment/kubernetes/deployments.yaml

# Deploy services and ingress
kubectl apply -f deployment/kubernetes/services.yaml
kubectl apply -f deployment/kubernetes/ingress.yaml
```

### **3. Verify Multi-Tenant Deployment**
```bash
# Check all pods are running
kubectl get pods -n payment-engine

# Check services
kubectl get services -n payment-engine

# Check tenant configuration
kubectl get configmap tenant-config -n payment-engine -o yaml

# Test tenant context
kubectl exec -it deployment/core-banking-service -n payment-engine -- \
  curl -H "X-Tenant-ID: default" http://localhost:8080/api/v1/config/health
```

## 📊 **Monitoring Deployment**

### **1. Deploy Prometheus and Grafana**
```bash
# Add Prometheus Helm repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Deploy Prometheus with tenant-specific rules
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --values monitoring/prometheus/values.yaml \
  --set prometheus.prometheusSpec.additionalScrapeConfigs[0].job_name=payment-engine \
  --set prometheus.prometheusSpec.additionalScrapeConfigs[0].kubernetes_sd_configs[0].role=pod

# Deploy custom tenant rules
kubectl apply -f monitoring/prometheus/tenant-rules.yml

# Deploy Grafana dashboards
kubectl create configmap tenant-dashboard \
  --from-file=monitoring/grafana/dashboards/tenant-overview.json \
  --namespace monitoring

kubectl label configmap tenant-dashboard grafana_dashboard=1 --namespace monitoring
```

### **2. Deploy ELK Stack**
```bash
# Deploy Elasticsearch
kubectl apply -f monitoring/elasticsearch/elasticsearch.yaml

# Deploy Logstash
kubectl apply -f monitoring/logstash/logstash.yaml

# Deploy Kibana
kubectl apply -f monitoring/kibana/kibana.yaml

# Deploy Filebeat
kubectl apply -f monitoring/filebeat/filebeat-daemonset.yaml
```

### **3. Configure Azure Monitor Integration**
```bash
# Enable Azure Monitor for containers
az aks enable-addons \
  --resource-group payment-engine-prod \
  --name payment-engine-aks-prod \
  --addons monitoring

# Configure Log Analytics workspace
az monitor log-analytics workspace create \
  --resource-group payment-engine-prod \
  --workspace-name payment-engine-logs-prod \
  --location "East US 2"
```

## 🔄 **CI/CD Pipeline Setup**

### **1. Azure DevOps Configuration**
```yaml
# azure-pipelines.yml
trigger:
  branches:
    include:
    - main
    - develop

variables:
  - group: payment-engine-prod
  - name: containerRegistry
    value: 'paymentengineacr.azurecr.io'
  - name: kubernetesServiceConnection
    value: 'payment-engine-aks-prod'

stages:
- stage: Build
  jobs:
  - job: BuildServices
    pool:
      vmImage: 'ubuntu-latest'
    steps:
    - task: Maven@3
      inputs:
        mavenPomFile: 'services/shared/pom.xml'
        goals: 'clean install'
        
    - task: Maven@3
      inputs:
        mavenPomFile: 'services/core-banking/pom.xml'
        goals: 'clean package'
        
    - task: Docker@2
      inputs:
        containerRegistry: $(containerRegistry)
        repository: 'core-banking'
        command: 'buildAndPush'
        Dockerfile: 'services/core-banking/Dockerfile'
        tags: |
          $(Build.BuildNumber)
          latest

- stage: Deploy
  dependsOn: Build
  jobs:
  - deployment: DeployToProduction
    environment: 'payment-engine-prod'
    pool:
      vmImage: 'ubuntu-latest'
    strategy:
      runOnce:
        deploy:
          steps:
          - task: KubernetesManifest@0
            inputs:
              action: 'deploy'
              kubernetesServiceConnection: $(kubernetesServiceConnection)
              namespace: 'payment-engine'
              manifests: |
                deployment/kubernetes/deployments.yaml
                deployment/kubernetes/services.yaml
```

### **2. GitOps with ArgoCD (Optional)**
```bash
# Install ArgoCD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Configure ArgoCD application
kubectl apply -f - <<EOF
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: payment-engine
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/your-org/payment-engine.git
    targetRevision: main
    path: deployment/kubernetes
  destination:
    server: https://kubernetes.default.svc
    namespace: payment-engine
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
EOF
```

## 🔐 **Security Configuration**

### **1. Configure OAuth2/JWT**
```bash
# Create JWT signing key
openssl genrsa -out jwt-private.key 2048
openssl rsa -in jwt-private.key -pubout -out jwt-public.key

# Store in Key Vault
az keyvault secret set \
  --vault-name payment-engine-kv-prod \
  --name "jwt-private-key" \
  --file jwt-private.key

az keyvault secret set \
  --vault-name payment-engine-kv-prod \
  --name "jwt-public-key" \
  --file jwt-public.key
```

### **2. Configure Network Security**
```bash
# Create Network Security Group
az network nsg create \
  --resource-group payment-engine-prod \
  --name payment-engine-nsg

# Allow HTTPS only
az network nsg rule create \
  --resource-group payment-engine-prod \
  --nsg-name payment-engine-nsg \
  --name AllowHTTPS \
  --protocol tcp \
  --priority 100 \
  --destination-port-range 443 \
  --access allow

# Deny HTTP
az network nsg rule create \
  --resource-group payment-engine-prod \
  --nsg-name payment-engine-nsg \
  --name DenyHTTP \
  --protocol tcp \
  --priority 200 \
  --destination-port-range 80 \
  --access deny
```

### **3. Configure TLS/SSL**
```bash
# Create TLS certificate (using Let's Encrypt or Azure certificates)
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: payment-engine-tls
  namespace: payment-engine
spec:
  secretName: payment-engine-tls
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
  - api.payment-engine.com
  - app.payment-engine.com
EOF
```

## 🚀 **Multi-Tenant Configuration**

### **1. Create Default Tenants**
```bash
# Create default tenant via API
curl -X POST https://api.payment-engine.com/api/v1/config/tenants \
  -H "Authorization: Bearer admin-token" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "default",
    "tenantName": "Default Tenant",
    "tenantType": "BANK",
    "subscriptionTier": "ENTERPRISE",
    "configuration": {
      "features": {
        "iso20022": true,
        "bulkProcessing": true,
        "advancedMonitoring": true
      }
    }
  }'

# Create demo tenant
curl -X POST https://api.payment-engine.com/api/v1/config/tenants \
  -H "Authorization: Bearer admin-token" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "demo-bank",
    "tenantName": "Demo Bank",
    "tenantType": "BANK",
    "subscriptionTier": "STANDARD",
    "configuration": {
      "features": {
        "iso20022": true,
        "bulkProcessing": false,
        "advancedMonitoring": false
      }
    }
  }'
```

### **2. Configure Tenant-Specific Settings**
```bash
# Set tenant-specific configuration
curl -X POST https://api.payment-engine.com/api/v1/config/tenants/demo-bank/config \
  -H "Authorization: Bearer admin-token" \
  -H "X-Tenant-ID: demo-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "configKey": "payment.max_daily_limit",
    "configValue": "100000.00",
    "environment": "production"
  }'

# Add tenant-specific payment type
curl -X POST https://api.payment-engine.com/api/v1/config/tenants/demo-bank/payment-types \
  -H "Authorization: Bearer admin-token" \
  -H "X-Tenant-ID: demo-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "DEMO_TRANSFER",
    "name": "Demo Transfer",
    "description": "Demo bank specific transfer",
    "isSynchronous": true,
    "maxAmount": 50000.00,
    "processingFee": 1.00
  }'

# Configure tenant-specific rate limits
curl -X PUT https://api.payment-engine.com/api/v1/config/tenants/demo-bank/rate-limits \
  -H "Authorization: Bearer admin-token" \
  -H "X-Tenant-ID: demo-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "rateLimitPerMinute": 500,
    "burstCapacity": 750,
    "windowSizeSeconds": 60
  }' \
  --data-urlencode "endpoint=/api/v1/transactions"
```

## ✅ **Deployment Verification**

### **1. Health Checks**
```bash
# Check all services are healthy
kubectl get pods -n payment-engine

# Check service endpoints
curl -H "Authorization: Bearer token" \
     https://api.payment-engine.com/api/v1/health

# Check configuration service
curl -H "Authorization: Bearer token" \
     https://api.payment-engine.com/api/v1/config/health

# Check tenant-specific health
curl -H "Authorization: Bearer token" \
     -H "X-Tenant-ID: demo-bank" \
     https://api.payment-engine.com/api/v1/health
```

### **2. Multi-Tenant Verification**
```bash
# Test tenant isolation
curl -H "Authorization: Bearer token" \
     -H "X-Tenant-ID: demo-bank" \
     https://api.payment-engine.com/api/v1/transactions

# Should only return demo-bank transactions

curl -H "Authorization: Bearer token" \
     -H "X-Tenant-ID: default" \
     https://api.payment-engine.com/api/v1/transactions

# Should only return default tenant transactions
```

### **3. Configuration Management Verification**
```bash
# Test runtime configuration changes
curl -X POST https://api.payment-engine.com/api/v1/config/tenants/demo-bank/features/test-feature \
  -H "Authorization: Bearer admin-token" \
  -H "X-Tenant-ID: demo-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "config": {"rolloutPercentage": 50}
  }'

# Verify feature flag is active
curl -H "Authorization: Bearer token" \
     -H "X-Tenant-ID: demo-bank" \
     https://api.payment-engine.com/api/v1/config/tenants/demo-bank/features/test-feature
```

### **4. ISO 20022 Testing**
```bash
# Test pain.001 message processing
curl -X POST https://api.payment-engine.com/api/v1/iso20022/pain001 \
  -H "Authorization: Bearer token" \
  -H "X-Tenant-ID: demo-bank" \
  -H "Content-Type: application/json" \
  -d @tests/iso20022/sample-pain001-messages.json

# Test camt.055 cancellation
curl -X POST https://api.payment-engine.com/api/v1/iso20022/camt055 \
  -H "Authorization: Bearer token" \
  -H "X-Tenant-ID: demo-bank" \
  -H "Content-Type: application/json" \
  -d @tests/iso20022/sample-camt055-messages.json
```

### **5. Monitoring Verification**
```bash
# Check Prometheus metrics
curl http://prometheus.monitoring.svc.cluster.local:9090/api/v1/query?query=payment_transactions_total

# Check Grafana dashboards
kubectl port-forward -n monitoring svc/grafana 3000:80
# Open http://localhost:3000 and verify tenant dashboards

# Check logs in Kibana
kubectl port-forward -n monitoring svc/kibana 5601:5601
# Open http://localhost:5601 and verify tenant-tagged logs
```

## 🔧 **Troubleshooting**

### **Common Issues**

#### **1. Database Connection Issues**
```bash
# Check database connectivity
kubectl exec -it deployment/core-banking-service -n payment-engine -- \
  pg_isready -h payment-engine-db-prod.postgres.database.azure.com -U payment_admin

# Check database configuration
kubectl get secret database-secret -n payment-engine -o yaml
```

#### **2. Tenant Context Issues**
```bash
# Check tenant interceptor logs
kubectl logs deployment/core-banking-service -n payment-engine | grep -i tenant

# Verify tenant configuration
kubectl get configmap tenant-config -n payment-engine -o yaml

# Check RLS policies
psql -h payment-engine-db-prod.postgres.database.azure.com \
     -U payment_admin \
     -d payment_engine \
     -c "SELECT * FROM pg_policies WHERE tablename IN ('transactions', 'accounts', 'customers');"
```

#### **3. Configuration Service Issues**
```bash
# Check configuration service logs
kubectl logs deployment/core-banking-service -n payment-engine | grep -i configuration

# Verify Redis connectivity
kubectl exec -it deployment/redis -n payment-engine -- redis-cli ping

# Check configuration cache
kubectl exec -it deployment/redis -n payment-engine -- \
  redis-cli keys "config:*"
```

#### **4. Monitoring Issues**
```bash
# Check Prometheus targets
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Open http://localhost:9090/targets

# Check Grafana data sources
kubectl logs deployment/grafana -n monitoring

# Verify ELK stack
kubectl get pods -n monitoring | grep -E "(elasticsearch|logstash|kibana)"
```

## 📊 **Performance Tuning**

### **1. Database Optimization**
```sql
-- Optimize tenant-specific queries
CREATE INDEX CONCURRENTLY idx_transactions_tenant_created_at 
ON payment_engine.transactions(tenant_id, created_at);

CREATE INDEX CONCURRENTLY idx_accounts_tenant_status 
ON payment_engine.accounts(tenant_id, status);

-- Update table statistics
ANALYZE payment_engine.transactions;
ANALYZE payment_engine.accounts;
ANALYZE payment_engine.customers;
```

### **2. Kubernetes Resource Optimization**
```yaml
# Update deployment resources based on load testing
apiVersion: apps/v1
kind: Deployment
metadata:
  name: core-banking-service
spec:
  replicas: 5  # Scale based on tenant load
  template:
    spec:
      containers:
      - name: core-banking
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

### **3. Redis Optimization**
```bash
# Configure Redis for multi-tenant caching
kubectl exec -it deployment/redis -n payment-engine -- \
  redis-cli config set maxmemory 2gb

kubectl exec -it deployment/redis -n payment-engine -- \
  redis-cli config set maxmemory-policy allkeys-lru
```

## 🔄 **Backup and Recovery**

### **1. Database Backup**
```bash
# Configure automated backups
az postgres flexible-server parameter set \
  --resource-group payment-engine-prod \
  --server-name payment-engine-db-prod \
  --name backup_retention_days \
  --value 30

# Manual backup
pg_dump -h payment-engine-db-prod.postgres.database.azure.com \
        -U payment_admin \
        -d payment_engine \
        --no-password \
        --format=custom \
        --file=payment_engine_backup_$(date +%Y%m%d_%H%M%S).dump
```

### **2. Configuration Backup**
```bash
# Backup Kubernetes configurations
kubectl get all -n payment-engine -o yaml > payment-engine-k8s-backup.yaml

# Backup tenant configurations
curl -H "Authorization: Bearer admin-token" \
     https://api.payment-engine.com/api/v1/config/tenants > tenant-config-backup.json
```

## 🏆 **Production Checklist**

### **Pre-Deployment**
- [ ] All environment variables configured
- [ ] Database migrations tested
- [ ] SSL certificates installed
- [ ] Security scanning completed
- [ ] Load testing performed
- [ ] Disaster recovery plan tested

### **Post-Deployment**
- [ ] Health checks passing
- [ ] Monitoring dashboards configured
- [ ] Alerting rules active
- [ ] Backup procedures verified
- [ ] Security audit completed
- [ ] Performance benchmarks established

### **Multi-Tenant Specific**
- [ ] Tenant isolation verified
- [ ] Row-level security active
- [ ] Configuration service functional
- [ ] Feature flags operational
- [ ] Tenant-specific monitoring active
- [ ] Rate limiting per tenant working

---

This deployment guide covers the complete setup of a production-ready, multi-tenant Payment Engine with enterprise-grade security, monitoring, and configurability. The system is designed to support unlimited bank clients with complete data isolation and runtime configuration management.