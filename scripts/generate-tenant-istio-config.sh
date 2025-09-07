#!/bin/bash

# Generate Istio Configuration for New Tenant
# This script creates all necessary Istio resources for a new tenant

set -e

# Configuration
NAMESPACE="payment-engine"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
K8S_DIR="$PROJECT_ROOT/k8s/istio"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS] TENANT_ID

Generate Istio configuration for a new tenant.

OPTIONS:
    -e, --environment ENV    Environment (dev, staging, prod) [default: dev]
    -d, --domain DOMAIN      Domain suffix [default: payment-engine.local]
    -p, --port PORT          Port number [default: 443]
    -h, --help               Show this help message

EXAMPLES:
    $0 tenant-001
    $0 tenant-002 --environment staging
    $0 tenant-003 --environment prod --domain example.com

EOF
}

# Default values
ENVIRONMENT="dev"
DOMAIN="payment-engine.local"
PORT="443"
TENANT_ID=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -d|--domain)
            DOMAIN="$2"
            shift 2
            ;;
        -p|--port)
            PORT="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        -*|--*)
            print_error "Unknown option $1"
            show_usage
            exit 1
            ;;
        *)
            if [[ -z "$TENANT_ID" ]]; then
                TENANT_ID="$1"
            else
                print_error "Multiple tenant IDs provided"
                show_usage
                exit 1
            fi
            shift
            ;;
    esac
done

# Validate required parameters
if [[ -z "$TENANT_ID" ]]; then
    print_error "Tenant ID is required"
    show_usage
    exit 1
fi

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|prod)$ ]]; then
    print_error "Environment must be one of: dev, staging, prod"
    exit 1
fi

# Validate tenant ID format
if [[ ! "$TENANT_ID" =~ ^[a-z0-9-]+$ ]]; then
    print_error "Tenant ID must contain only lowercase letters, numbers, and hyphens"
    exit 1
fi

print_info "Generating Istio configuration for tenant: $TENANT_ID"
print_info "Environment: $ENVIRONMENT"
print_info "Domain: $DOMAIN"
print_info "Port: $PORT"

# Create output directory
OUTPUT_DIR="$K8S_DIR/tenants/$TENANT_ID"
mkdir -p "$OUTPUT_DIR"

# Generate Gateway configuration
print_info "Generating Gateway configuration..."
cat > "$OUTPUT_DIR/gateway.yaml" << EOF
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: $TENANT_ID-gateway
  namespace: $NAMESPACE
  labels:
    app: payment-engine
    component: gateway
    tenant: $TENANT_ID
    environment: $ENVIRONMENT
    version: v1
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: $PORT
      name: https
      protocol: HTTPS
    hosts:
    - "$TENANT_ID.$ENVIRONMENT.$DOMAIN"
    tls:
      mode: SIMPLE
      credentialName: $TENANT_ID-tls
EOF

# Generate VirtualService configuration
print_info "Generating VirtualService configuration..."
cat > "$OUTPUT_DIR/virtual-service.yaml" << EOF
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: $TENANT_ID-vs
  namespace: $NAMESPACE
  labels:
    app: payment-engine
    component: virtual-service
    tenant: $TENANT_ID
    environment: $ENVIRONMENT
    version: v1
spec:
  hosts:
  - "$TENANT_ID.$ENVIRONMENT.$DOMAIN"
  gateways:
  - $TENANT_ID-gateway
  http:
  # Authentication service
  - match:
    - uri:
        prefix: /api/v1/auth
    route:
    - destination:
        host: auth-service
        port:
          number: 8080
    headers:
      request:
        set:
          X-Tenant-ID: "$TENANT_ID"
  # Configuration service
  - match:
    - uri:
        prefix: /api/v1/config
    route:
    - destination:
        host: config-service
        port:
          number: 8080
    headers:
      request:
        set:
          X-Tenant-ID: "$TENANT_ID"
  # Payment processing service
  - match:
    - uri:
        prefix: /api/v1/iso20022
    route:
    - destination:
        host: payment-processing-service
        port:
          number: 8080
    headers:
      request:
        set:
          X-Tenant-ID: "$TENANT_ID"
  # Core banking service
  - match:
    - uri:
        prefix: /api/v1/banking
    route:
    - destination:
        host: core-banking-service
        port:
          number: 8080
    headers:
      request:
        set:
          X-Tenant-ID: "$TENANT_ID"
  # Tenant auth configuration
  - match:
    - uri:
        prefix: /api/v1/tenant-auth-config
    route:
    - destination:
        host: payment-processing-service
        port:
          number: 8080
    headers:
      request:
        set:
          X-Tenant-ID: "$TENANT_ID"
  # Frontend
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: frontend-service
        port:
          number: 3000
    headers:
      request:
        set:
          X-Tenant-ID: "$TENANT_ID"
EOF

# Generate PeerAuthentication configuration
print_info "Generating PeerAuthentication configuration..."
cat > "$OUTPUT_DIR/peer-authentication.yaml" << EOF
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: $TENANT_ID-peer-auth
  namespace: $NAMESPACE
  labels:
    app: payment-engine
    component: security
    tenant: $TENANT_ID
    environment: $ENVIRONMENT
    version: v1
spec:
  selector:
    matchLabels:
      tenant: $TENANT_ID
  mtls:
    mode: STRICT
EOF

# Generate AuthorizationPolicy configuration
print_info "Generating AuthorizationPolicy configuration..."
cat > "$OUTPUT_DIR/authorization-policy.yaml" << EOF
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: $TENANT_ID-authz
  namespace: $NAMESPACE
  labels:
    app: payment-engine
    component: security
    tenant: $TENANT_ID
    environment: $ENVIRONMENT
    version: v1
spec:
  selector:
    matchLabels:
      tenant: $TENANT_ID
  rules:
  # Allow ingress gateway to access tenant services
  - from:
    - source:
        principals: ["cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"]
    to:
    - operation:
        methods: ["GET", "POST", "PUT", "DELETE", "PATCH"]
        paths: ["/api/v1/*"]
    when:
    - key: request.headers[x-tenant-id]
      values: ["$TENANT_ID"]
  # Allow inter-service communication within tenant
  - from:
    - source:
        principals: 
        - "cluster.local/ns/$NAMESPACE/sa/auth-service"
        - "cluster.local/ns/$NAMESPACE/sa/config-service"
        - "cluster.local/ns/$NAMESPACE/sa/payment-processing-service"
        - "cluster.local/ns/$NAMESPACE/sa/core-banking-service"
        - "cluster.local/ns/$NAMESPACE/sa/api-gateway-service"
    to:
    - operation:
        methods: ["GET", "POST", "PUT", "DELETE", "PATCH"]
        paths: ["/api/v1/*"]
    when:
    - key: request.headers[x-tenant-id]
      values: ["$TENANT_ID"]
EOF

# Generate TLS Secret template
print_info "Generating TLS Secret template..."
cat > "$OUTPUT_DIR/tls-secret-template.yaml" << EOF
apiVersion: v1
kind: Secret
metadata:
  name: $TENANT_ID-tls
  namespace: $NAMESPACE
  labels:
    app: payment-engine
    component: tls
    tenant: $TENANT_ID
    environment: $ENVIRONMENT
    version: v1
type: kubernetes.io/tls
data:
  # Base64 encoded certificate and key
  # Replace these with actual certificate and key
  tls.crt: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t...
  tls.key: LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0t...
stringData:
  # Alternative: use stringData for plain text (not recommended for production)
  # tls.crt: |
  #   -----BEGIN CERTIFICATE-----
  #   ...
  #   -----END CERTIFICATE-----
  # tls.key: |
  #   -----BEGIN PRIVATE KEY-----
  #   ...
  #   -----END PRIVATE KEY-----
EOF

# Generate kustomization file
print_info "Generating kustomization file..."
cat > "$OUTPUT_DIR/kustomization.yaml" << EOF
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: $NAMESPACE

resources:
- gateway.yaml
- virtual-service.yaml
- peer-authentication.yaml
- authorization-policy.yaml
- tls-secret-template.yaml

commonLabels:
  app: payment-engine
  tenant: $TENANT_ID
  environment: $ENVIRONMENT
  version: v1

patchesStrategicMerge:
- |-
  apiVersion: networking.istio.io/v1beta1
  kind: Gateway
  metadata:
    name: $TENANT_ID-gateway
  spec:
    servers:
    - hosts:
      - "$TENANT_ID.$ENVIRONMENT.$DOMAIN"
EOF

# Generate deployment script
print_info "Generating deployment script..."
cat > "$OUTPUT_DIR/deploy.sh" << 'EOF'
#!/bin/bash

# Deploy Istio configuration for tenant
set -e

TENANT_ID="$1"
NAMESPACE="payment-engine"

if [[ -z "$TENANT_ID" ]]; then
    echo "Usage: $0 TENANT_ID"
    exit 1
fi

echo "Deploying Istio configuration for tenant: $TENANT_ID"

# Apply configurations
kubectl apply -k .

# Verify deployment
echo "Verifying deployment..."
kubectl get gateway "$TENANT_ID-gateway" -n "$NAMESPACE"
kubectl get virtualservice "$TENANT_ID-vs" -n "$NAMESPACE"
kubectl get peerauthentication "$TENANT_ID-peer-auth" -n "$NAMESPACE"
kubectl get authorizationpolicy "$TENANT_ID-authz" -n "$NAMESPACE"

echo "Deployment completed successfully!"
EOF

chmod +x "$OUTPUT_DIR/deploy.sh"

# Generate cleanup script
print_info "Generating cleanup script..."
cat > "$OUTPUT_DIR/cleanup.sh" << 'EOF'
#!/bin/bash

# Cleanup Istio configuration for tenant
set -e

TENANT_ID="$1"
NAMESPACE="payment-engine"

if [[ -z "$TENANT_ID" ]]; then
    echo "Usage: $0 TENANT_ID"
    exit 1
fi

echo "Cleaning up Istio configuration for tenant: $TENANT_ID"

# Delete configurations
kubectl delete -k . --ignore-not-found=true

echo "Cleanup completed successfully!"
EOF

chmod +x "$OUTPUT_DIR/cleanup.sh"

# Generate README
print_info "Generating README..."
cat > "$OUTPUT_DIR/README.md" << EOF
# Istio Configuration for Tenant: $TENANT_ID

This directory contains the Istio configuration for tenant \`$TENANT_ID\` in the \`$ENVIRONMENT\` environment.

## Files

- \`gateway.yaml\` - Istio Gateway configuration
- \`virtual-service.yaml\` - VirtualService configuration for routing
- \`peer-authentication.yaml\` - PeerAuthentication for mTLS
- \`authorization-policy.yaml\` - AuthorizationPolicy for access control
- \`tls-secret-template.yaml\` - TLS Secret template (needs actual certificates)
- \`kustomization.yaml\` - Kustomize configuration
- \`deploy.sh\` - Deployment script
- \`cleanup.sh\` - Cleanup script

## Deployment

1. **Update TLS Secret**: Replace the placeholder certificate and key in \`tls-secret-template.yaml\` with actual certificates.

2. **Deploy Configuration**:
   \`\`\`bash
   ./deploy.sh $TENANT_ID
   \`\`\`

3. **Verify Deployment**:
   \`\`\`bash
   kubectl get gateway $TENANT_ID-gateway -n $NAMESPACE
   kubectl get virtualservice $TENANT_ID-vs -n $NAMESPACE
   \`\`\`

## Access

The tenant will be accessible at: \`https://$TENANT_ID.$ENVIRONMENT.$DOMAIN\`

## Cleanup

To remove the configuration:
\`\`\`bash
./cleanup.sh $TENANT_ID
\`\`\`

## Configuration Details

- **Tenant ID**: $TENANT_ID
- **Environment**: $ENVIRONMENT
- **Domain**: $DOMAIN
- **Port**: $PORT
- **Namespace**: $NAMESPACE

## Security

- mTLS is enforced (STRICT mode)
- Authorization policies restrict access to tenant-specific resources
- Tenant ID is automatically injected into request headers
EOF

print_success "Istio configuration generated successfully!"
print_info "Output directory: $OUTPUT_DIR"
print_info ""
print_info "Next steps:"
print_info "1. Update the TLS certificate in tls-secret-template.yaml"
print_info "2. Deploy the configuration: cd $OUTPUT_DIR && ./deploy.sh $TENANT_ID"
print_info "3. Verify the deployment with kubectl commands"
print_info ""
print_info "Tenant will be accessible at: https://$TENANT_ID.$ENVIRONMENT.$DOMAIN"