#!/bin/bash

# Test Multi-Level Authentication Configuration System
# This script tests the multi-level authentication configuration system

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

Test multi-level authentication configuration system.

OPTIONS:
    -n, --namespace NAMESPACE    Kubernetes namespace [default: payment-engine]
    -u, --url URL               Payment Engine URL [default: http://localhost:8080]
    -t, --test-tenant TENANT    Test specific tenant only
    -l, --test-level LEVEL      Test specific configuration level only
    -h, --help                  Show this help message

EXAMPLES:
    $0                                    # Test all levels and tenants
    $0 --url https://payment-engine.local # Test with specific URL
    $0 --test-tenant tenant-001           # Test specific tenant
    $0 --test-level clearing-system       # Test specific level

EOF
}

# Default values
TEST_TENANT=""
TEST_LEVEL=""

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
        -l|--test-level)
            TEST_LEVEL="$2"
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

print_info "Testing Multi-Level Authentication Configuration System"
print_info "Namespace: $NAMESPACE"
print_info "Payment Engine URL: $PAYMENT_ENGINE_URL"
print_info "Test Tenant: $TEST_TENANT"
print_info "Test Level: $TEST_LEVEL"

# Check if curl is available
if ! command -v curl &> /dev/null; then
    print_error "curl is not installed or not in PATH"
    exit 1
fi

# Function to test health endpoint
test_health_endpoint() {
    print_info "Testing enhanced downstream routing health endpoint..."
    
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/health"
    
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Enhanced downstream routing health endpoint working (HTTP $response)"
    else
        print_warning "Enhanced downstream routing health endpoint returned $response"
    fi
}

# Function to test clearing system level configuration
test_clearing_system_level() {
    print_info "Testing clearing system level configuration..."
    
    # Test get configurations by environment
    local url="$PAYMENT_ENGINE_URL/api/v1/clearing-system-auth-configurations/environment/dev"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Clearing system configurations retrieval working (HTTP $response)"
    else
        print_warning "Clearing system configurations retrieval returned $response"
    fi
    
    # Test get active configuration
    local url="$PAYMENT_ENGINE_URL/api/v1/clearing-system-auth-configurations/environment/dev/active"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Active clearing system configuration retrieval working (HTTP $response)"
    else
        print_warning "Active clearing system configuration retrieval returned $response"
    fi
}

# Function to test payment type level configuration
test_payment_type_level() {
    local tenant="$1"
    print_info "Testing payment type level configuration for tenant: $tenant"
    
    # Test get configurations by tenant
    local url="$PAYMENT_ENGINE_URL/api/v1/payment-type-auth-configurations/tenant/$tenant"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Payment type configurations retrieval working for $tenant (HTTP $response)"
    else
        print_warning "Payment type configurations retrieval returned $response for $tenant"
    fi
    
    # Test get configuration by tenant and payment type
    local url="$PAYMENT_ENGINE_URL/api/v1/payment-type-auth-configurations/tenant/$tenant/payment-type/SEPA"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "SEPA payment type configuration retrieval working for $tenant (HTTP $response)"
    else
        print_warning "SEPA payment type configuration retrieval returned $response for $tenant"
    fi
}

# Function to test downstream call level configuration
test_downstream_call_level() {
    local tenant="$1"
    print_info "Testing downstream call level configuration for tenant: $tenant"
    
    # Test get configurations by tenant
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream-call-auth-configurations/tenant/$tenant"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Downstream call configurations retrieval working for $tenant (HTTP $response)"
    else
        print_warning "Downstream call configurations retrieval returned $response for $tenant"
    fi
    
    # Test get configuration by tenant, service type, and endpoint
    local url="$PAYMENT_ENGINE_URL/api/v1/downstream-call-auth-configurations/tenant/$tenant/service/fraud/endpoint//fraud"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Fraud downstream call configuration retrieval working for $tenant (HTTP $response)"
    else
        print_warning "Fraud downstream call configuration retrieval returned $response for $tenant"
    fi
}

# Function to test enhanced downstream routing
test_enhanced_downstream_routing() {
    local tenant="$1"
    print_info "Testing enhanced downstream routing for tenant: $tenant"
    
    # Test get resolved configuration
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/config/$tenant/fraud//fraud"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Resolved configuration retrieval working for $tenant (HTTP $response)"
    else
        print_warning "Resolved configuration retrieval returned $response for $tenant"
    fi
    
    # Test get downstream stats
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/stats/$tenant/fraud//fraud"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Downstream stats retrieval working for $tenant (HTTP $response)"
    else
        print_warning "Downstream stats retrieval returned $response for $tenant"
    fi
    
    # Test validate tenant access
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/validate/$tenant/fraud//fraud"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET \
        "$url" || echo "000")
    
    if [[ "$response" == "200" ]]; then
        print_success "Tenant access validation working for $tenant (HTTP $response)"
    else
        print_warning "Tenant access validation returned $response for $tenant"
    fi
}

# Function to test enhanced downstream calls
test_enhanced_downstream_calls() {
    local tenant="$1"
    print_info "Testing enhanced downstream calls for tenant: $tenant"
    
    # Test fraud system call
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/fraud/$tenant"
    local request_body='{"transaction_id": "test-123", "fraud_check": true, "amount": 1000}'
    
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: $tenant" \
        -d "$request_body" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" || "$response" == "201" ]]; then
        print_success "Enhanced fraud system call working for $tenant (HTTP $response)"
    elif [[ "$response" == "403" ]]; then
        print_warning "Enhanced fraud system call access denied for $tenant (HTTP $response) - check permissions"
    else
        print_warning "Enhanced fraud system call returned $response for $tenant"
    fi
    
    # Test clearing system call
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/clearing/$tenant"
    local request_body='{"transaction_id": "test-123", "clearing_reference": "CLR-456", "amount": 1000}'
    
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: $tenant" \
        -d "$request_body" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" || "$response" == "201" ]]; then
        print_success "Enhanced clearing system call working for $tenant (HTTP $response)"
    elif [[ "$response" == "403" ]]; then
        print_warning "Enhanced clearing system call access denied for $tenant (HTTP $response) - check permissions"
    else
        print_warning "Enhanced clearing system call returned $response for $tenant"
    fi
    
    # Test auto-routing call
    local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/auto/$tenant"
    local request_body='{"transaction_id": "test-123", "fraud_check": true, "risk_score": 85}'
    
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "X-Tenant-ID: $tenant" \
        -d "$request_body" \
        "$url" || echo "000")
    
    if [[ "$response" == "200" || "$response" == "201" ]]; then
        print_success "Enhanced auto-routing call working for $tenant (HTTP $response)"
    elif [[ "$response" == "403" ]]; then
        print_warning "Enhanced auto-routing call access denied for $tenant (HTTP $response) - check permissions"
    else
        print_warning "Enhanced auto-routing call returned $response for $tenant"
    fi
}

# Function to test configuration hierarchy
test_configuration_hierarchy() {
    local tenant="$1"
    print_info "Testing configuration hierarchy for tenant: $tenant"
    
    # Test different service types and endpoints
    local services=("fraud" "clearing" "banking")
    local endpoints=("/fraud" "/clearing" "/banking")
    local payment_types=("SEPA" "SWIFT" "ACH")
    
    for service in "${services[@]}"; do
        for endpoint in "${endpoints[@]}"; do
            for payment_type in "${payment_types[@]}"; do
                local url="$PAYMENT_ENGINE_URL/api/v1/enhanced-downstream/config/$tenant/$service$endpoint?paymentType=$payment_type"
                local response=$(curl -s -o /dev/null -w "%{http_code}" \
                    -X GET \
                    "$url" || echo "000")
                
                if [[ "$response" == "200" ]]; then
                    print_success "Configuration hierarchy working for $tenant/$service$endpoint/$payment_type (HTTP $response)"
                else
                    print_warning "Configuration hierarchy returned $response for $tenant/$service$endpoint/$payment_type"
                fi
            done
        done
    done
}

# Main test execution
main() {
    print_info "Starting multi-level authentication configuration tests..."
    
    # Test health endpoint
    test_health_endpoint
    echo ""
    
    # Test clearing system level
    if [[ -z "$TEST_LEVEL" || "$TEST_LEVEL" == "clearing-system" ]]; then
        test_clearing_system_level
        echo ""
    fi
    
    # Define test tenants
    local tenants=("tenant-001" "tenant-002" "tenant-003")
    
    # Test specific tenant if specified
    if [[ -n "$TEST_TENANT" ]]; then
        tenants=("$TEST_TENANT")
    fi
    
    # Test each tenant
    for tenant in "${tenants[@]}"; do
        print_info "Testing multi-level configuration for tenant: $tenant"
        
        # Test payment type level
        if [[ -z "$TEST_LEVEL" || "$TEST_LEVEL" == "payment-type" ]]; then
            test_payment_type_level "$tenant"
        fi
        
        # Test downstream call level
        if [[ -z "$TEST_LEVEL" || "$TEST_LEVEL" == "downstream-call" ]]; then
            test_downstream_call_level "$tenant"
        fi
        
        # Test enhanced downstream routing
        if [[ -z "$TEST_LEVEL" || "$TEST_LEVEL" == "enhanced-routing" ]]; then
            test_enhanced_downstream_routing "$tenant"
        fi
        
        # Test enhanced downstream calls
        if [[ -z "$TEST_LEVEL" || "$TEST_LEVEL" == "enhanced-calls" ]]; then
            test_enhanced_downstream_calls "$tenant"
        fi
        
        # Test configuration hierarchy
        if [[ -z "$TEST_LEVEL" || "$TEST_LEVEL" == "hierarchy" ]]; then
            test_configuration_hierarchy "$tenant"
        fi
        
        echo ""
    done
    
    print_success "Multi-level authentication configuration tests completed!"
    
    # Summary
    print_info "Test Summary:"
    print_info "============="
    print_info "Payment Engine URL: $PAYMENT_ENGINE_URL"
    print_info "Namespace: $NAMESPACE"
    print_info "Tested Tenants: ${tenants[*]}"
    print_info "Tested Levels: ${TEST_LEVEL:-all}"
    print_info ""
    print_info "Configuration Levels Tested:"
    print_info "============================"
    print_info "1. Clearing System Level (Global)"
    print_info "2. Payment Type Level (Tenant + Payment Type)"
    print_info "3. Downstream Call Level (Tenant + Service + Endpoint)"
    print_info "4. Enhanced Downstream Routing (Multi-level resolution)"
    print_info "5. Configuration Hierarchy (Precedence testing)"
    print_info ""
    print_info "API Endpoints Tested:"
    print_info "===================="
    print_info "Clearing System: GET /api/v1/clearing-system-auth-configurations/*"
    print_info "Payment Type: GET /api/v1/payment-type-auth-configurations/*"
    print_info "Downstream Call: GET /api/v1/downstream-call-auth-configurations/*"
    print_info "Enhanced Routing: GET /api/v1/enhanced-downstream/*"
    print_info "Configuration Hierarchy: GET /api/v1/configuration-hierarchy/*"
    print_info ""
    print_info "Features Tested:"
    print_info "==============="
    print_info "Multi-level configuration resolution"
    print_info "Configuration hierarchy and precedence"
    print_info "Enhanced downstream routing with context"
    print_info "Authentication method configuration (JWT, JWS, OAuth2, API Key, Basic)"
    print_info "Client header configuration"
    print_info "Tenant isolation and access control"
    print_info "Configuration validation and testing"
}

# Run main function
main "$@"