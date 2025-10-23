# PowerShell deployment script for React Operations Portal
param(
    [string]$ImageTag = "latest",
    [string]$Environment = "production"
)

# Configuration
$NAMESPACE = "operations-portal"

Write-Host "Starting deployment of React Operations Portal..." -ForegroundColor Green

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Check if kubectl is available
try {
    kubectl version --client | Out-Null
} catch {
    Write-Error "kubectl is not installed or not in PATH"
    exit 1
}

# Create namespace if it doesn't exist
Write-Status "Creating namespace if it doesn't exist..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply Kubernetes manifests
Write-Status "Applying Kubernetes manifests..."

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
if ($ImageTag -ne "latest") {
    Write-Status "Updating image tag to $ImageTag..."
    kubectl set image deployment/operations-portal operations-portal=operations-portal:$ImageTag -n $NAMESPACE
}

# Wait for deployment to be ready
Write-Status "Waiting for deployment to be ready..."
kubectl rollout status deployment/operations-portal -n $NAMESPACE --timeout=300s

# Check pod status
Write-Status "Checking pod status..."
kubectl get pods -n $NAMESPACE

# Check service status
Write-Status "Checking service status..."
kubectl get services -n $NAMESPACE

# Check ingress status
Write-Status "Checking ingress status..."
kubectl get ingress -n $NAMESPACE

# Run health check
Write-Status "Running health check..."
$podName = kubectl get pods -n $NAMESPACE -l app=operations-portal -o jsonpath='{.items[0].metadata.name}'
kubectl exec $podName -n $NAMESPACE -- curl -f http://localhost:8080/health

Write-Status "Deployment completed successfully!"

# Display useful information
Write-Host "Deployment Information:" -ForegroundColor Green
Write-Host "Namespace: $NAMESPACE"
Write-Host "Image Tag: $ImageTag"
Write-Host "Environment: $Environment"
Write-Host ""
Write-Host "Useful commands:"
Write-Host "  kubectl get pods -n $NAMESPACE"
Write-Host "  kubectl get services -n $NAMESPACE"
Write-Host "  kubectl get ingress -n $NAMESPACE"
Write-Host "  kubectl logs -f deployment/operations-portal -n $NAMESPACE"
Write-Host "  kubectl port-forward service/operations-portal-service 8080:80 -n $NAMESPACE"
