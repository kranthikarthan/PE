#!/bin/bash
# Air-Gapped Deployment Script
# This script deploys the Payment Engine in an air-gapped environment

set -e

# Configuration
NAMESPACE="payment-engine"
REGISTRY_HOST="airgap-registry.company.com"
REGISTRY_PORT="5000"
LOCAL_REGISTRY="local-registry:5000"
APP_NAME="payment-engine"
VERSION="latest"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    log_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if helm is available
if ! command -v helm &> /dev/null; then
    log_error "helm is not installed or not in PATH"
    exit 1
fi

# Check if docker is available
if ! command -v docker &> /dev/null; then
    log_error "docker is not installed or not in PATH"
    exit 1
fi

log_info "Starting air-gapped deployment of Payment Engine..."

# Step 1: Create namespace
log_step "Creating namespace..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Step 2: Load application images
log_step "Loading application images..."
if [ -f "middleware-*.tar" ]; then
    log_info "Loading middleware image..."
    docker load < middleware-*.tar
    docker tag $REGISTRY_HOST:$REGISTRY_PORT/$APP_NAME-middleware:* $LOCAL_REGISTRY/$APP_NAME-middleware:$VERSION
    docker push $LOCAL_REGISTRY/$APP_NAME-middleware:$VERSION
fi

if [ -f "payment-engine-*.tar" ]; then
    log_info "Loading payment engine image..."
    docker load < payment-engine-*.tar
    docker tag $REGISTRY_HOST:$REGISTRY_PORT/$APP_NAME-payment-engine:* $LOCAL_REGISTRY/$APP_NAME-payment-engine:$VERSION
    docker push $LOCAL_REGISTRY/$APP_NAME-payment-engine:$VERSION
fi

if [ -f "frontend-*.tar" ]; then
    log_info "Loading frontend image..."
    docker load < frontend-*.tar
    docker tag $REGISTRY_HOST:$REGISTRY_PORT/$APP_NAME-frontend:* $LOCAL_REGISTRY/$APP_NAME-frontend:$VERSION
    docker push $LOCAL_REGISTRY/$APP_NAME-frontend:$VERSION
fi

# Step 3: Create ConfigMaps and Secrets
log_step "Creating ConfigMaps and Secrets..."

# Database configuration
kubectl create configmap database-config \
    --from-literal=DB_HOST=postgres-service \
    --from-literal=DB_PORT=5432 \
    --from-literal=DB_NAME=payment_engine \
    --from-literal=DB_USER=payment_user \
    --namespace=$NAMESPACE \
    --dry-run=client -o yaml | kubectl apply -f -

# Redis configuration
kubectl create configmap redis-config \
    --from-literal=REDIS_HOST=redis-service \
    --from-literal=REDIS_PORT=6379 \
    --namespace=$NAMESPACE \
    --dry-run=client -o yaml | kubectl apply -f -

# Application configuration
kubectl create configmap app-config \
    --from-literal=SPRING_PROFILES_ACTIVE=airgap \
    --from-literal=LOG_LEVEL=INFO \
    --from-literal=JAVA_OPTS="-Xmx2g -Xms1g" \
    --namespace=$NAMESPACE \
    --dry-run=client -o yaml | kubectl apply -f -

# Database secrets
kubectl create secret generic database-secret \
    --from-literal=DB_PASSWORD=secure_password_123 \
    --namespace=$NAMESPACE \
    --dry-run=client -o yaml | kubectl apply -f -

# JWT secrets
kubectl create secret generic jwt-secret \
    --from-literal=JWT_SECRET=your-super-secret-jwt-key-here \
    --namespace=$NAMESPACE \
    --dry-run=client -o yaml | kubectl apply -f -

# Step 4: Deploy PostgreSQL
log_step "Deploying PostgreSQL..."
cat > postgres-deployment.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: $NAMESPACE
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: payment_engine
        - name: POSTGRES_USER
          value: payment_user
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: DB_PASSWORD
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: $NAMESPACE
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: $NAMESPACE
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
EOF

kubectl apply -f postgres-deployment.yaml

# Step 5: Deploy Redis
log_step "Deploying Redis..."
cat > redis-deployment.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: $NAMESPACE
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "250m"
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: $NAMESPACE
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
EOF

kubectl apply -f redis-deployment.yaml

# Step 6: Deploy Middleware Service
log_step "Deploying Middleware Service..."
cat > middleware-deployment.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: middleware
  namespace: $NAMESPACE
spec:
  replicas: 2
  selector:
    matchLabels:
      app: middleware
  template:
    metadata:
      labels:
        app: middleware
    spec:
      containers:
      - name: middleware
        image: $LOCAL_REGISTRY/$APP_NAME-middleware:$VERSION
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_PROFILES_ACTIVE
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_HOST
        - name: DB_PORT
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_PORT
        - name: DB_NAME
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_NAME
        - name: DB_USER
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_USER
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: DB_PASSWORD
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: redis-config
              key: REDIS_HOST
        - name: REDIS_PORT
          valueFrom:
            configMapKeyRef:
              name: redis-config
              key: REDIS_PORT
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: JWT_SECRET
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
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
  name: middleware-service
  namespace: $NAMESPACE
spec:
  selector:
    app: middleware
  ports:
  - port: 8080
    targetPort: 8080
EOF

kubectl apply -f middleware-deployment.yaml

# Step 7: Deploy Payment Engine Service
log_step "Deploying Payment Engine Service..."
cat > payment-engine-deployment.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-engine
  namespace: $NAMESPACE
spec:
  replicas: 2
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
        image: $LOCAL_REGISTRY/$APP_NAME-payment-engine:$VERSION
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_PROFILES_ACTIVE
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_HOST
        - name: DB_PORT
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_PORT
        - name: DB_NAME
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_NAME
        - name: DB_USER
          valueFrom:
            configMapKeyRef:
              name: database-config
              key: DB_USER
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: DB_PASSWORD
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: redis-config
              key: REDIS_HOST
        - name: REDIS_PORT
          valueFrom:
            configMapKeyRef:
              name: redis-config
              key: REDIS_PORT
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: JWT_SECRET
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
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
  namespace: $NAMESPACE
spec:
  selector:
    app: payment-engine
  ports:
  - port: 8080
    targetPort: 8080
EOF

kubectl apply -f payment-engine-deployment.yaml

# Step 8: Deploy Frontend
log_step "Deploying Frontend..."
cat > frontend-deployment.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: $NAMESPACE
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: $LOCAL_REGISTRY/$APP_NAME-frontend:$VERSION
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
  namespace: $NAMESPACE
spec:
  selector:
    app: frontend
  ports:
  - port: 80
    targetPort: 80
EOF

kubectl apply -f frontend-deployment.yaml

# Step 9: Deploy Ingress
log_step "Deploying Ingress..."
cat > ingress.yaml << EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: payment-engine-ingress
  namespace: $NAMESPACE
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  rules:
  - host: payment-engine.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend-service
            port:
              number: 80
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: middleware-service
            port:
              number: 8080
      - path: /payment-engine
        pathType: Prefix
        backend:
          service:
            name: payment-engine-service
            port:
              number: 8080
EOF

kubectl apply -f ingress.yaml

# Step 10: Wait for deployments
log_step "Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/postgres -n $NAMESPACE
kubectl wait --for=condition=available --timeout=300s deployment/redis -n $NAMESPACE
kubectl wait --for=condition=available --timeout=300s deployment/middleware -n $NAMESPACE
kubectl wait --for=condition=available --timeout=300s deployment/payment-engine -n $NAMESPACE
kubectl wait --for=condition=available --timeout=300s deployment/frontend -n $NAMESPACE

# Step 11: Run database migrations
log_step "Running database migrations..."
kubectl run migration-job --image=$LOCAL_REGISTRY/$APP_NAME-middleware:$VERSION \
    --restart=Never \
    --namespace=$NAMESPACE \
    --env="SPRING_PROFILES_ACTIVE=airgap" \
    --env="DB_HOST=postgres-service" \
    --env="DB_PORT=5432" \
    --env="DB_NAME=payment_engine" \
    --env="DB_USER=payment_user" \
    --env="DB_PASSWORD=secure_password_123" \
    --command -- java -jar /app.jar --spring.flyway.enabled=true

# Wait for migration to complete
kubectl wait --for=condition=complete --timeout=300s job/migration-job -n $NAMESPACE

# Clean up migration job
kubectl delete job migration-job -n $NAMESPACE

# Step 12: Display deployment status
log_step "Deployment completed successfully!"
echo ""
log_info "Deployment Status:"
kubectl get pods -n $NAMESPACE
echo ""
log_info "Services:"
kubectl get services -n $NAMESPACE
echo ""
log_info "Ingress:"
kubectl get ingress -n $NAMESPACE
echo ""

# Clean up temporary files
rm -f postgres-deployment.yaml redis-deployment.yaml middleware-deployment.yaml
rm -f payment-engine-deployment.yaml frontend-deployment.yaml ingress.yaml

log_info "Air-gapped deployment completed successfully!"
log_info "Application is accessible at: http://payment-engine.local"
log_info "API endpoints:"
log_info "  - Frontend: http://payment-engine.local/"
log_info "  - Middleware API: http://payment-engine.local/api"
log_info "  - Payment Engine API: http://payment-engine.local/payment-engine"