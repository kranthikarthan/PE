# Install Istio Service Mesh for Payments Engine
# This script installs Istio on AKS and configures it for the payments namespace

param(
    [switch]$SkipDownload = $false
)

Write-Host "🚀 Installing Istio Service Mesh for Payments Engine..." -ForegroundColor Green

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

# Download Istio if not skipped
if (-not $SkipDownload) {
    Write-Host "📥 Downloading Istio..." -ForegroundColor Yellow
    $ISTIO_VERSION = "1.19.0"
    
    # Download Istio
    $downloadUrl = "https://github.com/istio/istio/releases/download/$ISTIO_VERSION/istio-$ISTIO_VERSION-win.zip"
    $zipFile = "istio-$ISTIO_VERSION-win.zip"
    
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipFile
    
    # Extract Istio
    Expand-Archive -Path $zipFile -DestinationPath "." -Force
    Remove-Item $zipFile
    
    # Add Istio to PATH for this session
    $env:PATH = "$PWD\istio-$ISTIO_VERSION\bin;$env:PATH"
} else {
    Write-Host "⏭️ Skipping Istio download (assuming already installed)" -ForegroundColor Yellow
}

# Install Istio with demo profile (includes all components)
Write-Host "🔧 Installing Istio control plane..." -ForegroundColor Yellow
istioctl install --set values.defaultRevision=default -y

# Wait for Istio to be ready
Write-Host "⏳ Waiting for Istio to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=istiod -n istio-system --timeout=300s

# Create payments namespace with Istio injection enabled
Write-Host "📦 Creating payments namespace with Istio injection..." -ForegroundColor Yellow
kubectl apply -f k8s/istio/namespace.yaml

# Wait for namespace to be ready
kubectl wait --for=condition=ready namespace payments --timeout=60s

# Apply Istio configurations
Write-Host "⚙️ Applying Istio configurations..." -ForegroundColor Yellow

# Enable mTLS
kubectl apply -f k8s/istio/peer-authentication.yaml

# Apply destination rules (circuit breakers)
kubectl apply -f k8s/istio/destination-rules.yaml

# Apply virtual services (traffic routing)
kubectl apply -f k8s/istio/virtual-services.yaml

# Apply authorization policies
kubectl apply -f k8s/istio/authorization-policies.yaml

# Apply tenant routing (optional)
kubectl apply -f k8s/istio/tenant-routing.yaml

# Install Kiali for observability
Write-Host "📊 Installing Kiali for service mesh observability..." -ForegroundColor Yellow
kubectl apply -f istio-$ISTIO_VERSION/samples/addons/kiali.yaml

# Install Prometheus for metrics
Write-Host "📈 Installing Prometheus for metrics collection..." -ForegroundColor Yellow
kubectl apply -f istio-$ISTIO_VERSION/samples/addons/prometheus.yaml

# Install Grafana for dashboards
Write-Host "📊 Installing Grafana for dashboards..." -ForegroundColor Yellow
kubectl apply -f istio-$ISTIO_VERSION/samples/addons/grafana.yaml

# Install Jaeger for distributed tracing
Write-Host "🔍 Installing Jaeger for distributed tracing..." -ForegroundColor Yellow
kubectl apply -f istio-$ISTIO_VERSION/samples/addons/jaeger.yaml

# Wait for addons to be ready
Write-Host "⏳ Waiting for observability addons to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=kiali -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=prometheus -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=grafana -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=jaeger -n istio-system --timeout=300s

# Verify Istio installation
Write-Host "🔍 Verifying Istio installation..." -ForegroundColor Yellow
istioctl verify-install

# Display access information
Write-Host ""
Write-Host "🎉 Istio Service Mesh installation completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Access URLs (port-forward to access):" -ForegroundColor Cyan
Write-Host "  Kiali (Service Mesh Dashboard):" -ForegroundColor White
Write-Host "    kubectl port-forward -n istio-system svc/kiali 20001:20001" -ForegroundColor Gray
Write-Host "    http://localhost:20001" -ForegroundColor Gray
Write-Host ""
Write-Host "  Grafana (Metrics Dashboards):" -ForegroundColor White
Write-Host "    kubectl port-forward -n istio-system svc/grafana 3000:3000" -ForegroundColor Gray
Write-Host "    http://localhost:3000 (admin/admin)" -ForegroundColor Gray
Write-Host ""
Write-Host "  Prometheus (Metrics):" -ForegroundColor White
Write-Host "    kubectl port-forward -n istio-system svc/prometheus 9090:9090" -ForegroundColor Gray
Write-Host "    http://localhost:9090" -ForegroundColor Gray
Write-Host ""
Write-Host "  Jaeger (Distributed Tracing):" -ForegroundColor White
Write-Host "    kubectl port-forward -n istio-system svc/tracing 16686:80" -ForegroundColor Gray
Write-Host "    http://localhost:16686" -ForegroundColor Gray
Write-Host ""
Write-Host "🔧 Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Deploy your microservices to the 'payments' namespace" -ForegroundColor White
Write-Host "  2. Verify mTLS is working: istioctl authn tls-check" -ForegroundColor White
Write-Host "  3. Check service mesh topology in Kiali" -ForegroundColor White
Write-Host "  4. Monitor metrics in Grafana" -ForegroundColor White
Write-Host ""
Write-Host "📚 Documentation:" -ForegroundColor Cyan
Write-Host "  - Istio Docs: https://istio.io/latest/docs/" -ForegroundColor White
Write-Host "  - Kiali Docs: https://kiali.io/documentation/" -ForegroundColor White
Write-Host ""

# Clean up
if (-not $SkipDownload) {
    Remove-Item -Recurse -Force "istio-$ISTIO_VERSION"
}

Write-Host "✅ Istio installation completed!" -ForegroundColor Green
