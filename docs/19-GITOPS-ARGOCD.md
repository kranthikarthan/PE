# GitOps with ArgoCD - Architecture Design

## Overview

This document provides the **GitOps architecture design** using ArgoCD for the Payments Engine. GitOps treats Git as the single source of truth for declarative infrastructure and applications, enabling automated, auditable, and reversible deployments.

---

## Why GitOps?

### Current Deployment Challenge

```
Current Process (Manual kubectl):

Developer builds image
  ↓
Push to Azure Container Registry
  ↓
SSH to AKS cluster
  ↓
kubectl apply -f deployment.yaml
  ↓
Manually verify deployment
  ↓
Update ConfigMaps manually
  ↓
Repeat for all 17 services

Problems:
❌ Manual process (human error)
❌ No audit trail (who deployed what?)
❌ No easy rollback
❌ Configuration drift (cluster vs Git)
❌ No synchronization (Git ≠ cluster)
```

### With GitOps

```
GitOps Process (Automated):

Developer commits code
  ↓
CI Pipeline builds image
  ↓
Update Git (deployment manifest)
  ↓
ArgoCD detects change (within 3 minutes)
  ↓
ArgoCD applies changes to cluster
  ↓
ArgoCD monitors and self-heals
  ↓
Git is always in sync with cluster ✅

Benefits:
✅ Automated deployment (no kubectl)
✅ Complete audit trail (Git history)
✅ Easy rollback (git revert)
✅ No configuration drift (sync loop)
✅ Self-healing (if manual change, revert)
```

---

## GitOps Principles

### 1. Declarative

```
Everything is declared in Git:

payments-engine-gitops/
├── apps/
│   ├── payment-service/
│   │   ├── deployment.yaml      # Declare desired state
│   │   ├── service.yaml
│   │   ├── configmap.yaml
│   │   └── hpa.yaml
│   │
│   ├── validation-service/
│   └── ... (15 more services)
│
├── infrastructure/
│   ├── istio/
│   ├── monitoring/
│   └── ingress/
│
└── environments/
    ├── dev/
    ├── staging/
    └── production/

Not: "Run these commands to deploy"
But: "This is what should be deployed"
```

### 2. Git as Single Source of Truth

```
Git repository = Desired state of cluster

If Git says:   payment-service: v1.2.3, 5 replicas
Then cluster:  payment-service: v1.2.3, 5 replicas

If someone manually changes cluster (kubectl scale replicas=10):
ArgoCD detects drift → reverts to Git (5 replicas)

Result: Git always wins ✅
```

### 3. Automated Synchronization

```
ArgoCD Sync Loop (every 3 minutes):

┌─────────────────────────────────────────┐
│  1. Fetch Git repository                │
│     (payments-engine-gitops)            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  2. Compare Git vs Kubernetes cluster   │
│     (desired state vs actual state)     │
└──────────────┬──────────────────────────┘
               │
               ▼
         ┌─────┴─────┐
         │ In Sync?  │
         └─────┬─────┘
               │
       ┌───────┴───────┐
       │               │
       ▼               ▼
    ┌─────┐        ┌─────────────┐
    │ Yes │        │ No (Drift)  │
    │     │        │             │
    │Skip │        │  Apply      │
    │     │        │  Changes    │
    └─────┘        └─────────────┘
                          │
                          ▼
                   ┌─────────────┐
                   │ Cluster     │
                   │ Updated     │
                   └─────────────┘
```

### 4. Observable & Auditable

```
Git Commit History = Deployment History

git log --oneline apps/payment-service/

abc123 - Deploy payment-service v1.3.0 (John, 2 hours ago)
def456 - Increase replicas to 10 (Jane, 4 hours ago)
ghi789 - Update DB connection string (Bob, yesterday)

Every change:
✅ Who made it
✅ When it was made
✅ What was changed (diff)
✅ Why (commit message)

Compare to kubectl: ❌ No history, no audit trail
```

---

## GitOps Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                        Developer                                 │
│                                                                  │
│  git commit → git push                                          │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Git Repository (GitOps Repo)                     │
│                                                                  │
│  github.com/payments-engine/gitops                              │
│                                                                  │
│  apps/                                                          │
│  ├── payment-service/      (Kubernetes manifests)              │
│  ├── validation-service/                                        │
│  └── ... (15 more)                                              │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                │ Watches (polls every 3 min)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ArgoCD (on AKS)                             │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ArgoCD Server                                           │  │
│  │  - Web UI                                                │  │
│  │  - API                                                   │  │
│  │  - Application Controller (sync loop)                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                │ Applies manifests
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Kubernetes Cluster (AKS)                        │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  Payment     │  │ Validation   │  │  Account     │         │
│  │  Service     │  │ Service      │  │  Adapter     │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                  │
│  ... (14 more services)                                         │
└─────────────────────────────────────────────────────────────────┘
```

### ArgoCD Components

```
┌────────────────────────────────────────────────────────┐
│                  ArgoCD Architecture                    │
├────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  ArgoCD Server                                  │   │
│  │  - Web UI (port 8080)                          │   │
│  │  - REST API                                     │   │
│  │  - gRPC API                                     │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Application Controller                         │   │
│  │  - Monitors Git repositories                    │   │
│  │  - Compares desired vs actual state            │   │
│  │  - Syncs applications                           │   │
│  │  - Health assessment                            │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Repo Server                                    │   │
│  │  - Clones Git repositories                      │   │
│  │  - Renders manifests (Helm, Kustomize)         │   │
│  │  - Caches rendered manifests                    │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Redis                                          │   │
│  │  - Caching                                      │   │
│  │  - Stores application state                     │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

## Git Repository Structure

### Monorepo Approach (Recommended)

```
payments-engine-gitops/
│
├── README.md
│
├── apps/                              # Application definitions
│   │
│   ├── payment-service/
│   │   ├── base/                      # Base configuration
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   ├── configmap.yaml
│   │   │   ├── hpa.yaml               # Horizontal Pod Autoscaler
│   │   │   └── kustomization.yaml
│   │   │
│   │   ├── overlays/                  # Environment-specific
│   │   │   ├── dev/
│   │   │   │   ├── kustomization.yaml # Dev overrides
│   │   │   │   └── configmap-patch.yaml
│   │   │   │
│   │   │   ├── staging/
│   │   │   │   ├── kustomization.yaml
│   │   │   │   └── replicas-patch.yaml
│   │   │   │
│   │   │   └── production/
│   │   │       ├── kustomization.yaml
│   │   │       ├── replicas-patch.yaml  # 10 replicas
│   │   │       └── resources-patch.yaml # Higher limits
│   │   │
│   │   └── argocd-app.yaml            # ArgoCD Application definition
│   │
│   ├── validation-service/
│   │   └── ... (same structure)
│   │
│   ├── account-adapter/
│   │   └── ... (same structure)
│   │
│   └── ... (14 more services)
│
├── infrastructure/                    # Infrastructure components
│   │
│   ├── istio/
│   │   ├── gateway.yaml
│   │   ├── virtualservice.yaml
│   │   └── destinationrule.yaml
│   │
│   ├── monitoring/
│   │   ├── prometheus/
│   │   ├── grafana/
│   │   └── jaeger/
│   │
│   ├── ingress/
│   │   └── nginx-ingress.yaml
│   │
│   └── cert-manager/
│       └── certificates.yaml
│
├── argocd/                            # ArgoCD configuration
│   ├── projects/
│   │   └── payments-project.yaml      # ArgoCD Project
│   │
│   ├── applications/
│   │   ├── payment-service-app.yaml
│   │   ├── validation-service-app.yaml
│   │   └── ... (15 more)
│   │
│   └── applicationsets/               # App of Apps pattern
│       └── all-services.yaml
│
└── scripts/
    ├── create-app.sh                  # Helper scripts
    └── sync-all.sh
```

### Why Kustomize (Not Helm)

| Feature | Kustomize | Helm |
|---------|-----------|------|
| **Template-free** | ✅ Pure YAML | ❌ Go templates |
| **Overlays** | ✅ Patch-based | ⚠️ Values-based |
| **Built-in kubectl** | ✅ Yes | ❌ Separate tool |
| **Learning Curve** | ✅ Low | ⚠️ Medium |
| **ArgoCD Support** | ✅ Native | ✅ Yes |

**Decision**: Kustomize (simpler, no templating language)

---

## ArgoCD Application Definition

### Example: Payment Service

```yaml
# argocd/applications/payment-service-app.yaml

apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: payment-service
  namespace: argocd
  
  # Finalizer for cascading delete
  finalizers:
    - resources-finalizer.argocd.argoproj.io

spec:
  # Project (RBAC boundary)
  project: payments-project
  
  # Source: Git repository
  source:
    repoURL: https://github.com/payments-engine/gitops.git
    targetRevision: main  # Branch
    path: apps/payment-service/overlays/production
    
    # Kustomize options
    kustomize:
      namePrefix: prod-
      commonLabels:
        environment: production
        team: payments
  
  # Destination: Kubernetes cluster
  destination:
    server: https://kubernetes.default.svc
    namespace: payments
  
  # Sync policy
  syncPolicy:
    automated:
      # Auto-sync when Git changes
      prune: true           # Delete resources not in Git
      selfHeal: true        # Revert manual changes
      allowEmpty: false     # Don't sync if no resources
    
    syncOptions:
      - CreateNamespace=true
      - PruneLast=true      # Delete resources last
    
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
  
  # Ignore differences (don't trigger sync)
  ignoreDifferences:
    - group: apps
      kind: Deployment
      jsonPointers:
        - /spec/replicas  # Ignore if HPA changes replicas
```

### Application States

```
Application Lifecycle:

┌─────────────────────────────────────────────────────────┐
│  Health Status                                          │
│                                                         │
│  ✅ Healthy       - All resources healthy               │
│  ⚠️  Progressing  - Deployment in progress              │
│  ⚠️  Degraded     - Some resources unhealthy            │
│  ❌ Missing       - Resources not found                 │
│  ⏸️  Suspended    - No running resources                │
│  ❓ Unknown       - Health cannot be determined         │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Sync Status                                            │
│                                                         │
│  ✅ Synced        - Git matches cluster                 │
│  ⚠️  OutOfSync    - Git differs from cluster            │
│  ❓ Unknown       - Cannot determine                    │
└─────────────────────────────────────────────────────────┘
```

---

## Deployment Workflows

### Workflow 1: New Version Deployment

```
Developer Flow:

1. Developer merges PR (payment-service v1.3.0)
   ↓
2. CI Pipeline builds Docker image
   ↓
3. CI updates Git (gitops repo):
   apps/payment-service/overlays/production/kustomization.yaml
   
   images:
     - name: payment-service
       newTag: v1.3.0  # Changed from v1.2.0
   ↓
4. Git commit pushed
   ↓
5. ArgoCD detects change (within 3 minutes)
   ↓
6. ArgoCD syncs:
   - Pulls new image
   - Rolling update deployment
   - Monitors health
   ↓
7. Deployment complete (visible in UI)

Time: 3-5 minutes (automated)
```

### Workflow 2: Configuration Change

```
DevOps Flow:

1. Need to increase replicas (5 → 10)
   ↓
2. Edit Git:
   apps/payment-service/overlays/production/replicas-patch.yaml
   
   spec:
     replicas: 10  # Changed from 5
   ↓
3. Create PR → Review → Merge
   ↓
4. ArgoCD detects change
   ↓
5. ArgoCD scales deployment to 10 replicas
   ↓
6. Complete

Audit Trail: Git commit shows who, what, when, why ✅
```

### Workflow 3: Rollback

```
Rollback Flow:

1. v1.3.0 has bug, need to rollback to v1.2.0
   ↓
2. Git revert:
   git revert HEAD  # Reverts last commit
   git push
   ↓
3. ArgoCD detects revert
   ↓
4. ArgoCD deploys v1.2.0 (previous version)
   ↓
5. Rollback complete

Time: 3-5 minutes (automated)
Compare to manual: kubectl rollout undo (error-prone)
```

---

## Multi-Environment Strategy

### Environment Promotion

```
Environments: dev → staging → production

Git Branches:
- main          (production)
- staging       (staging)
- develop       (dev)

Promotion Flow:

1. Developer merges to develop branch
   ↓ ArgoCD syncs to DEV cluster
   ↓
2. QA tests in dev
   ↓
3. Promote: Merge develop → staging
   ↓ ArgoCD syncs to STAGING cluster
   ↓
4. Integration tests in staging
   ↓
5. Promote: Merge staging → main
   ↓ ArgoCD syncs to PRODUCTION cluster
   ↓
6. Live in production

Each environment has separate ArgoCD Application:
- payment-service-dev       → develop branch
- payment-service-staging   → staging branch
- payment-service-prod      → main branch
```

### Environment Differences (Kustomize Overlays)

```yaml
# Base (shared configuration)
apps/payment-service/base/
├── deployment.yaml          # 3 replicas (base)
├── service.yaml
└── configmap.yaml           # DB: generic connection

# Dev overlay (minimal resources)
apps/payment-service/overlays/dev/
└── kustomization.yaml
    resources:
      - ../../base
    replicas:
      - name: payment-service
        count: 1              # 1 replica (dev)
    configMapGenerator:
      - name: payment-config
        literals:
          - DB_HOST=dev-postgres

# Production overlay (maximum resources)
apps/payment-service/overlays/production/
└── kustomization.yaml
    resources:
      - ../../base
    replicas:
      - name: payment-service
        count: 10             # 10 replicas (prod)
    configMapGenerator:
      - name: payment-config
        literals:
          - DB_HOST=prod-postgres-primary
    patchesStrategicMerge:
      - resources-patch.yaml  # Higher CPU/memory limits
```

---

## Multi-Tenancy with GitOps

### Tenant-Specific Deployments

```
Option 1: Separate namespaces per tenant

apps/payment-service/overlays/
├── tenant-std-001/           # Standard Bank
│   └── kustomization.yaml    # 10 replicas, high resources
├── tenant-ned-001/           # Nedbank
│   └── kustomization.yaml    # 5 replicas, medium resources
└── tenant-shared/            # Other tenants
    └── kustomization.yaml    # 3 replicas, shared pool

ArgoCD Applications:
- payment-service-std-001  → namespace: payments-std-001
- payment-service-ned-001  → namespace: payments-ned-001
- payment-service-shared   → namespace: payments-shared
```

### Configuration Per Tenant

```yaml
# Tenant-specific configurations stored in Git

apps/payment-service/overlays/tenant-std-001/
├── kustomization.yaml
├── configmap-patch.yaml      # Standard Bank config
│   data:
│     TENANT_ID: "STD-001"
│     MAX_TPS: "10000"
│     FRAUD_THRESHOLD: "5000"
│     CLEARING_ENDPOINT: "https://std-clearing.co.za"
└── env-patch.yaml

All changes version controlled ✅
All changes auditable (Git history) ✅
```

---

## Security & RBAC

### ArgoCD Projects (RBAC)

```yaml
# Separate project per team

apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: payments-team
  namespace: argocd
spec:
  description: Payments Team Services
  
  # Source repositories (Git)
  sourceRepos:
    - 'https://github.com/payments-engine/gitops.git'
  
  # Destination clusters and namespaces
  destinations:
    - namespace: payments
      server: https://kubernetes.default.svc
    - namespace: payments-*  # Wildcard for tenant namespaces
      server: https://kubernetes.default.svc
  
  # Allowed resource types
  clusterResourceWhitelist:
    - group: ''
      kind: Namespace
  
  namespaceResourceWhitelist:
    - group: 'apps'
      kind: Deployment
    - group: ''
      kind: Service
    - group: ''
      kind: ConfigMap
  
  # RBAC roles
  roles:
    - name: developer
      description: Developers can view and sync
      policies:
        - p, proj:payments-team:developer, applications, get, *, allow
        - p, proj:payments-team:developer, applications, sync, *, allow
    
    - name: admin
      description: Full access
      policies:
        - p, proj:payments-team:admin, applications, *, *, allow
```

### Git Repository Access

```yaml
# ArgoCD credentials for private Git repo

apiVersion: v1
kind: Secret
metadata:
  name: github-repo
  namespace: argocd
  labels:
    argocd.argoproj.io/secret-type: repository
type: Opaque
stringData:
  type: git
  url: https://github.com/payments-engine/gitops.git
  username: argocd-bot
  password: ${GITHUB_PAT}  # Personal Access Token from Key Vault
```

---

## Monitoring & Notifications

### ArgoCD Notifications

```yaml
# Notify team on sync status

apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-notifications-cm
  namespace: argocd
data:
  # Slack template
  template.app-sync-status: |
    message: |
      Application {{.app.metadata.name}} sync {{.app.status.operationState.phase}}.
      Revision: {{.app.status.sync.revision}}
      {{if eq .app.status.operationState.phase "Succeeded"}}✅{{end}}
      {{if eq .app.status.operationState.phase "Failed"}}❌{{end}}
  
  # Triggers
  trigger.on-sync-succeeded: |
    - when: app.status.operationState.phase in ['Succeeded']
      send: [slack]
  
  trigger.on-sync-failed: |
    - when: app.status.operationState.phase in ['Failed']
      send: [slack, email]
  
  # Slack webhook
  service.slack: |
    token: ${SLACK_BOT_TOKEN}
    channel: payments-deployments
```

### Integration with Azure Monitor

```
ArgoCD Metrics → Prometheus → Azure Monitor

Metrics:
- argocd_app_sync_total           # Total syncs
- argocd_app_sync_failed          # Failed syncs
- argocd_app_health_status        # Health status
- argocd_app_sync_duration        # Sync duration

Alerts:
- Sync failures (> 5 in 10 minutes)
- Apps out of sync (> 30 minutes)
- Apps unhealthy (> 10 minutes)
```

---

## Disaster Recovery

### Backup Strategy

```
ArgoCD State:
- Application definitions (in Git) ✅
- Configuration (in Git) ✅
- Secrets (in Azure Key Vault) ✅

Backup Needed:
- ArgoCD database (Redis)
  - Application state
  - Sync history
  
Backup Schedule:
- Redis snapshots: Every 6 hours
- Stored in Azure Blob Storage
- Retention: 30 days

Recovery:
1. Reinstall ArgoCD
2. Restore Redis from snapshot
3. ArgoCD re-syncs from Git
4. All applications restored ✅
```

### Cluster Rebuild

```
Scenario: Complete cluster failure

Recovery Steps:
1. Create new AKS cluster
2. Install ArgoCD
3. Configure Git repository connection
4. Create ArgoCD Applications (from Git)
5. ArgoCD deploys all 17 services
6. Cluster fully recovered

Time: 30-60 minutes (automated)
Source: Git (single source of truth) ✅
```

---

## Cost-Benefit Analysis

### Costs

| Cost Type | Impact |
|-----------|--------|
| **ArgoCD Infrastructure** | ~$50/month (small pods) |
| **Initial Setup** | 1-2 weeks (one-time) |
| **Learning Curve** | 1 week (team training) |
| **Git Repository** | Free (GitHub) |

**Total**: ~$600/year + initial setup

### Benefits

| Benefit | Value |
|---------|-------|
| **Deployment Automation** | $10,000/year (ops time saved) |
| **Reduced Errors** | 90% fewer deployment errors |
| **Audit Trail** | Compliance (Git history) |
| **Rollback Speed** | 5 minutes vs 30 minutes |
| **Self-Healing** | Automatic drift correction |

**Total**: $10,000+/year value

**ROI**: 15-20x return

---

## Implementation Checklist

### Week 1: ArgoCD Installation

- [ ] Install ArgoCD on AKS cluster
- [ ] Expose ArgoCD UI (LoadBalancer or Ingress)
- [ ] Configure admin password
- [ ] Install ArgoCD CLI
- [ ] Test ArgoCD accessible

### Week 2: Git Repository Setup

- [ ] Create gitops repository (GitHub)
- [ ] Set up directory structure
- [ ] Create Kustomize base for 1 service (pilot)
- [ ] Create overlays (dev, staging, production)
- [ ] Create ArgoCD Application definition
- [ ] Configure Git credentials in ArgoCD

### Week 3: Pilot Service

- [ ] Deploy 1 service via ArgoCD (Payment Service)
- [ ] Test sync (manual)
- [ ] Enable auto-sync
- [ ] Test self-healing (manual kubectl change)
- [ ] Test rollback (git revert)
- [ ] Validate works as expected

### Week 4: Rollout to All Services

- [ ] Create Kustomize configs for all 17 services
- [ ] Create ArgoCD Applications for all
- [ ] Migrate from kubectl to GitOps (one by one)
- [ ] Set up notifications (Slack)
- [ ] Create runbooks
- [ ] Train team

### Week 5: Advanced Features

- [ ] Set up ApplicationSet (App of Apps)
- [ ] Configure RBAC (ArgoCD Projects)
- [ ] Integrate with CI pipeline (auto-update Git)
- [ ] Set up monitoring dashboards
- [ ] Document workflows

---

## Summary

### What GitOps Provides

✅ **Automated Deployments**: No more kubectl commands  
✅ **Audit Trail**: Git history shows all changes  
✅ **Easy Rollback**: `git revert` to rollback  
✅ **Self-Healing**: Automatic drift correction  
✅ **Declarative**: Desired state in Git  
✅ **Multi-Environment**: Dev, staging, production from same repo  

### Implementation Effort

**Total**: 1-2 weeks
- Week 1: Install ArgoCD, set up Git repo
- Week 2: Migrate all 17 services

### Result

**Eliminate manual deployments**, replacing them with automated Git-driven deployments. Complete audit trail via Git history. Self-healing cluster that reverts manual changes. **$10K/year operational savings**.

---

## Related Documents

- **[13-MODERN-ARCHITECTURE-PATTERNS.md](13-MODERN-ARCHITECTURE-PATTERNS.md)** - GitOps overview
- **[07-AZURE-INFRASTRUCTURE.md](07-AZURE-INFRASTRUCTURE.md)** - AKS infrastructure
- **[17-SERVICE-MESH-ISTIO.md](17-SERVICE-MESH-ISTIO.md)** - Istio deployment via GitOps

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Phase**: 2 (Production Hardening)
