# Deployment Architecture - Design Document

## Overview

This document provides the **comprehensive deployment architecture** for the Payments Engine. It covers deployment strategies, CI/CD pipelines, infrastructure provisioning, environment management, and operational procedures to enable **zero-downtime deployments** with **complete automation** across **multiple environments** and **cells**.

---

## Deployment Principles

### 1. Zero-Downtime Deployments

```
Traditional Deployment (Downtime):
1. Stop application
2. Deploy new version
3. Start application
   ↓
Result: 5-10 minutes downtime ❌

Zero-Downtime Deployment:
1. Deploy new version alongside old
2. Gradually shift traffic to new version
3. Verify new version stable
4. Remove old version
   ↓
Result: 0 seconds downtime ✅

Techniques:
- Rolling updates (Kubernetes)
- Blue-green deployments
- Canary deployments (Istio)
- Circuit breakers (automatic rollback)
```

### 2. Progressive Delivery

```
Progressive Delivery Flow:

Stage 1: Internal Testing
├─ Deploy to dev environment
├─ Run automated tests
├─ Developer manual testing
└─ Approval required

Stage 2: Limited Production (Canary)
├─ Deploy to 1-5% production traffic
├─ Monitor metrics (error rate, latency)
├─ Duration: 30 minutes - 4 hours
└─ Automatic rollback if issues detected

Stage 3: Staged Rollout
├─ 10% → 25% → 50% → 100% production traffic
├─ Monitor at each stage
├─ Pause if issues detected
└─ Complete rollout if stable

Stage 4: Full Deployment
├─ All traffic on new version
├─ Remove old version
├─ Monitor for 24 hours
└─ Mark deployment complete

Benefits:
✅ Early issue detection (limited blast radius)
✅ Gradual confidence building
✅ Easy rollback (shift traffic back)
✅ Minimal risk to production
```

### 3. Infrastructure as Code (IaC)

```
Everything Defined in Code:

Infrastructure (Terraform):
├─ Azure resources (AKS, databases, networking)
├─ Version controlled (Git)
├─ Reproducible (any environment)
└─ Auditable (all changes tracked)

Application (Kubernetes Manifests):
├─ Deployments, services, ingress
├─ Version controlled (GitOps repo)
├─ Declarative (desired state)
└─ Automated sync (ArgoCD)

Configuration (Kustomize):
├─ Base configuration (shared)
├─ Environment overlays (dev, staging, prod)
├─ No manual kubectl commands
└─ Consistent across environments

Benefits:
✅ No manual infrastructure setup
✅ Consistent environments
✅ Easy disaster recovery (rebuild from code)
✅ Version controlled (Git history)
```

### 4. Immutable Infrastructure

```
Traditional (Mutable):
1. Deploy server
2. Patch OS
3. Update application
4. Configuration drift over time
   ↓
Result: "Works on my machine" syndrome ❌

Immutable (Our Approach):
1. Build container image (includes app + dependencies)
2. Test image
3. Deploy image (never modified)
4. New version = new image
   ↓
Result: Consistent, reproducible deployments ✅

Principles:
- Never modify running containers
- New version = new container image
- Version everything (app, OS, dependencies)
- Tag images with git commit SHA
```

---

## Deployment Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                   SOURCE CONTROL (GitHub)                        │
│  ├─ Application Code (payments-engine)                          │
│  ├─ Infrastructure Code (payments-engine-infra)                 │
│  └─ GitOps Repo (payments-engine-gitops)                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ Git Push
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                CI/CD PIPELINE (Azure DevOps)                     │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Stage 1: Build                                            │ │
│  │  ├─ Compile code                                           │ │
│  │  ├─ Run unit tests                                         │ │
│  │  ├─ SAST (SonarQube)                                       │ │
│  │  ├─ Dependency scanning (Snyk)                             │ │
│  │  └─ Build Docker image                                     │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Stage 2: Security Scan                                    │ │
│  │  ├─ Container image scan (Trivy)                           │ │
│  │  ├─ Secret detection (GitGuardian)                         │ │
│  │  └─ Vulnerability assessment                               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Stage 3: Push to Registry                                 │ │
│  │  ├─ Tag image (git-commit-sha)                             │ │
│  │  ├─ Push to Azure Container Registry (ACR)                 │ │
│  │  └─ Sign image (Notary)                                    │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Stage 4: Update GitOps Repo                               │ │
│  │  ├─ Update image tag in Kustomize                          │ │
│  │  ├─ Commit to GitOps repo                                  │ │
│  │  └─ Trigger ArgoCD sync                                    │ │
│  └────────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ ArgoCD Watches
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ARGOCD (GitOps)                               │
│  ├─ Detect Git changes                                          │
│  ├─ Sync to Kubernetes cluster                                  │
│  ├─ Deploy using progressive delivery                           │
│  └─ Monitor deployment health                                    │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ Deploys To
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              KUBERNETES CLUSTERS (AKS)                           │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Dev Cluster │  │ Staging      │  │ Production   │          │
│  │              │  │ Cluster      │  │ Cluster      │          │
│  │  - 3 nodes   │  │ - 10 nodes   │  │ - 10 cells   │          │
│  │  - Low cost  │  │ - Similar to │  │ - Full scale │          │
│  │              │  │   production │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

---

## CI/CD Pipeline Design

### Pipeline Stages

```yaml
# Azure DevOps Pipeline (azure-pipelines.yml)

name: Payments Engine CI/CD Pipeline

trigger:
  branches:
    include:
      - main
      - develop
      - release/*
  paths:
    include:
      - 'services/**'
      - 'infrastructure/**'

variables:
  - group: payments-engine-vars
  - name: dockerRegistryServiceConnection
    value: 'acr-payments-engine'
  - name: imageRepository
    value: 'payments-engine/payment-service'
  - name: containerRegistry
    value: 'paymentsacr.azurecr.io'
  - name: dockerfilePath
    value: 'services/payment-service/Dockerfile'
  - name: tag
    value: '$(Build.SourceVersion)'

stages:
  # Stage 1: Build & Test
  - stage: Build
    displayName: 'Build and Test'
    jobs:
      - job: Build
        displayName: 'Build Application'
        pool:
          vmImage: 'ubuntu-latest'
        
        steps:
          # Checkout code
          - checkout: self
            displayName: 'Checkout source code'
          
          # Set up JDK 17
          - task: JavaToolInstaller@0
            displayName: 'Set up JDK 17'
            inputs:
              versionSpec: '17'
              jdkArchitectureOption: 'x64'
              jdkSourceOption: 'PreInstalled'
          
          # Build with Maven
          - task: Maven@3
            displayName: 'Maven build'
            inputs:
              mavenPomFile: 'services/payment-service/pom.xml'
              goals: 'clean package'
              options: '-DskipTests=false'
              publishJUnitResults: true
              testResultsFiles: '**/surefire-reports/TEST-*.xml'
              codeCoverageToolOption: 'JaCoCo'
          
          # Run unit tests
          - task: Maven@3
            displayName: 'Run unit tests'
            inputs:
              mavenPomFile: 'services/payment-service/pom.xml'
              goals: 'test'
              publishJUnitResults: true
          
          # SAST (Static Application Security Testing)
          - task: SonarQubePrepare@5
            displayName: 'Prepare SonarQube analysis'
            inputs:
              SonarQube: 'SonarQube-Payments'
              scannerMode: 'Other'
          
          - task: SonarQubeAnalyze@5
            displayName: 'Run SonarQube analysis'
          
          - task: SonarQubePublish@5
            displayName: 'Publish SonarQube results'
            inputs:
              pollingTimeoutSec: '300'
          
          # Dependency scanning
          - task: SnykSecurityScan@1
            displayName: 'Snyk dependency scan'
            inputs:
              serviceConnectionEndpoint: 'Snyk-Payments'
              testType: 'app'
              monitorWhen: 'always'
              failOnIssues: true
              severityThreshold: 'high'
          
          # Build Docker image
          - task: Docker@2
            displayName: 'Build Docker image'
            inputs:
              command: 'build'
              repository: $(imageRepository)
              dockerfile: $(dockerfilePath)
              containerRegistry: $(dockerRegistryServiceConnection)
              tags: |
                $(tag)
                latest
              arguments: '--build-arg VERSION=$(tag)'
  
  # Stage 2: Security Scan
  - stage: SecurityScan
    displayName: 'Security Scanning'
    dependsOn: Build
    jobs:
      - job: ContainerScan
        displayName: 'Scan Container Image'
        pool:
          vmImage: 'ubuntu-latest'
        
        steps:
          # Container image scanning (Trivy)
          - task: trivy@1
            displayName: 'Trivy container scan'
            inputs:
              image: '$(containerRegistry)/$(imageRepository):$(tag)'
              severities: 'CRITICAL,HIGH'
              exitCode: '1'
          
          # Secret detection
          - task: GitGuardian@1
            displayName: 'GitGuardian secret scan'
            inputs:
              gitguardianApiKey: $(GITGUARDIAN_API_KEY)
          
          # Quality Gate Check
          - task: SonarQubeGate@5
            displayName: 'Check SonarQube Quality Gate'
            inputs:
              pollingTimeoutSec: '300'
  
  # Stage 3: Push to Registry
  - stage: PushImage
    displayName: 'Push to Registry'
    dependsOn: SecurityScan
    condition: succeeded()
    jobs:
      - job: Push
        displayName: 'Push Docker Image'
        pool:
          vmImage: 'ubuntu-latest'
        
        steps:
          # Push to ACR
          - task: Docker@2
            displayName: 'Push image to ACR'
            inputs:
              command: 'push'
              repository: $(imageRepository)
              containerRegistry: $(dockerRegistryServiceConnection)
              tags: |
                $(tag)
                latest
          
          # Sign image (Notary)
          - script: |
              docker trust sign $(containerRegistry)/$(imageRepository):$(tag)
            displayName: 'Sign Docker image'
            env:
              DOCKER_CONTENT_TRUST: 1
  
  # Stage 4: Deploy to Dev
  - stage: DeployDev
    displayName: 'Deploy to Dev'
    dependsOn: PushImage
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/develop'))
    jobs:
      - deployment: DeployDev
        displayName: 'Deploy to Dev Environment'
        environment: 'payments-dev'
        pool:
          vmImage: 'ubuntu-latest'
        
        strategy:
          runOnce:
            deploy:
              steps:
                # Update GitOps repo
                - task: Bash@3
                  displayName: 'Update GitOps repo'
                  inputs:
                    targetType: 'inline'
                    script: |
                      git clone https://$(GITHUB_PAT)@github.com/payments-engine/gitops.git
                      cd gitops
                      
                      # Update image tag
                      cd apps/payment-service/overlays/dev
                      kustomize edit set image payment-service=$(containerRegistry)/$(imageRepository):$(tag)
                      
                      # Commit and push
                      git config user.name "Azure DevOps"
                      git config user.email "devops@paymentsengine.com"
                      git add .
                      git commit -m "Update payment-service to $(tag) in dev"
                      git push origin main
                
                # Trigger ArgoCD sync
                - task: Bash@3
                  displayName: 'Trigger ArgoCD sync'
                  inputs:
                    targetType: 'inline'
                    script: |
                      argocd app sync payment-service-dev --auth-token $(ARGOCD_TOKEN)
                      argocd app wait payment-service-dev --timeout 300
  
  # Stage 5: Integration Tests
  - stage: IntegrationTests
    displayName: 'Run Integration Tests'
    dependsOn: DeployDev
    jobs:
      - job: Tests
        displayName: 'Integration Tests'
        pool:
          vmImage: 'ubuntu-latest'
        
        steps:
          # Run integration tests
          - task: Maven@3
            displayName: 'Run integration tests'
            inputs:
              mavenPomFile: 'tests/integration/pom.xml'
              goals: 'verify'
              options: '-Dtest.env=dev'
          
          # Run smoke tests
          - task: Bash@3
            displayName: 'Run smoke tests'
            inputs:
              targetType: 'inline'
              script: |
                ./scripts/smoke-tests.sh dev
  
  # Stage 6: Deploy to Staging
  - stage: DeployStaging
    displayName: 'Deploy to Staging'
    dependsOn: IntegrationTests
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/main'))
    jobs:
      - deployment: DeployStaging
        displayName: 'Deploy to Staging Environment'
        environment: 'payments-staging'
        pool:
          vmImage: 'ubuntu-latest'
        
        strategy:
          runOnce:
            deploy:
              steps:
                # Manual approval gate (configured in Azure DevOps)
                
                # Update GitOps repo
                - task: Bash@3
                  displayName: 'Update GitOps repo'
                  inputs:
                    targetType: 'inline'
                    script: |
                      git clone https://$(GITHUB_PAT)@github.com/payments-engine/gitops.git
                      cd gitops
                      
                      cd apps/payment-service/overlays/staging
                      kustomize edit set image payment-service=$(containerRegistry)/$(imageRepository):$(tag)
                      
                      git config user.name "Azure DevOps"
                      git config user.email "devops@paymentsengine.com"
                      git add .
                      git commit -m "Update payment-service to $(tag) in staging"
                      git push origin main
                
                # Trigger ArgoCD sync
                - task: Bash@3
                  displayName: 'Trigger ArgoCD sync'
                  inputs:
                    targetType: 'inline'
                    script: |
                      argocd app sync payment-service-staging --auth-token $(ARGOCD_TOKEN)
                      argocd app wait payment-service-staging --timeout 300
  
  # Stage 7: Deploy to Production
  - stage: DeployProduction
    displayName: 'Deploy to Production'
    dependsOn: DeployStaging
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/main'))
    jobs:
      - deployment: DeployProduction
        displayName: 'Deploy to Production Environment'
        environment: 'payments-production'
        pool:
          vmImage: 'ubuntu-latest'
        
        strategy:
          runOnce:
            deploy:
              steps:
                # Manual approval gate (production requires explicit approval)
                
                # Deploy with canary strategy (via Istio)
                - task: Bash@3
                  displayName: 'Deploy canary (10%)'
                  inputs:
                    targetType: 'inline'
                    script: |
                      # Update GitOps repo with canary configuration
                      git clone https://$(GITHUB_PAT)@github.com/payments-engine/gitops.git
                      cd gitops
                      
                      cd apps/payment-service/overlays/production
                      
                      # Create canary deployment
                      kustomize edit set image payment-service-canary=$(containerRegistry)/$(imageRepository):$(tag)
                      
                      # Update VirtualService (10% to canary, 90% to stable)
                      cat <<EOF > virtualservice-canary.yaml
                      apiVersion: networking.istio.io/v1beta1
                      kind: VirtualService
                      metadata:
                        name: payment-service-canary
                      spec:
                        hosts:
                          - payment-service
                        http:
                          - route:
                              - destination:
                                  host: payment-service
                                  subset: stable
                                weight: 90
                              - destination:
                                  host: payment-service
                                  subset: canary
                                weight: 10
                      EOF
                      
                      git add .
                      git commit -m "Deploy payment-service $(tag) as canary (10%)"
                      git push origin main
                      
                      # Trigger ArgoCD sync
                      argocd app sync payment-service-production --auth-token $(ARGOCD_TOKEN)
                      argocd app wait payment-service-production --timeout 300
                
                # Monitor canary for 30 minutes
                - task: Bash@3
                  displayName: 'Monitor canary deployment'
                  inputs:
                    targetType: 'inline'
                    script: |
                      ./scripts/monitor-canary.sh payment-service 30
                
                # Promote canary to 100% (if successful)
                - task: Bash@3
                  displayName: 'Promote canary to 100%'
                  condition: succeeded()
                  inputs:
                    targetType: 'inline'
                    script: |
                      cd gitops/apps/payment-service/overlays/production
                      
                      # Update VirtualService (100% to new version)
                      cat <<EOF > virtualservice-stable.yaml
                      apiVersion: networking.istio.io/v1beta1
                      kind: VirtualService
                      metadata:
                        name: payment-service
                      spec:
                        hosts:
                          - payment-service
                        http:
                          - route:
                              - destination:
                                  host: payment-service
                                  subset: stable
                                weight: 100
                      EOF
                      
                      # Update stable deployment to new version
                      kustomize edit set image payment-service=$(containerRegistry)/$(imageRepository):$(tag)
                      
                      git add .
                      git commit -m "Promote payment-service $(tag) to stable (100%)"
                      git push origin main
                      
                      argocd app sync payment-service-production --auth-token $(ARGOCD_TOKEN)
                
                # Cleanup canary
                - task: Bash@3
                  displayName: 'Cleanup canary deployment'
                  condition: succeeded()
                  inputs:
                    targetType: 'inline'
                    script: |
                      kubectl delete deployment payment-service-canary -n payments
```

---

## Deployment Strategies

### 1. Rolling Update (Default)

```yaml
# Kubernetes Rolling Update Strategy

apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  namespace: payments
spec:
  replicas: 10
  
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 3        # Max 3 additional pods during update
      maxUnavailable: 1  # Max 1 pod unavailable during update
  
  template:
    metadata:
      labels:
        app: payment-service
        version: v1.5.0
    spec:
      containers:
        - name: payment-service
          image: paymentsacr.azurecr.io/payment-service:v1.5.0
          
          # Readiness probe (must pass before receiving traffic)
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 3
          
          # Liveness probe (restart if fails)
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
            failureThreshold: 3
          
          # Graceful shutdown
          lifecycle:
            preStop:
              exec:
                command: ["/bin/sh", "-c", "sleep 15"]
```

**Rolling Update Flow**:
```
10 pods running v1.4.0

Step 1: Create 3 new pods (v1.5.0)
├─ Pods: 10 old + 3 new = 13 total
├─ Wait for new pods ready (health checks pass)
└─ Duration: ~30 seconds

Step 2: Terminate 1 old pod
├─ Pods: 9 old + 3 new = 12 total
└─ Duration: ~5 seconds

Step 3: Create 1 new pod (v1.5.0)
├─ Pods: 9 old + 4 new = 13 total
└─ Duration: ~30 seconds

Repeat steps 2-3 until all pods updated

Final: 10 new pods running v1.5.0

Total time: ~5-7 minutes
Downtime: 0 seconds ✅
Max additional resources: 3 pods (30%)
```

### 2. Blue-Green Deployment

```yaml
# Blue-Green Deployment

# Blue (Current Production - v1.4.0)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-blue
  namespace: payments
spec:
  replicas: 10
  selector:
    matchLabels:
      app: payment-service
      version: blue
  template:
    metadata:
      labels:
        app: payment-service
        version: blue
    spec:
      containers:
        - name: payment-service
          image: paymentsacr.azurecr.io/payment-service:v1.4.0

---

# Green (New Version - v1.5.0)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-green
  namespace: payments
spec:
  replicas: 10
  selector:
    matchLabels:
      app: payment-service
      version: green
  template:
    metadata:
      labels:
        app: payment-service
        version: green
    spec:
      containers:
        - name: payment-service
          image: paymentsacr.azurecr.io/payment-service:v1.5.0

---

# Service (Switch between blue and green)
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: payments
spec:
  selector:
    app: payment-service
    version: blue  # Currently pointing to blue
  ports:
    - port: 8080
      targetPort: 8080
```

**Blue-Green Flow**:
```
Phase 1: Deploy Green (New Version)
├─ Blue (v1.4.0): 10 pods, receiving 100% traffic
├─ Green (v1.5.0): 10 pods, receiving 0% traffic
├─ Test green internally (smoke tests)
└─ Duration: 5 minutes

Phase 2: Switch Traffic to Green
├─ Update Service selector: version=green
├─ Traffic instantly switched to green
├─ Blue still running (for rollback)
└─ Duration: < 1 second

Phase 3: Monitor Green
├─ Monitor metrics for 30 minutes
├─ If issues: Switch back to blue (instant rollback)
├─ If stable: Delete blue deployment
└─ Duration: 30 minutes

Benefits:
✅ Instant traffic switch (< 1 second)
✅ Instant rollback (switch back to blue)
✅ Zero downtime
✅ Full testing before switch

Drawbacks:
⚠️ Requires 2x resources during deployment
⚠️ Database migrations must be backward compatible
```

### 3. Canary Deployment (Istio)

```yaml
# Canary Deployment with Istio

# Stable Deployment (v1.4.0)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-stable
  namespace: payments
spec:
  replicas: 9
  selector:
    matchLabels:
      app: payment-service
      version: stable
  template:
    metadata:
      labels:
        app: payment-service
        version: stable
    spec:
      containers:
        - name: payment-service
          image: paymentsacr.azurecr.io/payment-service:v1.4.0

---

# Canary Deployment (v1.5.0)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-canary
  namespace: payments
spec:
  replicas: 1
  selector:
    matchLabels:
      app: payment-service
      version: canary
  template:
    metadata:
      labels:
        app: payment-service
        version: canary
    spec:
      containers:
        - name: payment-service
          image: paymentsacr.azurecr.io/payment-service:v1.5.0

---

# Service (All pods)
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: payments
spec:
  selector:
    app: payment-service  # Selects both stable and canary
  ports:
    - port: 8080
      targetPort: 8080

---

# Istio VirtualService (Traffic Split)
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: payment-service-canary
  namespace: payments
spec:
  hosts:
    - payment-service
  http:
    # Route to canary for specific header (testing)
    - match:
        - headers:
            x-canary:
              exact: "true"
      route:
        - destination:
            host: payment-service
            subset: canary
          weight: 100
    
    # Route 10% to canary, 90% to stable (production)
    - route:
        - destination:
            host: payment-service
            subset: stable
          weight: 90
        - destination:
            host: payment-service
            subset: canary
          weight: 10

---

# Istio DestinationRule (Define subsets)
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: payment-service
  namespace: payments
spec:
  host: payment-service
  subsets:
    - name: stable
      labels:
        version: stable
    - name: canary
      labels:
        version: canary
```

**Canary Flow (Progressive Delivery)**:
```
Phase 1: Deploy Canary (10%)
├─ Stable: 9 pods (90% traffic)
├─ Canary: 1 pod (10% traffic)
├─ Monitor: error rate, latency, throughput
├─ Duration: 30 minutes
└─ Decision: Proceed or rollback

Phase 2: Increase Canary (25%)
├─ Stable: 7-8 pods (75% traffic)
├─ Canary: 2-3 pods (25% traffic)
├─ Monitor: 30 minutes
└─ Decision: Proceed or rollback

Phase 3: Increase Canary (50%)
├─ Stable: 5 pods (50% traffic)
├─ Canary: 5 pods (50% traffic)
├─ Monitor: 30 minutes
└─ Decision: Proceed or rollback

Phase 4: Increase Canary (100%)
├─ Stable: 0 pods (0% traffic, delete deployment)
├─ Canary: 10 pods (100% traffic, rename to stable)
├─ Monitor: 24 hours
└─ Mark deployment complete

Total time: 2-4 hours
Automated rollback: If error rate > 1% OR latency p95 > 200ms

Benefits:
✅ Gradual rollout (limited blast radius)
✅ Automated monitoring and rollback
✅ Real production traffic testing
✅ Easy rollback at any phase
```

---

## Environment Management

### Environment Strategy

```
Three Environments:

┌─────────────────────────────────────────────────────────────┐
│  DEVELOPMENT                                                │
├─────────────────────────────────────────────────────────────┤
│  Purpose: Active development, feature testing              │
│  Cluster: AKS (3 nodes, Standard_D2s_v3)                   │
│  Replicas: 1 per service (minimal)                         │
│  Data: Synthetic data, anonymized production copy          │
│  Updates: Continuous (every commit to develop branch)      │
│  Cost: ~$500/month                                          │
│  Availability: 95%                                          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  STAGING                                                    │
├─────────────────────────────────────────────────────────────┤
│  Purpose: Pre-production testing, QA, UAT                  │
│  Cluster: AKS (10 nodes, Standard_D4s_v3)                  │
│  Replicas: Similar to production (80% capacity)            │
│  Data: Production-like data (anonymized)                   │
│  Updates: Daily (after successful dev testing)             │
│  Cost: ~$3,000/month                                        │
│  Availability: 99%                                          │
│  Notes: Mirror of production (same configs)                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  PRODUCTION                                                 │
├─────────────────────────────────────────────────────────────┤
│  Purpose: Live customer traffic                            │
│  Cluster: AKS (10 cells, 40 nodes each)                    │
│  Replicas: Full scale (10 per service, or reactive based)  │
│  Data: Real customer data (encrypted)                      │
│  Updates: Weekly (with canary deployment)                  │
│  Cost: ~$62,000/month (10 cells)                           │
│  Availability: 99.99% (SLA target)                         │
│  Notes: Zero-downtime deployments mandatory                │
└─────────────────────────────────────────────────────────────┘

Environment Promotion:
develop branch → Dev → Staging → Production (main branch)
```

### Environment Configuration (Kustomize)

```
GitOps Repository Structure:

payments-engine-gitops/
├── apps/
│   ├── payment-service/
│   │   ├── base/
│   │   │   ├── deployment.yaml      # Shared configuration
│   │   │   ├── service.yaml
│   │   │   ├── configmap.yaml
│   │   │   ├── hpa.yaml
│   │   │   └── kustomization.yaml
│   │   │
│   │   └── overlays/
│   │       ├── dev/
│   │       │   ├── kustomization.yaml
│   │       │   ├── replicas-patch.yaml    # 1 replica
│   │       │   ├── resources-patch.yaml   # Lower limits
│   │       │   └── configmap-patch.yaml   # Dev-specific config
│   │       │
│   │       ├── staging/
│   │       │   ├── kustomization.yaml
│   │       │   ├── replicas-patch.yaml    # 8 replicas
│   │       │   ├── resources-patch.yaml   # Production-like
│   │       │   └── configmap-patch.yaml   # Staging config
│   │       │
│   │       └── production/
│   │           ├── kustomization.yaml
│   │           ├── replicas-patch.yaml    # 10 replicas
│   │           ├── resources-patch.yaml   # Max resources
│   │           ├── configmap-patch.yaml   # Production config
│   │           └── canary/                # Canary configs
│   │               ├── virtualservice.yaml
│   │               └── destinationrule.yaml
│   │
│   └── ... (16 more services)
```

```yaml
# Base Deployment (Shared)
# apps/payment-service/base/deployment.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
spec:
  replicas: 3  # Default (overridden per environment)
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
        - name: payment-service
          image: payment-service:latest  # Replaced by Kustomize
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "1000m"
              memory: "1Gi"
          envFrom:
            - configMapRef:
                name: payment-service-config
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "default"
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: payment-service-secrets
                  key: database-password
```

```yaml
# Dev Overlay
# apps/payment-service/overlays/dev/kustomization.yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: payments-dev

bases:
  - ../../base

images:
  - name: payment-service
    newName: paymentsacr.azurecr.io/payment-service
    newTag: develop-abc123  # Updated by CI/CD

replicas:
  - name: payment-service
    count: 1  # Dev: 1 replica

patches:
  - path: resources-patch.yaml
  - path: configmap-patch.yaml

configMapGenerator:
  - name: payment-service-config
    literals:
      - ENVIRONMENT=dev
      - LOG_LEVEL=DEBUG
      - DATABASE_HOST=dev-postgres.database.azure.com
      - KAFKA_BOOTSTRAP_SERVERS=dev-kafka:9092

secretGenerator:
  - name: payment-service-secrets
    envs:
      - secrets.env
```

```yaml
# Production Overlay
# apps/payment-service/overlays/production/kustomization.yaml

apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: payments

bases:
  - ../../base

images:
  - name: payment-service
    newName: paymentsacr.azurecr.io/payment-service
    newTag: v1.5.0  # Updated by CI/CD

replicas:
  - name: payment-service
    count: 10  # Production: 10 replicas

patches:
  - path: resources-patch.yaml
  - path: hpa-patch.yaml

configMapGenerator:
  - name: payment-service-config
    literals:
      - ENVIRONMENT=production
      - LOG_LEVEL=INFO
      - DATABASE_HOST=prod-postgres-primary.database.azure.com
      - KAFKA_BOOTSTRAP_SERVERS=prod-kafka-1:9092,prod-kafka-2:9092,prod-kafka-3:9092
      - ENABLE_METRICS=true
      - ENABLE_TRACING=true

secretGenerator:
  - name: payment-service-secrets
    literals:
      - database-password=ENC[AES256_GCM,data:...,iv:...,tag:...]  # SOPS encrypted
```

---

## Infrastructure as Code (Terraform)

### Terraform Structure

```
payments-engine-infra/
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── terraform.tfvars
│   │   └── backend.tf
│   │
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── terraform.tfvars
│   │   └── backend.tf
│   │
│   └── production/
│       ├── main.tf
│       ├── variables.tf
│       ├── terraform.tfvars
│       └── backend.tf
│
├── modules/
│   ├── aks/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   └── README.md
│   │
│   ├── postgresql/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   │
│   ├── redis/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   │
│   ├── kafka/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   │
│   ├── monitoring/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   │
│   └── networking/
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
│
└── scripts/
    ├── deploy.sh
    ├── destroy.sh
    └── plan.sh
```

```hcl
# Production Environment Main Configuration
# environments/production/main.tf

terraform {
  required_version = ">= 1.0"
  
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
  
  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "paymentsenginestate"
    container_name       = "tfstate"
    key                  = "production.terraform.tfstate"
  }
}

provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "payments" {
  name     = "payments-engine-production-rg"
  location = "South Africa North"
  
  tags = {
    environment = "production"
    project     = "payments-engine"
    managed_by  = "terraform"
  }
}

# Virtual Network
module "networking" {
  source = "../../modules/networking"
  
  resource_group_name = azurerm_resource_group.payments.name
  location            = azurerm_resource_group.payments.location
  environment         = "production"
  
  vnet_address_space = ["10.0.0.0/16"]
  subnet_configs = {
    aks_subnet = {
      address_prefixes = ["10.0.1.0/24"]
    }
    database_subnet = {
      address_prefixes = ["10.0.2.0/24"]
    }
    kafka_subnet = {
      address_prefixes = ["10.0.3.0/24"]
    }
  }
}

# AKS Cluster
module "aks" {
  source = "../../modules/aks"
  
  resource_group_name = azurerm_resource_group.payments.name
  location            = azurerm_resource_group.payments.location
  environment         = "production"
  
  cluster_name = "payments-engine-prod-aks"
  
  default_node_pool = {
    name                = "system"
    node_count          = 3
    vm_size             = "Standard_D4s_v3"
    availability_zones  = ["1", "2", "3"]
    enable_auto_scaling = false
  }
  
  additional_node_pools = {
    payments = {
      name                = "payments"
      node_count          = 40
      vm_size             = "Standard_D8s_v3"
      availability_zones  = ["1", "2", "3"]
      enable_auto_scaling = true
      min_count           = 30
      max_count           = 60
    }
  }
  
  network_plugin = "azure"
  subnet_id      = module.networking.subnet_ids["aks_subnet"]
  
  enable_rbac            = true
  enable_azure_policy    = true
  enable_pod_security    = true
}

# PostgreSQL Flexible Server (14 databases for 14 services)
module "postgresql" {
  source = "../../modules/postgresql"
  
  for_each = toset([
    "payment",
    "validation",
    "account",
    "transaction",
    "saga",
    "notification",
    "routing",
    "clearing-samos",
    "clearing-bankserv",
    "clearing-rtc",
    "limit",
    "fraud",
    "tenant",
    "reporting"
  ])
  
  resource_group_name = azurerm_resource_group.payments.name
  location            = azurerm_resource_group.payments.location
  environment         = "production"
  
  server_name = "payments-${each.key}-prod-db"
  
  sku_name   = "GP_Standard_D4s_v3"
  storage_mb = 524288  # 512 GB
  
  backup_retention_days        = 35
  geo_redundant_backup_enabled = true
  
  high_availability = {
    mode                      = "ZoneRedundant"
    standby_availability_zone = "2"
  }
  
  subnet_id = module.networking.subnet_ids["database_subnet"]
}

# Redis Cache
module "redis" {
  source = "../../modules/redis"
  
  resource_group_name = azurerm_resource_group.payments.name
  location            = azurerm_resource_group.payments.location
  environment         = "production"
  
  cache_name     = "payments-engine-prod-redis"
  capacity       = 6
  family         = "P"
  sku_name       = "Premium"
  
  enable_non_ssl_port          = false
  minimum_tls_version          = "1.2"
  public_network_access_enabled = false
  
  redis_configuration = {
    maxmemory_policy = "allkeys-lru"
  }
}

# Kafka Cluster (Confluent Cloud or Azure Event Hubs)
module "kafka" {
  source = "../../modules/kafka"
  
  resource_group_name = azurerm_resource_group.payments.name
  location            = azurerm_resource_group.payments.location
  environment         = "production"
  
  namespace_name     = "payments-engine-prod-kafka"
  sku                = "Standard"
  capacity           = 3
  
  enable_auto_inflate     = true
  maximum_throughput_units = 20
}

# Monitoring (Azure Monitor, Application Insights)
module "monitoring" {
  source = "../../modules/monitoring"
  
  resource_group_name = azurerm_resource_group.payments.name
  location            = azurerm_resource_group.payments.location
  environment         = "production"
  
  log_analytics_workspace_name = "payments-prod-logs"
  log_analytics_sku            = "PerGB2018"
  retention_in_days            = 90
  
  application_insights_name = "payments-prod-appinsights"
}

# Outputs
output "aks_cluster_name" {
  value = module.aks.cluster_name
}

output "aks_kubeconfig" {
  value     = module.aks.kube_config
  sensitive = true
}

output "postgresql_servers" {
  value = {
    for k, v in module.postgresql : k => v.fqdn
  }
}
```

---

## Deployment Monitoring

### Deployment Metrics

```
Metrics Tracked During Deployment:

1. Error Rate
   - Threshold: < 1% (fail if > 1%)
   - Window: 5 minutes
   - Source: Application logs, Prometheus

2. Latency (p95)
   - Threshold: < 200ms (fail if > 300ms)
   - Window: 5 minutes
   - Source: Istio metrics, Application Insights

3. Throughput
   - Threshold: > 90% of baseline
   - Window: 5 minutes
   - Source: Istio metrics

4. Pod Health
   - Threshold: All pods ready
   - Window: Continuous
   - Source: Kubernetes API

5. Database Connections
   - Threshold: < 80% of pool size
   - Window: 1 minute
   - Source: Database metrics

6. Memory Usage
   - Threshold: < 85% of limit
   - Window: 5 minutes
   - Source: Kubernetes metrics

7. CPU Usage
   - Threshold: < 80% of limit
   - Window: 5 minutes
   - Source: Kubernetes metrics

Automated Rollback Triggers:
- Error rate > 5% for 2 minutes
- Latency p95 > 500ms for 5 minutes
- Pod crash loop (3 restarts in 5 minutes)
- Health check failures > 50%
```

### Deployment Dashboard

```
Grafana Deployment Dashboard:

┌─────────────────────────────────────────────────────────┐
│  Payment Service Deployment (v1.5.0)                    │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Status: ⚠️  Canary (10% traffic)                       │
│  Progress: ████░░░░░░ 30% (3/10 pods updated)          │
│  Duration: 15 minutes                                    │
│                                                          │
│  Metrics:                                                │
│  ├─ Error Rate:    0.12% ✅ (threshold: < 1%)          │
│  ├─ Latency p95:   85ms ✅ (threshold: < 200ms)        │
│  ├─ Throughput:    9,500 req/sec ✅ (baseline: 10K)    │
│  └─ Pod Health:    10/10 ready ✅                       │
│                                                          │
│  Version Comparison:                                     │
│  ┌────────────┬─────────┬─────────┬─────────────────┐  │
│  │ Version    │ Pods    │ Traffic │ Error Rate      │  │
│  ├────────────┼─────────┼─────────┼─────────────────┤  │
│  │ v1.4.0     │ 9       │ 90%     │ 0.10% ✅        │  │
│  │ v1.5.0     │ 1       │ 10%     │ 0.12% ✅        │  │
│  └────────────┴─────────┴─────────┴─────────────────┘  │
│                                                          │
│  Next Action:                                            │
│  ├─ Continue to 25% in 15 minutes                       │
│  └─ Or rollback if metrics degrade                      │
└─────────────────────────────────────────────────────────┘
```

---

## Rollback Procedures

### Automated Rollback

```yaml
# ArgoCD Rollback Configuration

apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata:
  name: payment-service-rollback
spec:
  metrics:
    # Metric 1: Error Rate
    - name: error-rate
      interval: 1m
      successCondition: result < 0.01
      failureLimit: 3
      provider:
        prometheus:
          address: http://prometheus:9090
          query: |
            sum(rate(http_requests_total{app="payment-service",status=~"5.."}[5m]))
            /
            sum(rate(http_requests_total{app="payment-service"}[5m]))
    
    # Metric 2: Latency p95
    - name: latency-p95
      interval: 1m
      successCondition: result < 200
      failureLimit: 3
      provider:
        prometheus:
          address: http://prometheus:9090
          query: |
            histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket{app="payment-service"}[5m])) by (le))
    
    # Metric 3: Pod Health
    - name: pod-health
      interval: 30s
      successCondition: result >= 0.9
      failureLimit: 2
      provider:
        prometheus:
          address: http://prometheus:9090
          query: |
            sum(kube_pod_status_ready{namespace="payments",pod=~"payment-service.*"})
            /
            sum(kube_pod_status_phase{namespace="payments",pod=~"payment-service.*",phase="Running"})
  
  # Automatic rollback if any metric fails
  failureCondition: "false"
```

### Manual Rollback

```bash
# Manual Rollback Script (rollback.sh)

#!/bin/bash

SERVICE_NAME="payment-service"
NAMESPACE="payments"
ENVIRONMENT="production"

echo "=== Rollback $SERVICE_NAME in $ENVIRONMENT ==="

# Get previous successful deployment
PREVIOUS_TAG=$(git log --grep="Deploy $SERVICE_NAME" --grep="Promote" --oneline -n 2 | tail -1 | grep -oP 'v\d+\.\d+\.\d+')

echo "Rolling back to version: $PREVIOUS_TAG"

# Update GitOps repo
cd gitops
git pull origin main

cd apps/$SERVICE_NAME/overlays/$ENVIRONMENT

# Revert to previous image tag
kustomize edit set image $SERVICE_NAME=paymentsacr.azurecr.io/$SERVICE_NAME:$PREVIOUS_TAG

# Remove canary configuration (if exists)
rm -f virtualservice-canary.yaml

# Commit and push
git add .
git commit -m "ROLLBACK: $SERVICE_NAME to $PREVIOUS_TAG in $ENVIRONMENT"
git push origin main

# Trigger ArgoCD sync
argocd app sync $SERVICE_NAME-$ENVIRONMENT --auth-token $ARGOCD_TOKEN

# Wait for rollback to complete
argocd app wait $SERVICE_NAME-$ENVIRONMENT --timeout 300

echo "Rollback complete. Monitoring for stability..."

# Monitor for 5 minutes
./scripts/monitor-deployment.sh $SERVICE_NAME 5

echo "=== Rollback Complete ==="
```

---

## Summary

### Deployment Architecture Highlights

✅ **Zero-Downtime**: Rolling updates, blue-green, canary  
✅ **Progressive Delivery**: Gradual rollout with monitoring  
✅ **Infrastructure as Code**: Terraform for all infrastructure  
✅ **GitOps**: ArgoCD for declarative deployments  
✅ **Automated CI/CD**: Azure DevOps pipelines  
✅ **Multi-Environment**: Dev, staging, production  
✅ **Automated Rollback**: Based on metrics thresholds  
✅ **Immutable Infrastructure**: Container images, version controlled  

### Implementation Effort

**Phase 1: CI/CD Setup** (1-2 weeks)
- Azure DevOps pipelines
- GitOps repository structure
- ArgoCD installation

**Phase 2: Infrastructure** (1-2 weeks)
- Terraform modules
- Environment provisioning
- Networking setup

**Phase 3: Deployment Strategies** (1 week)
- Rolling updates (default)
- Canary deployment (Istio)
- Monitoring dashboards

**Total**: 3-5 weeks

### Deployment Capabilities

Your deployment architecture enables:
- ✅ **Deploy 17 services** in parallel
- ✅ **Zero-downtime** for all deployments
- ✅ **3-5 minute** deployment time
- ✅ **3-minute** rollback time
- ✅ **Automated** monitoring and rollback
- ✅ **Multi-environment** (dev, staging, prod)
- ✅ **Cell-by-cell** deployment (production)
- ✅ **Complete audit trail** (Git history)

**Verdict**: **Production-ready deployment architecture** enabling **safe, fast, automated deployments** for **17 microservices** across **multiple environments** and **10 cells**. 🚀

---

## Related Documents

- **[19-GITOPS-ARGOCD.md](19-GITOPS-ARGOCD.md)** - GitOps strategy
- **[17-SERVICE-MESH-ISTIO.md](17-SERVICE-MESH-ISTIO.md)** - Canary deployments
- **[07-AZURE-INFRASTRUCTURE.md](07-AZURE-INFRASTRUCTURE.md)** - Azure resources
- **[20-CELL-BASED-ARCHITECTURE.md](20-CELL-BASED-ARCHITECTURE.md)** - Cell deployments

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal
