# Air-Gapped Deployment Guide

## Overview

This guide provides comprehensive instructions for building and deploying the Payment Engine in air-gapped environments using Azure DevOps. Air-gapped environments are isolated networks with no internet connectivity, requiring special considerations for package management, container registry mirroring, and deployment strategies.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Architecture Overview](#architecture-overview)
3. [Infrastructure Setup](#infrastructure-setup)
4. [Build Pipeline Configuration](#build-pipeline-configuration)
5. [Deployment Process](#deployment-process)
6. [Monitoring and Maintenance](#monitoring-and-maintenance)
7. [Troubleshooting](#troubleshooting)
8. [Security Considerations](#security-considerations)

## Prerequisites

### Infrastructure Requirements

- **Air-gapped Kubernetes cluster** (v1.24+)
- **Docker registry** (Harbor, Nexus, or similar)
- **Package repository** (Nexus, Artifactory, or similar)
- **Azure DevOps** with air-gapped agent
- **Storage** for offline packages and images
- **Network connectivity** between air-gapped environment and build environment

### Software Requirements

- **Docker** (v20.10+)
- **Kubernetes** (v1.24+)
- **Helm** (v3.8+)
- **kubectl** (v1.24+)
- **Maven** (v3.8+)
- **Node.js** (v18+)
- **Java** (v17+)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    AIR-GAPPED ENVIRONMENT                      │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Kubernetes    │  │  Container      │  │   Package       │ │
│  │    Cluster      │  │   Registry      │  │  Repository     │ │
│  │                 │  │                 │  │                 │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │ │
│  │  │ Payment   │  │  │  │   App     │  │  │  │   Maven   │  │ │
│  │  │ Engine    │  │  │  │  Images   │  │  │  │   Repo    │  │ │
│  │  │ Services  │  │  │  │           │  │  │  │           │  │ │
│  │  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │ │
│  │  │ Frontend  │  │  │  │  Base     │  │  │  │    NPM    │  │ │
│  │  │   App     │  │  │  │  Images   │  │  │  │   Repo    │  │ │
│  │  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ (Offline Transfer)
                                │
┌─────────────────────────────────────────────────────────────────┐
│                    BUILD ENVIRONMENT                           │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  Azure DevOps   │  │   Package       │  │   Container     │ │
│  │   Pipeline      │  │  Download       │  │   Build         │ │
│  │                 │  │                 │  │                 │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │ │
│  │  │   Build   │  │  │  │   Maven   │  │  │  │   Docker  │  │ │
│  │  │  Stages   │  │  │  │   Deps    │  │  │  │   Images  │  │ │
│  │  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │ │
│  │  │   Test    │  │  │  │    NPM    │  │  │  │   Base    │  │ │
│  │  │  Stages   │  │  │  │   Deps    │  │  │  │  Images   │  │ │
│  │  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Infrastructure Setup

### 1. Container Registry Setup

#### Using Harbor (Recommended)

```bash
# Install Harbor
helm repo add harbor https://helm.goharbor.io
helm install harbor harbor/harbor \
  --namespace harbor \
  --create-namespace \
  --set expose.type=nodePort \
  --set expose.tls.enabled=false \
  --set persistence.enabled=true \
  --set persistence.size=100Gi
```

#### Using Nexus Repository

```bash
# Run the Nexus setup script
chmod +x infrastructure/air-gapped/nexus-setup.sh
sudo ./infrastructure/air-gapped/nexus-setup.sh
```

### 2. Package Repository Setup

#### Nexus Repository Configuration

```bash
# Configure repositories
chmod +x /opt/nexus/configure-repositories.sh
sudo /opt/nexus/configure-repositories.sh

# Download packages
chmod +x /opt/nexus/download-packages.sh
sudo /opt/nexus/download-packages.sh
```

### 3. Kubernetes Cluster Setup

#### Air-gapped Kubernetes Installation

```bash
# Download Kubernetes binaries
wget https://dl.k8s.io/v1.28.0/kubernetes-server-linux-amd64.tar.gz
tar -xzf kubernetes-server-linux-amd64.tar.gz

# Install kubeadm, kubelet, kubectl
sudo cp kubernetes/server/bin/kubeadm /usr/local/bin/
sudo cp kubernetes/server/bin/kubelet /usr/local/bin/
sudo cp kubernetes/server/bin/kubectl /usr/local/bin/

# Initialize cluster
sudo kubeadm init --pod-network-cidr=10.244.0.0/16
```

## Build Pipeline Configuration

### 1. Azure DevOps Pipeline

The air-gapped build pipeline is configured in `azure-pipelines/air-gapped-build.yml`:

```yaml
# Key features:
# - Offline package management
# - Container registry mirroring
# - Multi-stage builds
# - Artifact generation
```

#### Pipeline Stages

1. **Prepare Offline Packages**
   - Download Maven dependencies
   - Download NPM packages
   - Download Docker base images

2. **Build Application**
   - Build backend services
   - Build frontend application
   - Generate artifacts

3. **Build Containers**
   - Build application images
   - Save images for offline transfer

4. **Create Deployment Package**
   - Package all components
   - Generate deployment scripts

### 2. Environment Variables

Configure the following variables in Azure DevOps:

```yaml
variables:
  AIRGAP_MODE: true
  OFFLINE_REGISTRY: 'airgap-registry.company.com'
  OFFLINE_NEXUS: 'airgap-nexus.company.com'
  OFFLINE_NPM_REGISTRY: 'http://airgap-nexus.company.com/repository/npm-proxy/'
  OFFLINE_MAVEN_REPO: 'http://airgap-nexus.company.com/repository/maven-public/'
```

### 3. Agent Configuration

#### Self-hosted Agent Setup

```bash
# Download agent
wget https://vstsagentpackage.azureedge.net/agent/3.220.5/vsts-agent-linux-x64-3.220.5.tar.gz
tar -xzf vsts-agent-linux-x64-3.220.5.tar.gz

# Configure agent
./config.sh --url https://dev.azure.com/yourorg --auth pat --token YOUR_TOKEN
./run.sh
```

## Deployment Process

### 1. Transfer Deployment Package

#### From Build Environment to Air-gapped Environment

```bash
# Create deployment package
tar -czf deployment-package.tar.gz deployment-package/

# Transfer via secure media
scp deployment-package.tar.gz airgap-server:/opt/deployment/

# Extract in air-gapped environment
cd /opt/deployment
tar -xzf deployment-package.tar.gz
```

### 2. Deploy Application

#### Using Deployment Script

```bash
# Make script executable
chmod +x infrastructure/air-gapped/offline-deploy.sh

# Run deployment
sudo ./infrastructure/air-gapped/offline-deploy.sh
```

#### Using Helm

```bash
# Install Helm chart
helm upgrade --install payment-engine helm/payment-engine \
  --values infrastructure/air-gapped/helm-values-airgap.yaml \
  --namespace payment-engine \
  --create-namespace
```

### 3. Verify Deployment

```bash
# Check pod status
kubectl get pods -n payment-engine

# Check services
kubectl get services -n payment-engine

# Check ingress
kubectl get ingress -n payment-engine

# Test application
curl http://payment-engine.local/api/health
```

## Monitoring and Maintenance

### 1. Health Checks

#### Application Health

```bash
# Check application health
kubectl get pods -n payment-engine
kubectl logs -f deployment/payment-processing -n payment-engine
kubectl logs -f deployment/payment-engine -n payment-engine
```

#### Infrastructure Health

```bash
# Check cluster health
kubectl get nodes
kubectl get pods -n kube-system

# Check registry health
curl -k https://airgap-registry.company.com:5000/v2/

# Check package repository health
curl http://airgap-nexus.company.com:8081/service/rest/v1/status
```

### 2. Backup and Recovery

#### Database Backup

```bash
# Create backup script
cat > backup-database.sh << 'EOF'
#!/bin/bash
kubectl exec -n payment-engine deployment/postgres -- pg_dump -U payment_user payment_engine > backup-$(date +%Y%m%d).sql
EOF

chmod +x backup-database.sh
```

#### Configuration Backup

```bash
# Backup Kubernetes resources
kubectl get all -n payment-engine -o yaml > k8s-resources-backup.yaml
kubectl get configmaps -n payment-engine -o yaml > configmaps-backup.yaml
kubectl get secrets -n payment-engine -o yaml > secrets-backup.yaml
```

### 3. Updates and Patches

#### Application Updates

```bash
# Build new version
# Transfer new deployment package
# Deploy new version
helm upgrade payment-engine helm/payment-engine \
  --values infrastructure/air-gapped/helm-values-airgap.yaml \
  --set image.tag=new-version
```

#### Infrastructure Updates

```bash
# Update Kubernetes
kubeadm upgrade plan
kubeadm upgrade apply v1.28.1

# Update container registry
docker-compose -f /opt/registry/docker-compose.yml pull
docker-compose -f /opt/registry/docker-compose.yml up -d
```

## Troubleshooting

### Common Issues

#### 1. Image Pull Errors

```bash
# Check registry connectivity
curl -k https://airgap-registry.company.com:5000/v2/

# Check image tags
docker images | grep payment-engine

# Check Kubernetes events
kubectl get events -n payment-engine
```

#### 2. Package Resolution Errors

```bash
# Check Maven repository
curl http://airgap-nexus.company.com:8081/repository/maven-public/

# Check NPM registry
curl http://airgap-nexus.company.com:8081/repository/npm-proxy/

# Check package availability
mvn dependency:resolve -Dmaven.repo.local=/opt/nexus/offline-packages/maven
```

#### 3. Network Connectivity Issues

```bash
# Check DNS resolution
nslookup airgap-registry.company.com
nslookup airgap-nexus.company.com

# Check network connectivity
ping airgap-registry.company.com
ping airgap-nexus.company.com

# Check firewall rules
iptables -L
ufw status
```

### Debugging Commands

```bash
# Check pod logs
kubectl logs -f deployment/payment-processing -n payment-engine
kubectl logs -f deployment/payment-engine -n payment-engine

# Check pod description
kubectl describe pod <pod-name> -n payment-engine

# Check service endpoints
kubectl get endpoints -n payment-engine

# Check ingress status
kubectl describe ingress payment-engine-ingress -n payment-engine
```

## Security Considerations

### 1. Network Security

- **Firewall Configuration**: Restrict access to registry and repository ports
- **TLS/SSL**: Use certificates for secure communication
- **Network Policies**: Implement Kubernetes network policies

### 2. Container Security

- **Image Scanning**: Scan images for vulnerabilities
- **Non-root Users**: Run containers as non-root users
- **Read-only Filesystems**: Use read-only root filesystems
- **Resource Limits**: Set CPU and memory limits

### 3. Access Control

- **RBAC**: Implement role-based access control
- **Service Accounts**: Use dedicated service accounts
- **Secrets Management**: Secure storage of sensitive data

### 4. Compliance

- **Audit Logging**: Enable audit logging
- **Compliance Scanning**: Regular compliance checks
- **Documentation**: Maintain security documentation

## Best Practices

### 1. Build Process

- **Reproducible Builds**: Use fixed versions and checksums
- **Dependency Management**: Pin all dependencies
- **Build Caching**: Implement build caching strategies
- **Artifact Signing**: Sign all artifacts

### 2. Deployment Process

- **Blue-Green Deployment**: Use blue-green deployment strategy
- **Rolling Updates**: Implement rolling updates
- **Health Checks**: Comprehensive health checks
- **Rollback Strategy**: Quick rollback capabilities

### 3. Monitoring

- **Comprehensive Monitoring**: Monitor all components
- **Alerting**: Set up alerting for critical issues
- **Logging**: Centralized logging
- **Metrics**: Application and infrastructure metrics

### 4. Maintenance

- **Regular Updates**: Keep components updated
- **Backup Strategy**: Regular backups
- **Disaster Recovery**: Test disaster recovery procedures
- **Documentation**: Keep documentation updated

## Conclusion

This guide provides a comprehensive approach to building and deploying the Payment Engine in air-gapped environments. The solution includes offline package management, container registry mirroring, and automated deployment processes that ensure reliable and secure operations in isolated environments.

For additional support or questions, please refer to the troubleshooting section or contact the development team.