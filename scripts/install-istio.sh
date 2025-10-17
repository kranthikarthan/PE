#!/bin/bash

# Install Istio Service Mesh for Payments Engine
# This script installs Istio on AKS and configures it for the payments namespace

set -e

echo "🚀 Installing Istio Service Mesh for Payments Engine..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed or not in PATH"
    exit 1
fi

# Check if AKS cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to AKS cluster. Please check your kubeconfig."
    exit 1
fi

echo "✅ AKS cluster is accessible"

# Download Istio
echo "📥 Downloading Istio..."
ISTIO_VERSION="1.19.0"
curl -L https://istio.io/downloadIstio | ISTIO_VERSION=$ISTIO_VERSION sh -
cd istio-$ISTIO_VERSION

# Add Istio to PATH
export PATH=$PWD/bin:$PATH

# Install Istio with demo profile (includes all components)
echo "🔧 Installing Istio control plane..."
istioctl install --set values.defaultRevision=default -y

# Wait for Istio to be ready
echo "⏳ Waiting for Istio to be ready..."
kubectl wait --for=condition=ready pod -l app=istiod -n istio-system --timeout=300s

# Create payments namespace with Istio injection enabled
echo "📦 Creating payments namespace with Istio injection..."
kubectl apply -f ../k8s/istio/namespace.yaml

# Wait for namespace to be ready
kubectl wait --for=condition=ready namespace payments --timeout=60s

# Apply Istio configurations
echo "⚙️ Applying Istio configurations..."

# Enable mTLS
kubectl apply -f ../k8s/istio/peer-authentication.yaml

# Apply destination rules (circuit breakers)
kubectl apply -f ../k8s/istio/destination-rules.yaml

# Apply virtual services (traffic routing)
kubectl apply -f ../k8s/istio/virtual-services.yaml

# Apply authorization policies
kubectl apply -f ../k8s/istio/authorization-policies.yaml

# Apply tenant routing (optional)
kubectl apply -f ../k8s/istio/tenant-routing.yaml

# Install Kiali for observability
echo "📊 Installing Kiali for service mesh observability..."
kubectl apply -f samples/addons/kiali.yaml

# Install Prometheus for metrics
echo "📈 Installing Prometheus for metrics collection..."
kubectl apply -f samples/addons/prometheus.yaml

# Install Grafana for dashboards
echo "📊 Installing Grafana for dashboards..."
kubectl apply -f samples/addons/grafana.yaml

# Install Jaeger for distributed tracing
echo "🔍 Installing Jaeger for distributed tracing..."
kubectl apply -f samples/addons/jaeger.yaml

# Wait for addons to be ready
echo "⏳ Waiting for observability addons to be ready..."
kubectl wait --for=condition=ready pod -l app=kiali -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=prometheus -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=grafana -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=jaeger -n istio-system --timeout=300s

# Verify Istio installation
echo "🔍 Verifying Istio installation..."
istioctl verify-install

# Display access information
echo ""
echo "🎉 Istio Service Mesh installation completed successfully!"
echo ""
echo "📊 Access URLs (port-forward to access):"
echo "  Kiali (Service Mesh Dashboard):"
echo "    kubectl port-forward -n istio-system svc/kiali 20001:20001"
echo "    http://localhost:20001"
echo ""
echo "  Grafana (Metrics Dashboards):"
echo "    kubectl port-forward -n istio-system svc/grafana 3000:3000"
echo "    http://localhost:3000 (admin/admin)"
echo ""
echo "  Prometheus (Metrics):"
echo "    kubectl port-forward -n istio-system svc/prometheus 9090:9090"
echo "    http://localhost:9090"
echo ""
echo "  Jaeger (Distributed Tracing):"
echo "    kubectl port-forward -n istio-system svc/tracing 16686:80"
echo "    http://localhost:16686"
echo ""
echo "🔧 Next Steps:"
echo "  1. Deploy your microservices to the 'payments' namespace"
echo "  2. Verify mTLS is working: istioctl authn tls-check"
echo "  3. Check service mesh topology in Kiali"
echo "  4. Monitor metrics in Grafana"
echo ""
echo "📚 Documentation:"
echo "  - Istio Docs: https://istio.io/latest/docs/"
echo "  - Kiali Docs: https://kiali.io/documentation/"
echo ""

# Clean up
cd ..
rm -rf istio-$ISTIO_VERSION

echo "✅ Istio installation completed!"
