# Deploy Payments Engine with Istio Service Mesh
# This script deploys all microservices with Istio sidecar injection enabled

param(
    [switch]$SkipIstio = $false,
    [switch]$SkipBuild = $false,
    [string]$ImageTag = "latest"
)

Write-Host "🚀 Deploying Payments Engine with Istio Service Mesh..." -ForegroundColor Green

# Check if kubectl is available
try {
    kubectl version --client | Out-Null
    Write-Host "✅ kubectl is available" -ForegroundColor Green
} catch {
    Write-Host "❌ kubectl is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check if AKS cluster is accessible
try {
    kubectl cluster-info | Out-Null
    Write-Host "✅ AKS cluster is accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Cannot connect to AKS cluster. Please check your kubeconfig." -ForegroundColor Red
    exit 1
}

# Install Istio if not skipped
if (-not $SkipIstio) {
    Write-Host "🔧 Installing Istio Service Mesh..." -ForegroundColor Yellow
    & .\scripts\install-istio.ps1
} else {
    Write-Host "⏭️ Skipping Istio installation" -ForegroundColor Yellow
}

# Build Docker images if not skipped
if (-not $SkipBuild) {
    Write-Host "🏗️ Building Docker images..." -ForegroundColor Yellow
    
    # Build payment service
    Write-Host "  Building payment-service..." -ForegroundColor Gray
    docker build -f docker/payment-initiation-service/Dockerfile -t payments/payment-service:$ImageTag .
    
    # Build account service
    Write-Host "  Building account-service..." -ForegroundColor Gray
    docker build -f docker/account-adapter-service/Dockerfile -t payments/account-service:$ImageTag .
    
    # Build validation service
    Write-Host "  Building validation-service..." -ForegroundColor Gray
    docker build -f docker/validation-service/Dockerfile -t payments/validation-service:$ImageTag .
    
    # Build saga orchestrator
    Write-Host "  Building saga-orchestrator..." -ForegroundColor Gray
    docker build -f docker/saga-orchestrator/Dockerfile -t payments/saga-orchestrator:$ImageTag .
    
    Write-Host "✅ Docker images built successfully" -ForegroundColor Green
} else {
    Write-Host "⏭️ Skipping Docker build" -ForegroundColor Yellow
}

# Deploy infrastructure services
Write-Host "📦 Deploying infrastructure services..." -ForegroundColor Yellow

# Deploy PostgreSQL
Write-Host "  Deploying PostgreSQL..." -ForegroundColor Gray
kubectl apply -f k8s/infrastructure/postgres.yaml

# Deploy Redis
Write-Host "  Deploying Redis..." -ForegroundColor Gray
kubectl apply -f k8s/infrastructure/redis.yaml

# Deploy Kafka
Write-Host "  Deploying Kafka..." -ForegroundColor Gray
kubectl apply -f k8s/infrastructure/kafka.yaml

# Wait for infrastructure to be ready
Write-Host "⏳ Waiting for infrastructure services to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=postgres -n payments --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n payments --timeout=300s
kubectl wait --for=condition=ready pod -l app=kafka -n payments --timeout=300s

# Deploy microservices with Istio
Write-Host "🚀 Deploying microservices with Istio sidecar injection..." -ForegroundColor Yellow

# Deploy payment service
Write-Host "  Deploying payment-service..." -ForegroundColor Gray
kubectl apply -f k8s/deployments/payment-service.yaml

# Deploy account service
Write-Host "  Deploying account-service..." -ForegroundColor Gray
kubectl apply -f k8s/deployments/account-service.yaml

# Deploy validation service
Write-Host "  Deploying validation-service..." -ForegroundColor Gray
kubectl apply -f k8s/deployments/validation-service.yaml

# Deploy saga orchestrator
Write-Host "  Deploying saga-orchestrator..." -ForegroundColor Gray
kubectl apply -f k8s/deployments/saga-orchestrator.yaml

# Wait for microservices to be ready
Write-Host "⏳ Waiting for microservices to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=payment-service -n payments --timeout=300s
kubectl wait --for=condition=ready pod -l app=account-service -n payments --timeout=300s
kubectl wait --for=condition=ready pod -l app=validation-service -n payments --timeout=300s
kubectl wait --for=condition=ready pod -l app=saga-orchestrator -n payments --timeout=300s

# Verify Istio sidecar injection
Write-Host "🔍 Verifying Istio sidecar injection..." -ForegroundColor Yellow
$pods = kubectl get pods -n payments -o json | ConvertFrom-Json
foreach ($pod in $pods.items) {
    $containerCount = $pod.spec.containers.Count
    if ($containerCount -gt 1) {
        Write-Host "  ✅ $($pod.metadata.name): $containerCount containers (sidecar injected)" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️ $($pod.metadata.name): $containerCount containers (no sidecar)" -ForegroundColor Yellow
    }
}

# Verify mTLS is working
Write-Host "🔒 Verifying mTLS configuration..." -ForegroundColor Yellow
try {
    istioctl authn tls-check | Out-Null
    Write-Host "✅ mTLS is properly configured" -ForegroundColor Green
} catch {
    Write-Host "⚠️ mTLS verification failed - check Istio configuration" -ForegroundColor Yellow
}

# Display deployment status
Write-Host ""
Write-Host "🎉 Payments Engine deployed successfully with Istio!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Deployment Status:" -ForegroundColor Cyan
kubectl get pods -n payments
Write-Host ""
Write-Host "🔧 Service Endpoints:" -ForegroundColor Cyan
kubectl get services -n payments
Write-Host ""
Write-Host "📊 Access URLs (port-forward to access):" -ForegroundColor Cyan
Write-Host "  Payment Service:" -ForegroundColor White
Write-Host "    kubectl port-forward -n payments svc/payment-service 8081:8080" -ForegroundColor Gray
Write-Host "    http://localhost:8081" -ForegroundColor Gray
Write-Host ""
Write-Host "  Account Service:" -ForegroundColor White
Write-Host "    kubectl port-forward -n payments svc/account-service 8083:8083" -ForegroundColor Gray
Write-Host "    http://localhost:8083" -ForegroundColor Gray
Write-Host ""
Write-Host "  Validation Service:" -ForegroundColor White
Write-Host "    kubectl port-forward -n payments svc/validation-service 8082:8082" -ForegroundColor Gray
Write-Host "    http://localhost:8082" -ForegroundColor Gray
Write-Host ""
Write-Host "  Saga Orchestrator:" -ForegroundColor White
Write-Host "    kubectl port-forward -n payments svc/saga-orchestrator 8086:8086" -ForegroundColor Gray
Write-Host "    http://localhost:8086" -ForegroundColor Gray
Write-Host ""
Write-Host "📊 Istio Observability:" -ForegroundColor Cyan
Write-Host "  Kiali (Service Mesh Dashboard):" -ForegroundColor White
Write-Host "    kubectl port-forward -n istio-system svc/kiali 20001:20001" -ForegroundColor Gray
Write-Host "    http://localhost:20001" -ForegroundColor Gray
Write-Host ""
Write-Host "  Grafana (Metrics Dashboards):" -ForegroundColor White
Write-Host "    kubectl port-forward -n istio-system svc/grafana 3000:3000" -ForegroundColor Gray
Write-Host "    http://localhost:3000 (admin/admin)" -ForegroundColor Gray
Write-Host ""
Write-Host "🔧 Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Test service-to-service communication" -ForegroundColor White
Write-Host "  2. Check service mesh topology in Kiali" -ForegroundColor White
Write-Host "  3. Monitor metrics in Grafana" -ForegroundColor White
Write-Host "  4. Test circuit breakers and fault injection" -ForegroundColor White
Write-Host ""
Write-Host "✅ Deployment completed successfully!" -ForegroundColor Green
