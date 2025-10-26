#!/bin/bash

# Deployment script for React Operations Portal
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="operations-portal"
IMAGE_TAG=${1:-latest}
ENVIRONMENT=${2:-production}

echo -e "${GREEN}Starting deployment of React Operations Portal...${NC}"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if kubeconfig is set
if [ -z "$KUBECONFIG" ] && [ ! -f ~/.kube/config ]; then
    print_error "kubeconfig is not set"
    exit 1
fi

# Create namespace if it doesn't exist
print_status "Creating namespace if it doesn't exist..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply Kubernetes manifests
print_status "Applying Kubernetes manifests..."

# Apply in order
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
kubectl apply -f k8s/networkpolicy.yaml
kubectl apply -f k8s/pdb.yaml

# Update image tag if provided
if [ "$IMAGE_TAG" != "latest" ]; then
    print_status "Updating image tag to $IMAGE_TAG..."
    kubectl set image deployment/operations-portal operations-portal=operations-portal:$IMAGE_TAG -n $NAMESPACE
fi

# Wait for deployment to be ready
print_status "Waiting for deployment to be ready..."
kubectl rollout status deployment/operations-portal -n $NAMESPACE --timeout=300s

# Check pod status
print_status "Checking pod status..."
kubectl get pods -n $NAMESPACE

# Check service status
print_status "Checking service status..."
kubectl get services -n $NAMESPACE

# Check ingress status
print_status "Checking ingress status..."
kubectl get ingress -n $NAMESPACE

# Run health check
print_status "Running health check..."
kubectl get pods -n $NAMESPACE -l app=operations-portal -o jsonpath='{.items[0].metadata.name}' | xargs -I {} kubectl exec {} -n $NAMESPACE -- curl -f http://localhost:8080/health

print_status "Deployment completed successfully!"

# Display useful information
echo -e "${GREEN}Deployment Information:${NC}"
echo "Namespace: $NAMESPACE"
echo "Image Tag: $IMAGE_TAG"
echo "Environment: $ENVIRONMENT"
echo ""
echo "Useful commands:"
echo "  kubectl get pods -n $NAMESPACE"
echo "  kubectl get services -n $NAMESPACE"
echo "  kubectl get ingress -n $NAMESPACE"
echo "  kubectl logs -f deployment/operations-portal -n $NAMESPACE"
echo "  kubectl port-forward service/operations-portal-service 8080:80 -n $NAMESPACE"
