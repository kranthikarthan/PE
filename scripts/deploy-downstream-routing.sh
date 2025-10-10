#!/bin/bash

# Deploy Downstream Routing Solution
# This script deploys the solution for same host/port routing conflicts

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
K8S_DIR="$PROJECT_ROOT/k8s/istio"
NAMESPACE="payment-engine"

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
Usage: $0 [OPTIONS]

Deploy downstream routing solution for same host/port conflicts.

OPTIONS:
    -n, --namespace NAMESPACE    Kubernetes namespace [default: payment-engine]
    -h, --host HOST              Bank's NGINX host [default: bank-nginx.example.com]
    -p, --port PORT              Bank's NGINX port [default: 443]
    -d, --dry-run               Show what would be deployed without applying
    -c, --cleanup               Cleanup existing configuration before deploying
    -h, --help                  Show this help message

EXAMPLES:
    $0                                    # Deploy with defaults
    $0 --host my-bank.com --port 443     # Deploy with custom host/port
    $0 --dry-run                         # Show what would be deployed
    $0 --cleanup                         # Cleanup and redeploy

EOF
}

# Default values
BANK_HOST="bank-nginx.example.com"
BANK_PORT="443"
DRY_RUN=false
CLEANUP=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -h|--host)
            BANK_HOST="$2"
            shift 2
            ;;
        -p|--port)
            BANK_PORT="$2"
            shift 2
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -c|--cleanup)
            CLEANUP=true
            shift
            ;;
        --help)
            show_usage
            exit 0
            ;;
        -*|--*)
            print_error "Unknown option $1"
            show_usage
            exit 1
            ;;
        *)
            print_error "Unknown argument $1"
            show_usage
            exit 1
            ;;
    esac
done

print_info "Deploying Downstream Routing Solution"
print_info "Namespace: $NAMESPACE"
print_info "Bank Host: $BANK_HOST"
print_info "Bank Port: $BANK_PORT"
print_info "Dry Run: $DRY_RUN"
print_info "Cleanup: $CLEANUP"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Function to apply configuration
apply_config() {
    local config_file="$1"
    local description="$2"
    
    print_info "Applying $description..."
    
    if [[ "$DRY_RUN" == "true" ]]; then
        print_info "Would apply: $config_file"
        kubectl apply -f "$config_file" --dry-run=client
    else
        kubectl apply -f "$config_file"
        print_success "$description applied successfully"
    fi
}

# Function to cleanup configuration
cleanup_config() {
    local config_file="$1"
    local description="$2"
    
    print_info "Cleaning up $description..."
    
    if [[ "$DRY_RUN" == "true" ]]; then
        print_info "Would delete: $config_file"
        kubectl delete -f "$config_file" --dry-run=client --ignore-not-found=true
    else
        kubectl delete -f "$config_file" --ignore-not-found=true
        print_success "$description cleaned up successfully"
    fi
}

# Function to update configuration with custom values
update_config() {
    local config_file="$1"
    local temp_file="/tmp/$(basename "$config_file")"
    
    if [[ "$DRY_RUN" == "false" ]]; then
        # Replace placeholders with actual values
        sed "s/bank-nginx.example.com/$BANK_HOST/g" "$config_file" > "$temp_file"
        sed -i "s/443/$BANK_PORT/g" "$temp_file"
        echo "$temp_file"
    else
        echo "$config_file"
    fi
}

# Cleanup existing configuration if requested
if [[ "$CLEANUP" == "true" ]]; then
    print_info "Cleaning up existing downstream routing configuration..."
    
    # Cleanup in reverse order
    cleanup_config "$K8S_DIR/downstream-envoy-filter.yaml" "Downstream EnvoyFilter"
    cleanup_config "$K8S_DIR/same-host-port-routing.yaml" "Same Host/Port Routing"
    cleanup_config "$K8S_DIR/downstream-routing-solution.yaml" "Downstream Routing Solution"
    
    print_success "Cleanup completed"
fi

# Deploy downstream routing configuration
print_info "Deploying downstream routing solution..."

# 1. Deploy ServiceEntries and basic routing
config_file=$(update_config "$K8S_DIR/downstream-routing-solution.yaml")
apply_config "$config_file" "Downstream Routing Solution"

# 2. Deploy same host/port routing
config_file=$(update_config "$K8S_DIR/same-host-port-routing.yaml")
apply_config "$config_file" "Same Host/Port Routing"

# 3. Deploy EnvoyFilter for advanced routing
apply_config "$K8S_DIR/downstream-envoy-filter.yaml" "Downstream EnvoyFilter"

# Clean up temporary files
if [[ "$DRY_RUN" == "false" ]]; then
    rm -f /tmp/downstream-*.yaml
fi

# Verify deployment
if [[ "$DRY_RUN" == "false" ]]; then
    print_info "Verifying deployment..."
    
    # Check ServiceEntries
    print_info "Checking ServiceEntries..."
    kubectl get serviceentries -n "$NAMESPACE" -l app=payment-engine
    
    # Check VirtualServices
    print_info "Checking VirtualServices..."
    kubectl get virtualservices -n "$NAMESPACE" -l routing-type=same-host-port
    
    # Check DestinationRules
    print_info "Checking DestinationRules..."
    kubectl get destinationrules -n "$NAMESPACE" -l external-service=bank-nginx
    
    # Check EnvoyFilters
    print_info "Checking EnvoyFilters..."
    kubectl get envoyfilters -n "$NAMESPACE" -l app=payment-engine
    
    # Check AuthorizationPolicies
    print_info "Checking AuthorizationPolicies..."
    kubectl get authorizationpolicies -n "$NAMESPACE" -l routing-type=downstream
    
    print_success "Deployment verification completed"
fi

# Display configuration information
print_info "Downstream routing solution deployment completed!"
print_info ""
print_info "Configuration Summary:"
print_info "====================="
print_info "Bank Host: $BANK_HOST"
print_info "Bank Port: $BANK_PORT"
print_info "Namespace: $NAMESPACE"
print_info ""
print_info "Routing Rules:"
print_info "=============="
print_info "Tenant-001 Fraud:    X-Tenant-ID=tenant-001, X-Service-Type=fraud"
print_info "Tenant-001 Clearing: X-Tenant-ID=tenant-001, X-Service-Type=clearing"
print_info "Tenant-002 Fraud:    X-Tenant-ID=tenant-002, X-Service-Type=fraud"
print_info "Tenant-002 Clearing: X-Tenant-ID=tenant-002, X-Service-Type=clearing"
print_info "Tenant-003 Fraud:    X-Tenant-ID=tenant-003, X-Service-Type=fraud"
print_info "Tenant-003 Clearing: X-Tenant-ID=tenant-003, X-Service-Type=clearing"
print_info ""
print_info "API Endpoints:"
print_info "=============="
print_info "Fraud System:    POST /api/v1/downstream/fraud/{tenantId}"
print_info "Clearing System: POST /api/v1/downstream/clearing/{tenantId}"
print_info "Auto Routing:    POST /api/v1/downstream/auto/{tenantId}"
print_info "Specific Service: POST /api/v1/downstream/service/{tenantId}/{serviceType}"
print_info ""
print_info "Example Usage:"
print_info "=============="
print_info "curl -X POST https://payment-engine.local/api/v1/downstream/fraud/tenant-001 \\"
print_info "  -H 'Content-Type: application/json' \\"
print_info "  -H 'X-Tenant-ID: tenant-001' \\"
print_info "  -H 'X-Service-Type: fraud' \\"
print_info "  -d '{\"transaction_id\": \"123\", \"amount\": 1000}'"
print_info ""
print_info "Next Steps:"
print_info "==========="
print_info "1. Update bank-nginx.example.com to your actual bank's NGINX host"
print_info "2. Configure TLS certificates for external communication"
print_info "3. Test routing with different tenant IDs and service types"
print_info "4. Monitor logs for routing decisions"
print_info ""
print_info "Useful Commands:"
print_info "==============="
print_info "kubectl get serviceentries -n $NAMESPACE"
print_info "kubectl get virtualservices -n $NAMESPACE -l routing-type=same-host-port"
print_info "kubectl get destinationrules -n $NAMESPACE -l external-service=bank-nginx"
print_info "kubectl get envoyfilters -n $NAMESPACE -l app=payment-engine"
print_info "kubectl logs -n $NAMESPACE -l app=payment-processing-service | grep downstream"