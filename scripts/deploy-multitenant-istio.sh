#!/bin/bash

# Deploy Multi-Tenant Istio Configuration
# This script deploys the complete multi-tenant Istio setup

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

Deploy multi-tenant Istio configuration for Payment Engine.

OPTIONS:
    -n, --namespace NAMESPACE    Kubernetes namespace [default: payment-engine]
    -d, --dry-run               Show what would be deployed without applying
    -c, --cleanup               Cleanup existing configuration before deploying
    -h, --help                  Show this help message

EXAMPLES:
    $0                          # Deploy to default namespace
    $0 --namespace my-namespace # Deploy to custom namespace
    $0 --dry-run               # Show what would be deployed
    $0 --cleanup               # Cleanup and redeploy

EOF
}

# Default values
DRY_RUN=false
CLEANUP=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
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
            print_error "Unknown argument $1"
            show_usage
            exit 1
            ;;
    esac
done

print_info "Deploying Multi-Tenant Istio Configuration"
print_info "Namespace: $NAMESPACE"
print_info "Dry Run: $DRY_RUN"
print_info "Cleanup: $CLEANUP"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    print_warning "Namespace $NAMESPACE does not exist. Creating..."
    if [[ "$DRY_RUN" == "false" ]]; then
        kubectl create namespace "$NAMESPACE"
        print_success "Namespace $NAMESPACE created"
    else
        print_info "Would create namespace $NAMESPACE"
    fi
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

# Cleanup existing configuration if requested
if [[ "$CLEANUP" == "true" ]]; then
    print_info "Cleaning up existing configuration..."
    
    # Cleanup in reverse order
    cleanup_config "$K8S_DIR/conflict-resolution.yaml" "Conflict Resolution"
    cleanup_config "$K8S_DIR/tenant-config-operator.yaml" "Tenant Config Operator"
    cleanup_config "$K8S_DIR/multi-tenant-security-policies.yaml" "Multi-Tenant Security Policies"
    cleanup_config "$K8S_DIR/multi-tenant-destination-rules.yaml" "Multi-Tenant Destination Rules"
    cleanup_config "$K8S_DIR/multi-tenant-virtual-services.yaml" "Multi-Tenant Virtual Services"
    cleanup_config "$K8S_DIR/multi-tenant-gateway.yaml" "Multi-Tenant Gateway"
    
    # Cleanup old configuration
    cleanup_config "$K8S_DIR/security-policies.yaml" "Old Security Policies"
    cleanup_config "$K8S_DIR/destination-rules.yaml" "Old Destination Rules"
    cleanup_config "$K8S_DIR/gateway.yaml" "Old Gateway"
    
    print_success "Cleanup completed"
fi

# Deploy multi-tenant configuration
print_info "Deploying multi-tenant Istio configuration..."

# 1. Deploy multi-tenant gateway
apply_config "$K8S_DIR/multi-tenant-gateway.yaml" "Multi-Tenant Gateway"

# 2. Deploy multi-tenant virtual services
apply_config "$K8S_DIR/multi-tenant-virtual-services.yaml" "Multi-Tenant Virtual Services"

# 3. Deploy multi-tenant destination rules
apply_config "$K8S_DIR/multi-tenant-destination-rules.yaml" "Multi-Tenant Destination Rules"

# 4. Deploy multi-tenant security policies
apply_config "$K8S_DIR/multi-tenant-security-policies.yaml" "Multi-Tenant Security Policies"

# 5. Deploy tenant config operator
apply_config "$K8S_DIR/tenant-config-operator.yaml" "Tenant Config Operator"

# 6. Deploy conflict resolution
apply_config "$K8S_DIR/conflict-resolution.yaml" "Conflict Resolution"

# Verify deployment
if [[ "$DRY_RUN" == "false" ]]; then
    print_info "Verifying deployment..."
    
    # Check gateways
    print_info "Checking gateways..."
    kubectl get gateways -n "$NAMESPACE" -l app=payment-engine
    
    # Check virtual services
    print_info "Checking virtual services..."
    kubectl get virtualservices -n "$NAMESPACE" -l app=payment-engine
    
    # Check destination rules
    print_info "Checking destination rules..."
    kubectl get destinationrules -n "$NAMESPACE" -l app=payment-engine
    
    # Check security policies
    print_info "Checking security policies..."
    kubectl get peerauthentications -n "$NAMESPACE" -l app=payment-engine
    kubectl get authorizationpolicies -n "$NAMESPACE" -l app=payment-engine
    kubectl get requestauthentications -n "$NAMESPACE" -l app=payment-engine
    
    # Check deployments
    print_info "Checking deployments..."
    kubectl get deployments -n "$NAMESPACE" -l app=payment-engine
    
    print_success "Deployment verification completed"
fi

# Generate tenant configuration examples
print_info "Generating tenant configuration examples..."

# Create example tenant configurations
EXAMPLE_TENANTS=("tenant-001" "tenant-002" "tenant-003")

for tenant in "${EXAMPLE_TENANTS[@]}"; do
    print_info "Generating configuration for $tenant..."
    
    if [[ "$DRY_RUN" == "false" ]]; then
        "$SCRIPT_DIR/generate-tenant-istio-config.sh" "$tenant" --environment dev
    else
        print_info "Would generate configuration for $tenant"
    fi
done

# Display access information
print_info "Multi-tenant Istio configuration deployment completed!"
print_info ""
print_info "Access Information:"
print_info "==================="
print_info "Main Domain: https://payment-engine.local"
print_info "Tenant-001:  https://tenant-001.payment-engine.local"
print_info "Tenant-002:  https://tenant-002.payment-engine.local"
print_info "Tenant-003:  https://tenant-003.payment-engine.local"
print_info ""
print_info "Development Environment:"
print_info "Tenant-001:  https://tenant-001.dev.payment-engine.local"
print_info "Tenant-002:  https://tenant-002.dev.payment-engine.local"
print_info "Tenant-003:  https://tenant-003.dev.payment-engine.local"
print_info ""
print_info "Staging Environment:"
print_info "Tenant-001:  https://tenant-001.staging.payment-engine.local"
print_info "Tenant-002:  https://tenant-002.staging.payment-engine.local"
print_info "Tenant-003:  https://tenant-003.staging.payment-engine.local"
print_info ""
print_info "Production Environment:"
print_info "Tenant-001:  https://tenant-001.prod.payment-engine.local"
print_info "Tenant-002:  https://tenant-002.prod.payment-engine.local"
print_info "Tenant-003:  https://tenant-003.prod.payment-engine.local"
print_info ""
print_info "Next Steps:"
print_info "==========="
print_info "1. Configure DNS to point to your Istio ingress gateway"
print_info "2. Install TLS certificates for each tenant domain"
print_info "3. Test tenant isolation and routing"
print_info "4. Use the tenant config generator for new tenants:"
print_info "   ./scripts/generate-tenant-istio-config.sh <tenant-id>"
print_info ""
print_info "Useful Commands:"
print_info "==============="
print_info "kubectl get gateways -n $NAMESPACE"
print_info "kubectl get virtualservices -n $NAMESPACE"
print_info "kubectl get destinationrules -n $NAMESPACE"
print_info "kubectl get authorizationpolicies -n $NAMESPACE"
print_info "kubectl logs -n $NAMESPACE -l app=tenant-config-operator"
print_info "kubectl logs -n $NAMESPACE -l app=istio-conflict-resolution"