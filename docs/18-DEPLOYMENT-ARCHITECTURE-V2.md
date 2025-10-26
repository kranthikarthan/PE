# Deployment Architecture v2 - Enhanced Deployment Strategy

## 🎯 **Deployment Architecture Overview**

The Payments Engine v2 implements a comprehensive deployment architecture with Kubernetes, Helm charts, CI/CD pipelines, and multi-environment support for ISO 20022 message processing and UETR correlation.

## 🏗️ **Deployment Strategy**

### **Deployment Environments**
```yaml
Development:
  - Purpose: Development and testing
  - Namespace: payments-engine-dev
  - Branch: develop
  - Auto-deploy: true
  - Resources: Minimal

Staging:
  - Purpose: Pre-production testing
  - Namespace: payments-engine-staging
  - Branch: staging
  - Auto-deploy: false
  - Resources: Production-like

Production:
  - Purpose: Live production system
  - Namespace: payments-engine-production
  - Branch: main
  - Auto-deploy: false
  - Resources: Full production
```

### **Deployment Principles**
```yaml
Blue-Green Deployment:
  - Zero-downtime deployments
  - Instant rollback capability
  - Traffic switching
  - Database migration support

Canary Deployment:
  - Gradual traffic shifting
  - Risk mitigation
  - Performance monitoring
  - Automatic rollback

GitOps:
  - Git-based configuration
  - Automated deployments
  - Configuration drift detection
  - Audit trails
```

## 🚀 **Kubernetes Deployment**

### **Namespace Configuration**
```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: payments-engine
  labels:
    name: payments-engine
    environment: production
    tier: application
---
apiVersion: v1
kind: Namespace
metadata:
  name: payments-engine-staging
  labels:
    name: payments-engine-staging
    environment: staging
    tier: application
---
apiVersion: v1
kind: Namespace
metadata:
  name: payments-engine-dev
  labels:
    name: payments-engine-dev
    environment: development
    tier: application
```

### **Payment Initiation Service Deployment**
```yaml
# payment-initiation-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-initiation-service
  namespace: payments-engine
  labels:
    app: payment-initiation-service
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-initiation-service
  template:
    metadata:
      labels:
        app: payment-initiation-service
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: payment-initiation-service
      containers:
      - name: payment-initiation-service
        image: payments-engine/payment-initiation-service:latest
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 8081
          name: management
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: password
        - name: REDIS_URL
          valueFrom:
            secretKeyRef:
              name: redis-secrets
              key: url
        - name: KAFKA_BOOTSTRAP_SERVERS
          valueFrom:
            configMapKeyRef:
              name: kafka-config
              key: bootstrap-servers
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
      imagePullSecrets:
      - name: registry-secret
      nodeSelector:
        node-type: application
      tolerations:
      - key: application
        operator: Equal
        value: "true"
        effect: NoSchedule
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
                  - payment-initiation-service
              topologyKey: kubernetes.io/hostname
---
apiVersion: v1
kind: Service
metadata:
  name: payment-initiation-service
  namespace: payments-engine
  labels:
    app: payment-initiation-service
spec:
  selector:
    app: payment-initiation-service
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: management
    port: 8081
    targetPort: 8081
    protocol: TCP
  type: ClusterIP
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: payment-initiation-service
  namespace: payments-engine
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: payment-initiation-service
  namespace: payments-engine
rules:
- apiGroups: [""]
  resources: ["secrets", "configmaps"]
  verbs: ["get", "list", "watch"]
- apiGroups: ["apps"]
  resources: ["deployments"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: payment-initiation-service
  namespace: payments-engine
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: payment-initiation-service
subjects:
- kind: ServiceAccount
  name: payment-initiation-service
  namespace: payments-engine
```

### **Database Deployment**
```yaml
# postgresql-deployment.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgresql
  namespace: payments-engine
spec:
  serviceName: postgresql
  replicas: 1
  selector:
    matchLabels:
      app: postgresql
  template:
    metadata:
      labels:
        app: postgresql
    spec:
      containers:
      - name: postgresql
        image: postgres:13
        ports:
        - containerPort: 5432
          name: postgresql
        env:
        - name: POSTGRES_DB
          value: "payments_engine"
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: postgresql-secrets
              key: username
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgresql-secrets
              key: password
        volumeMounts:
        - name: postgresql-data
          mountPath: /var/lib/postgresql/data
        - name: postgresql-config
          mountPath: /etc/postgresql
        resources:
          requests:
            cpu: 1000m
            memory: 2Gi
          limits:
            cpu: 4000m
            memory: 8Gi
        livenessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - postgres
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - postgres
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: postgresql-data
        persistentVolumeClaim:
          claimName: postgresql-data
      - name: postgresql-config
        configMap:
          name: postgresql-config
---
apiVersion: v1
kind: Service
metadata:
  name: postgresql
  namespace: payments-engine
spec:
  selector:
    app: postgresql
  ports:
  - port: 5432
    targetPort: 5432
    name: postgresql
  type: ClusterIP
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgresql-data
  namespace: payments-engine
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 100Gi
  storageClassName: fast-ssd
```

## 🔄 **CI/CD Pipeline**

### **GitHub Actions Workflow**
```yaml
# .github/workflows/deploy.yml
name: Deploy Payments Engine

on:
  push:
    branches: [main, staging, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: payments-engine

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests
      run: mvn clean test
    
    - name: Run integration tests
      run: mvn clean verify
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/staging' || github.ref == 'refs/heads/develop'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build application
      run: mvn clean package -DskipTests
    
    - name: Build Docker image
      run: |
        docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/payment-initiation-service:${{ github.sha }} .
        docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/validation-service:${{ github.sha }} .
        docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/account-adapter-service:${{ github.sha }} .
    
    - name: Login to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Push Docker images
      run: |
        docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/payment-initiation-service:${{ github.sha }}
        docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/validation-service:${{ github.sha }}
        docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/account-adapter-service:${{ github.sha }}

  deploy-dev:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: development
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG_DEV }}
    
    - name: Deploy to development
      run: |
        helm upgrade --install payments-engine-dev ./helm/payments-engine \
          --namespace payments-engine-dev \
          --create-namespace \
          --set image.tag=${{ github.sha }} \
          --set environment=development \
          --set replicas=1

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/staging'
    environment: staging
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG_STAGING }}
    
    - name: Deploy to staging
      run: |
        helm upgrade --install payments-engine-staging ./helm/payments-engine \
          --namespace payments-engine-staging \
          --create-namespace \
          --set image.tag=${{ github.sha }} \
          --set environment=staging \
          --set replicas=2

  deploy-production:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG_PROD }}
    
    - name: Deploy to production
      run: |
        helm upgrade --install payments-engine ./helm/payments-engine \
          --namespace payments-engine \
          --create-namespace \
          --set image.tag=${{ github.sha }} \
          --set environment=production \
          --set replicas=3
```

### **Helm Chart Structure**
```yaml
# helm/payments-engine/Chart.yaml
apiVersion: v2
name: payments-engine
description: Payments Engine v2 Helm Chart
version: 1.0.0
appVersion: "1.0.0"
dependencies:
- name: postgresql
  version: 12.1.2
  repository: https://charts.bitnami.com/bitnami
- name: redis
  version: 16.5.0
  repository: https://charts.bitnami.com/bitnami
- name: kafka
  version: 19.1.0
  repository: https://charts.bitnami.com/bitnami
```

### **Helm Values**
```yaml
# helm/payments-engine/values.yaml
replicaCount: 3

image:
  repository: ghcr.io/payments-engine/payments-engine
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080

ingress:
  enabled: true
  className: "istio"
  annotations:
    kubernetes.io/ingress.class: istio
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: payments-engine.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: payments-engine-tls
      hosts:
        - payments-engine.com

resources:
  limits:
    cpu: 2000m
    memory: 4Gi
  requests:
    cpu: 500m
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

nodeSelector: {}
tolerations: []
affinity: {}

postgresql:
  enabled: true
  auth:
    postgresPassword: "postgres"
    database: "payments_engine"
  primary:
    persistence:
      enabled: true
      size: 100Gi
    resources:
      limits:
        cpu: 4000m
        memory: 8Gi
      requests:
        cpu: 1000m
        memory: 2Gi

redis:
  enabled: true
  auth:
    enabled: false
  master:
    persistence:
      enabled: true
      size: 50Gi
    resources:
      limits:
        cpu: 1000m
        memory: 2Gi
      requests:
        cpu: 250m
        memory: 512Mi

kafka:
  enabled: true
  persistence:
    enabled: true
    size: 100Gi
  resources:
    limits:
      cpu: 2000m
      memory: 4Gi
    requests:
      cpu: 500m
      memory: 1Gi
```

## 🔄 **Blue-Green Deployment**

### **Blue-Green Configuration**
```yaml
# blue-green-deployment.yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: payment-initiation-service
  namespace: payments-engine
spec:
  replicas: 3
  strategy:
    blueGreen:
      activeService: payment-initiation-service-active
      previewService: payment-initiation-service-preview
      autoPromotionEnabled: false
      scaleDownDelaySeconds: 30
      prePromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: payment-initiation-service-preview
      postPromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: payment-initiation-service-active
  selector:
    matchLabels:
      app: payment-initiation-service
  template:
    metadata:
      labels:
        app: payment-initiation-service
    spec:
      containers:
      - name: payment-initiation-service
        image: payments-engine/payment-initiation-service:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
---
apiVersion: v1
kind: Service
metadata:
  name: payment-initiation-service-active
  namespace: payments-engine
spec:
  selector:
    app: payment-initiation-service
  ports:
  - port: 8080
    targetPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: payment-initiation-service-preview
  namespace: payments-engine
spec:
  selector:
    app: payment-initiation-service
  ports:
  - port: 8080
    targetPort: 8080
```

### **Canary Deployment**
```yaml
# canary-deployment.yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: payment-initiation-service-canary
  namespace: payments-engine
spec:
  replicas: 3
  strategy:
    canary:
      steps:
      - setWeight: 20
      - pause: {duration: 10m}
      - setWeight: 40
      - pause: {duration: 10m}
      - setWeight: 60
      - pause: {duration: 10m}
      - setWeight: 80
      - pause: {duration: 10m}
      analysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: payment-initiation-service
        startingStep: 2
        successfulHistoryLimit: 1
        unsuccessfulHistoryLimit: 1
  selector:
    matchLabels:
      app: payment-initiation-service
  template:
    metadata:
      labels:
        app: payment-initiation-service
    spec:
      containers:
      - name: payment-initiation-service
        image: payments-engine/payment-initiation-service:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
```

## 📊 **Deployment Monitoring**

### **Deployment Metrics**
```yaml
Deployment KPIs:
  - Deployment success rate
  - Deployment duration
  - Rollback frequency
  - Service availability
  - Performance impact

Monitoring:
  - Deployment status
  - Health checks
  - Performance metrics
  - Error rates
  - Resource utilization
```

### **Deployment Dashboards**
```yaml
Deployment Overview Dashboard:
  - Deployment history
  - Success/failure rates
  - Deployment duration
  - Rollback frequency

Service Health Dashboard:
  - Service availability
  - Response times
  - Error rates
  - Resource utilization
  - Performance trends
```

---

**Version**: 2.0  
**Last Updated**: 2025-01-27  
**Status**: 🚀 Ready for Implementation  
**Next Review**: Weekly during implementation
