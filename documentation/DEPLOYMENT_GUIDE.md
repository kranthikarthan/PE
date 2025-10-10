# Payment Engine Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Payment Engine to Azure Kubernetes Service (AKS) using Azure DevOps pipelines. The deployment includes all necessary infrastructure, monitoring, and security configurations.

## Prerequisites

### Required Tools

- Azure CLI (version 2.0 or later)
- kubectl (version 1.20 or later)
- Docker (version 20.0 or later)
- Azure DevOps account with appropriate permissions
- GitHub/Azure Repos access

### Required Azure Resources

- Azure Subscription with appropriate permissions
- Azure DevOps Organization
- Container Registry (ACR)
- Key Vault for secrets management
- Log Analytics Workspace

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Azure Cloud                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   Frontend      │    │   Load Balancer │                │
│  │   (React SPA)   │    │   (Azure LB)    │                │
│  └─────────────────┘    └─────────────────┘                │
│           │                       │                         │
│  ┌─────────────────────────────────────────────────────────┤
│  │                 AKS Cluster                             │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  │API Gateway  │  │Core Banking │  │ Payment Processing  │    │
│  │  │             │  │   Service   │  │   Service   │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │
│  │           │               │               │            │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  │ PostgreSQL  │  │   Kafka     │  │    Redis    │    │
│  │  │             │  │  Cluster    │  │             │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │
│  │                                                        │
│  │  ┌─────────────┐  ┌─────────────┐                     │
│  │  │ Prometheus  │  │   Grafana   │                     │
│  │  │             │  │             │                     │
│  │  └─────────────┘  └─────────────┘                     │
│  └─────────────────────────────────────────────────────────┘
└─────────────────────────────────────────────────────────────┘
```

## Infrastructure Setup

### Step 1: Azure Resource Group and Basic Resources

```bash
# Login to Azure
az login

# Set subscription
az account set --subscription "your-subscription-id"

# Create resource group
az group create --name payment-engine-rg --location eastus

# Create container registry
az acr create --resource-group payment-engine-rg \
  --name paymentengineacr \
  --sku Premium \
  --admin-enabled true

# Create Key Vault
az keyvault create --name payment-engine-kv \
  --resource-group payment-engine-rg \
  --location eastus \
  --enabled-for-deployment true \
  --enabled-for-template-deployment true
```

### Step 2: AKS Cluster Creation

```bash
# Create AKS cluster
az aks create \
  --resource-group payment-engine-rg \
  --name payment-engine-aks \
  --node-count 3 \
  --node-vm-size Standard_D4s_v3 \
  --kubernetes-version 1.28.0 \
  --enable-managed-identity \
  --enable-addons monitoring \
  --generate-ssh-keys \
  --attach-acr paymentengineacr \
  --enable-cluster-autoscaler \
  --min-count 3 \
  --max-count 10

# Get AKS credentials
az aks get-credentials --resource-group payment-engine-rg --name payment-engine-aks

# Verify cluster connection
kubectl cluster-info
kubectl get nodes
```

### Step 3: Configure Azure Key Vault

```bash
# Add secrets to Key Vault
az keyvault secret set --vault-name payment-engine-kv \
  --name "DatabasePassword" \
  --value "your-secure-database-password"

az keyvault secret set --vault-name payment-engine-kv \
  --name "JWTSecret" \
  --value "your-jwt-secret-key"

az keyvault secret set --vault-name payment-engine-kv \
  --name "EncryptionKey" \
  --value "your-encryption-key"

# Grant AKS access to Key Vault
AKS_IDENTITY=$(az aks show --resource-group payment-engine-rg \
  --name payment-engine-aks \
  --query "identity.principalId" --output tsv)

az keyvault set-policy --name payment-engine-kv \
  --object-id $AKS_IDENTITY \
  --secret-permissions get list
```

## Azure DevOps Setup

### Step 1: Create Azure DevOps Project

1. Navigate to [Azure DevOps](https://dev.azure.com)
2. Create a new project named "Payment Engine"
3. Import the repository containing the Payment Engine code

### Step 2: Service Connections

Create the following service connections in Azure DevOps:

#### Azure Resource Manager Connection
- **Connection Name**: `PaymentEngineSubscription`
- **Scope**: Subscription
- **Authentication**: Service Principal (automatic)

#### Container Registry Connection
- **Connection Name**: `PaymentEngineACR`
- **Registry URL**: `paymentengineacr.azurecr.io`
- **Authentication**: Service Principal

#### Kubernetes Connection
- **Connection Name**: `PaymentEngineAKS`
- **Cluster**: payment-engine-aks
- **Authentication**: Azure Subscription

### Step 3: Variable Groups

Create variable groups for different environments:

#### Production Variables (`payment-engine-prod`)
```yaml
variables:
  - subscriptionId: "your-subscription-id"
  - resourceGroupName: "payment-engine-rg"
  - aksClusterName: "payment-engine-aks"
  - acrName: "paymentengineacr"
  - keyVaultName: "payment-engine-kv"
  - databasePassword: "$(DatabasePassword)" # From Key Vault
  - jwtSecret: "$(JWTSecret)" # From Key Vault
  - encryptionKey: "$(EncryptionKey)" # From Key Vault
```

#### Staging Variables (`payment-engine-staging`)
```yaml
variables:
  - subscriptionId: "your-subscription-id"
  - resourceGroupName: "payment-engine-staging-rg"
  - aksClusterName: "payment-engine-staging-aks"
  - acrName: "paymentengineacr"
  - keyVaultName: "payment-engine-staging-kv"
```

### Step 4: Pipeline Setup

1. Create a new pipeline using the existing `azure-pipelines.yml` file
2. Configure branch policies for main branch
3. Enable continuous integration triggers
4. Set up approval gates for production deployments

## Manual Deployment Steps

If you prefer to deploy manually without Azure DevOps, follow these steps:

### Step 1: Build and Push Docker Images

```bash
# Build Core Banking Service
cd services/core-banking
docker build -t paymentengineacr.azurecr.io/core-banking:$BUILD_VERSION .

# Build Frontend
cd ../../frontend
docker build -t paymentengineacr.azurecr.io/frontend:$BUILD_VERSION .

# Login to ACR
az acr login --name paymentengineacr

# Push images
docker push paymentengineacr.azurecr.io/core-banking:$BUILD_VERSION
docker push paymentengineacr.azurecr.io/frontend:$BUILD_VERSION
```

### Step 2: Deploy Kubernetes Resources

```bash
# Apply namespace and RBAC
kubectl apply -f deployment/kubernetes/namespace.yaml

# Apply ConfigMaps and configure External Secrets for Azure Key Vault
kubectl apply -f deployment/kubernetes/configmaps.yaml
kubectl apply -f deployment/kubernetes/external-secrets.yaml

# Deploy the platform via Helm with immutable images
helm upgrade --install payment-engine deployment/helm/payment-engine \
  --namespace payment-engine \
  --create-namespace \
  -f deployment/helm/payment-engine/values.yaml \
  -f deployment/helm/payment-engine/values-production.yaml \
  --set global.imageRegistry=paymentengineacr.azurecr.io \
  --set components.apiGateway.image.tag=$BUILD_VERSION \
  --set components.apiGateway.image.digest=$API_GATEWAY_DIGEST \
  --set components.paymentProcessing.image.tag=$BUILD_VERSION \
  --set components.paymentProcessing.image.digest=$PAYMENT_PROCESSING_DIGEST \
  --set components.authService.image.tag=$BUILD_VERSION \
  --set components.authService.image.digest=$AUTH_SERVICE_DIGEST \
  --wait --timeout 10m
```

### Step 3: Verify Deployment

```bash
# Check pod status
kubectl get pods -n payment-engine

# Check services
kubectl get services -n payment-engine

# Check ingress (if configured)
kubectl get ingress -n payment-engine

# View logs
kubectl logs -f deployment/core-banking -n payment-engine
```

## Configuration Management

### Environment-Specific Configurations

#### Development Environment
```yaml
# Override values for development
replicas: 1
resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "200m"
```

#### Staging Environment
```yaml
# Override values for staging
replicas: 2
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

#### Production Environment
```yaml
# Override values for production
replicas: 3
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1"
```

### Database Initialization

The PostgreSQL database is automatically initialized with the schema and seed data:

```bash
# Check database initialization
kubectl exec -it postgresql-0 -n payment-engine -- psql -U payment_user -d payment_engine -c "\dt"

# Run additional migrations if needed
kubectl exec -it postgresql-0 -n payment-engine -- psql -U payment_user -d payment_engine -f /docker-entrypoint-initdb.d/migration.sql
```

## Monitoring and Observability

### Prometheus Setup

Prometheus is automatically deployed and configured to scrape metrics from all services:

```bash
# Access Prometheus UI
kubectl port-forward service/prometheus-service 9090:9090 -n payment-engine

# Open http://localhost:9090 in browser
```

### Grafana Setup

```bash
# Access Grafana UI
kubectl port-forward service/grafana-service 3000:3000 -n payment-engine

# Open http://localhost:3000 in browser
# Default credentials: admin/admin (change on first login)
```

### Log Aggregation

Logs are collected using Fluentd and stored in Azure Log Analytics:

```bash
# View application logs
kubectl logs -f deployment/core-banking -n payment-engine

# Query logs in Azure Portal
# Navigate to Log Analytics Workspace > Logs
# Query: ContainerLog | where ContainerName == "core-banking"
```

## Security Configuration

### Network Security

```bash
# Apply network policies
kubectl apply -f deployment/kubernetes/network-policies.yaml

# Configure pod security policies
kubectl apply -f deployment/kubernetes/pod-security-policies.yaml
```

### RBAC Configuration

```bash
# Create service account
kubectl create serviceaccount payment-engine-sa -n payment-engine

# Apply RBAC rules
kubectl apply -f deployment/kubernetes/rbac.yaml
```

### TLS/SSL Configuration

```bash
# Create TLS certificate (using cert-manager)
kubectl apply -f deployment/kubernetes/certificates.yaml

# Verify certificate
kubectl get certificate -n payment-engine
```

## Scaling and Performance

### Horizontal Pod Autoscaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: core-banking-hpa
  namespace: payment-engine
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: core-banking
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

### Cluster Autoscaling

```bash
# Enable cluster autoscaler (already configured during cluster creation)
az aks update \
  --resource-group payment-engine-rg \
  --name payment-engine-aks \
  --enable-cluster-autoscaler \
  --min-count 3 \
  --max-count 20
```

## Backup and Disaster Recovery

### Database Backup

```bash
# Create automated backup job
kubectl apply -f deployment/kubernetes/backup-job.yaml

# Manual backup
kubectl exec -it postgresql-0 -n payment-engine -- pg_dump -U payment_user payment_engine > backup.sql
```

### Persistent Volume Backup

```bash
# Create volume snapshot
kubectl apply -f deployment/kubernetes/volume-snapshot.yaml

# List snapshots
kubectl get volumesnapshot -n payment-engine
```

## Troubleshooting

### Common Issues

#### Pod Startup Issues
```bash
# Check pod events
kubectl describe pod <pod-name> -n payment-engine

# Check logs
kubectl logs <pod-name> -n payment-engine

# Check resource constraints
kubectl top pods -n payment-engine
```

#### Database Connection Issues
```bash
# Test database connectivity
kubectl exec -it core-banking-<pod-id> -n payment-engine -- nc -zv postgresql-service 5432

# Check database logs
kubectl logs postgresql-0 -n payment-engine
```

#### Kafka Issues
```bash
# Check Kafka cluster status
kubectl exec -it kafka-0 -n payment-engine -- kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics
kubectl exec -it kafka-0 -n payment-engine -- kafka-topics --bootstrap-server localhost:9092 --list
```

### Health Checks

```bash
# Check application health endpoints
kubectl exec -it core-banking-<pod-id> -n payment-engine -- curl http://localhost:8081/actuator/health

# Check service endpoints
kubectl get endpoints -n payment-engine
```

### Performance Monitoring

```bash
# Check resource usage
kubectl top nodes
kubectl top pods -n payment-engine

# Check HPA status
kubectl get hpa -n payment-engine

# Check cluster autoscaler logs
kubectl logs -f deployment/cluster-autoscaler -n kube-system
```

## Maintenance

### Rolling Updates

```bash
# Update deployment image
kubectl set image deployment/core-banking core-banking=paymentengineacr.azurecr.io/core-banking:v1.1.0 -n payment-engine

# Check rollout status
kubectl rollout status deployment/core-banking -n payment-engine

# Rollback if needed
kubectl rollout undo deployment/core-banking -n payment-engine
```

### Certificate Renewal

```bash
# Check certificate expiration
kubectl get certificate -n payment-engine

# Force certificate renewal
kubectl delete certificate payment-engine-tls -n payment-engine
kubectl apply -f deployment/kubernetes/certificates.yaml
```

### Database Maintenance

```bash
# Run database maintenance
kubectl exec -it postgresql-0 -n payment-engine -- psql -U payment_user -d payment_engine -c "VACUUM ANALYZE;"

# Update statistics
kubectl exec -it postgresql-0 -n payment-engine -- psql -U payment_user -d payment_engine -c "ANALYZE;"
```

## Support and Documentation

- **Kubernetes Documentation**: [https://kubernetes.io/docs/](https://kubernetes.io/docs/)
- **Azure AKS Documentation**: [https://docs.microsoft.com/en-us/azure/aks/](https://docs.microsoft.com/en-us/azure/aks/)
- **Azure DevOps Documentation**: [https://docs.microsoft.com/en-us/azure/devops/](https://docs.microsoft.com/en-us/azure/devops/)
- **Internal Wiki**: [https://wiki.company.com/payment-engine](https://wiki.company.com/payment-engine)
- **Support Team**: [devops-support@company.com](mailto:devops-support@company.com)