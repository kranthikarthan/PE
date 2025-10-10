#!/bin/bash

# Test Downstream Routing Solution
# This script tests the same host/port routing solution

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
NAMESPACE="payment-engine"
PAYMENT_ENGINE_URL="http://localhost:8080"

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

Test downstream routing solution for same host/port conflicts.

OPTIONS:
    -n, --namespace NAMESPACE    Kubernetes namespace [default: payment-engine]
    -u, --url URL               Payment Engine URL [default: http://localhost:8080]
    -t, --test-tenant TENANT    Test specific tenant only
    -h, --help                  Show this help message

EXAMPLES:
    $0                                    # Test all tenants
    $0 --url https://payment-engine.local # Test with specific URL
    $0 --test-tenant tenant-001           # Test specific tenant

EOF
}

# Default values
TEST_TENANT=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -u|--url)
            PAYMENT_ENGINE_URL="$2"
            shift 2
            ;;
        -t|--test-tenant)
            TEST_TENANT="$2"
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
            print_error "Unknown argument $1"
            show_usage
            exit 1
            ;;
    esac
done

print_info "Testing Downstream Routing Solution"
print_info "Namespace: $NAMESPACE"
print_info "Payment Engine URL: $PAYMENT_ENGINE_URL"
print_info "Test Tenant: $TEST_TENANT"

# Check if curl is available
if ! command -v curl &> /dev/null; then
    print_error "curl is not installed or not in PATH"
    exit 1
fi

# Function to test downstream routing
test_downstream_routing() {
    local tenant="$1"
    local service_type="$2"
    local endpoint="$3"
    local description="$4"
    
    print_info "Testing $description for tenant: $tenant"
    
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream/$endpoint/$tenant"
    local request_body='{"transaction_id": "test-123", "amount": 1000, "currency": "USD"}'
    
    # Test the endpoint
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: $tenant" \
        -H "X-Service-Type: $service_type" \
        -d "$request_body" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" || "$response" == "201" ]]; then
        print_success "$description working for $tenant (HTTP $response)"
    elif [[ "$response" == "403" ]]; then
        print_warning "$description access denied for $tenant (HTTP $response) - check permissions"
    elif [[ "$response" == "404" ]]; then
        print_warning "$description not found for $tenant (HTTP $response) - check configuration"
    else
        print_warning "$description returned $response for $tenant"
    fi
}

# Function to test auto-routing
test_auto_routing() {
    local tenant="$1"
    local request_body="$2"
    local description="$3"
    
    print_info "Testing $description for tenant: $tenant"
    
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream/auto/$tenant"
    
    # Test the endpoint
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: $tenant" \
        -d "$request_body" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" || "$response" == "201" ]]; then
        print_success "$description working for $tenant (HTTP $response)"
    elif [[ "$response" == "403" ]]; then
        print_warning "$description access denied for $tenant (HTTP $response) - check permissions"
    elif [[ "$response" == "404" ]]; then
        print_warning "$description not found for $tenant (HTTP $response) - check configuration"
    else
        print_warning "$description returned $response for $tenant"
    fi
}

# Function to test tenant configuration
test_tenant_config() {
    local tenant="$1"
    
    print_info "Testing tenant configuration for: $tenant"
    
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream/config/$tenant"
    
    # Test the endpoint
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        -H "X-Tenant-ID: $tenant" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Tenant configuration accessible for $tenant (HTTP $response)"
    else
        print_warning "Tenant configuration returned $response for $tenant"
    fi
}

# Function to test tenant statistics
test_tenant_stats() {
    local tenant="$1"
    
    print_info "Testing tenant statistics for: $tenant"
    
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream/stats/$tenant"
    
    # Test the endpoint
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        -H "X-Tenant-ID: $tenant" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Tenant statistics accessible for $tenant (HTTP $response)"
    else
        print_warning "Tenant statistics returned $response for $tenant"
    fi
}

# Function to check Istio resources
check_istio_resources() {
    print_info "Checking Istio resources..."
    
    # Check ServiceEntries
    print_info "Checking ServiceEntries..."
    local serviceentries=$(kubectl get serviceentries -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    if [[ -n "$serviceentries" ]]; then
        print_success "Found ServiceEntries: $(echo $serviceentries | wc -w)"
        kubectl get serviceentries -n "$NAMESPACE"
    else
        print_error "No ServiceEntries found in namespace $NAMESPACE"
    fi
    
    # Check VirtualServices
    print_info "Checking VirtualServices..."
    local virtualservices=$(kubectl get virtualservices -n "$NAMESPACE" -l routing-type=same-host-port -o name 2>/dev/null || echo "")
    if [[ -n "$virtualservices" ]]; then
        print_success "Found VirtualServices: $(echo $virtualservices | wc -w)"
        kubectl get virtualservices -n "$NAMESPACE" -l routing-type=same-host-port
    else
        print_warning "No same-host-port VirtualServices found in namespace $NAMESPACE"
    fi
    
    # Check DestinationRules
    print_info "Checking DestinationRules..."
    local destinationrules=$(kubectl get destinationrules -n "$NAMESPACE" -l external-service=bank-nginx -o name 2>/dev/null || echo "")
    if [[ -n "$destinationrules" ]]; then
        print_success "Found DestinationRules: $(echo $destinationrules | wc -w)"
        kubectl get destinationrules -n "$NAMESPACE" -l external-service=bank-nginx
    else
        print_warning "No bank-nginx DestinationRules found in namespace $NAMESPACE"
    fi
    
    # Check EnvoyFilters
    print_info "Checking EnvoyFilters..."
    local envoyfilters=$(kubectl get envoyfilters -n "$NAMESPACE" -l app=payment-engine -o name 2>/dev/null || echo "")
    if [[ -n "$envoyfilters" ]]; then
        print_success "Found EnvoyFilters: $(echo $envoyfilters | wc -w)"
        kubectl get envoyfilters -n "$NAMESPACE" -l app=payment-engine
    else
        print_warning "No EnvoyFilters found in namespace $NAMESPACE"
    fi
}

# Function to test health endpoint
test_health_endpoint() {
    print_info "Testing health endpoint..."
    
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream/health"
    
    # Test the endpoint
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Health endpoint working (HTTP $response)"
    else
        print_warning "Health endpoint returned $response"
    fi
}

# Main test execution
main() {
    print_info "Starting downstream routing tests..."
    
    # Test health endpoint
    test_health_endpoint
    echo ""
    
    # Check Istio resources
    check_istio_resources
    echo ""
    
    # Define test tenants
    local tenants=("tenant-001" "tenant-002" "tenant-003")
    
    # Test specific tenant if specified
    if [[ -n "$TEST_TENANT" ]]; then
        tenants=("$TEST_TENANT")
    fi
    
    # Test downstream routing for each tenant
    for tenant in "${tenants[@]}"; do
        print_info "Testing downstream routing for tenant: $tenant"
        
        # Test fraud system routing
        test_downstream_routing "$tenant" "fraud" "fraud" "Fraud System Routing"
        
        # Test clearing system routing
        test_downstream_routing "$tenant" "clearing" "clearing" "Clearing System Routing"
        
        # Test auto-routing with fraud content
        test_auto_routing "$tenant" '{"fraud_check": true, "risk_score": 85}' "Auto-Routing (Fraud)"
        
        # Test auto-routing with clearing content
        test_auto_routing "$tenant" '{"clearing_reference": "CLR123", "settlement_date": "2024-01-01"}' "Auto-Routing (Clearing)"
        
        # Test specific service routing
        test_downstream_routing "$tenant" "fraud" "service/fraud" "Specific Service Routing (Fraud)"
        test_downstream_routing "$tenant" "clearing" "service/clearing" "Specific Service Routing (Clearing)"
        
        # Test tenant configuration
        test_tenant_config "$tenant"
        
        # Test tenant statistics
        test_tenant_stats "$tenant"
        
        echo ""
    done
    
    # Test cross-tenant isolation
    print_info "Testing cross-tenant isolation..."
    
    # Test that tenant-001 cannot access tenant-002's resources
    local isolation_response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: tenant-001" \
        -H "X-Service-Type: fraud" \
        -d '{"transaction_id": "test-123"}' \
        "$PAYMENT_ENGINE_URL/api/v1/downstream/fraud/tenant-002" || echo "000")
    
    if [[ "$isolation_response" == "403" || "$isolation_response" == "404" ]]; then
        print_success "Cross-tenant isolation working (HTTP $isolation_response)"
    else
        print_warning "Cross-tenant isolation returned $isolation_response (expected 403/404)"
    fi
    
    print_success "Downstream routing tests completed!"
    
    # Summary
    print_info "Test Summary:"
    print_info "============="
    print_info "Payment Engine URL: $PAYMENT_ENGINE_URL"
    print_info "Namespace: $NAMESPACE"
    print_info "Tested Tenants: ${tenants[*]}"
    print_info ""
    print_info "Tested Endpoints:"
    print_info "================="
    print_info "Fraud System:    POST /api/v1/downstream/fraud/{tenantId}"
    print_info "Clearing System: POST /api/v1/downstream/clearing/{tenantId}"
    print_info "Auto Routing:    POST /api/v1/downstream/auto/{tenantId}"
    print_info "Specific Service: POST /api/v1/downstream/service/{tenantId}/{serviceType}"
    print_info "Configuration:   GET /api/v1/downstream/config/{tenantId}"
    print_info "Statistics:      GET /api/v1/downstream/stats/{tenantId}"
    print_info "Health Check:    GET /api/v1/downstream/health"
    print_info ""
    print_info "Routing Headers:"
    print_info "================"
    print_info "X-Tenant-ID:     Identifies the tenant"
    print_info "X-Service-Type:  Identifies the service (fraud/clearing)"
    print_info "X-Route-Context: Combines tenant and service"
    print_info "X-Downstream-Route: Final routing destination"
    print_info "X-Bank-Route:    Bank-specific routing path"
}

# Run main function
main "$@"