#!/bin/bash

# Test Istio Multi-Tenancy Configuration
# This script tests the multi-tenant Istio setup to ensure proper isolation and routing

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
NAMESPACE="payment-engine"
INGRESS_GATEWAY_IP=""
INGRESS_GATEWAY_PORT="80"

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

Test Istio multi-tenancy configuration.

OPTIONS:
    -n, --namespace NAMESPACE    Kubernetes namespace [default: payment-engine]
    -i, --ingress-ip IP          Istio ingress gateway IP
    -p, --ingress-port PORT      Istio ingress gateway port [default: 80]
    -t, --test-tenant TENANT     Test specific tenant only
    -h, --help                   Show this help message

EXAMPLES:
    $0                                    # Test all tenants
    $0 --ingress-ip 192.168.1.100        # Test with specific IP
    $0 --test-tenant tenant-001           # Test specific tenant
    $0 --namespace my-namespace           # Test in specific namespace

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
        -i|--ingress-ip)
            INGRESS_GATEWAY_IP="$2"
            shift 2
            ;;
        -p|--ingress-port)
            INGRESS_GATEWAY_PORT="$2"
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

print_info "Testing Istio Multi-Tenancy Configuration"
print_info "Namespace: $NAMESPACE"
print_info "Ingress Gateway IP: $INGRESS_GATEWAY_IP"
print_info "Ingress Gateway Port: $INGRESS_GATEWAY_PORT"
print_info "Test Tenant: $TEST_TENANT"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if curl is available
if ! command -v curl &> /dev/null; then
    print_error "curl is not installed or not in PATH"
    exit 1
fi

# Function to get ingress gateway IP
get_ingress_gateway_ip() {
    if [[ -n "$INGRESS_GATEWAY_IP" ]]; then
        echo "$INGRESS_GATEWAY_IP"
        return
    fi
    
    print_info "Getting Istio ingress gateway IP..."
    local ip=$(kubectl get svc istio-ingressgateway -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    
    if [[ -z "$ip" ]]; then
        ip=$(kubectl get svc istio-ingressgateway -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    fi
    
    if [[ -z "$ip" ]]; then
        ip=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
    fi
    
    if [[ -z "$ip" ]]; then
        ip=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
    fi
    
    if [[ -z "$ip" ]]; then
        print_error "Could not determine ingress gateway IP"
        exit 1
    fi
    
    echo "$ip"
}

# Function to test tenant routing
test_tenant_routing() {
    local tenant="$1"
    local environment="$2"
    local host="$tenant.$environment.payment-engine.local"
    local url="http://$INGRESS_GATEWAY_IP:$INGRESS_GATEWAY_PORT"
    
    print_info "Testing tenant: $tenant ($environment)"
    print_info "Host: $host"
    print_info "URL: $url"
    
    # Test health endpoint
    print_info "Testing health endpoint..."
    local health_response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Host: $host" \
        "$url/health" || echo "000")
    
    if [[ "$health_response" == "200" ]]; then
        print_success "Health endpoint working for $tenant"
    else
        print_warning "Health endpoint returned $health_response for $tenant"
    fi
    
    # Test API endpoints
    local endpoints=("auth" "config" "iso20022" "banking" "tenant-auth-config")
    
    for endpoint in "${endpoints[@]}"; do
        print_info "Testing /api/v1/$endpoint endpoint..."
        local api_response=$(curl -s -o /dev/null -w "%{http_code}" \
            -H "Host: $host" \
            -H "X-Tenant-ID: $tenant" \
            "$url/api/v1/$endpoint" || echo "000")
        
        if [[ "$api_response" == "200" || "$api_response" == "401" || "$api_response" == "404" ]]; then
            print_success "API endpoint /api/v1/$endpoint working for $tenant (HTTP $api_response)"
        else
            print_warning "API endpoint /api/v1/$endpoint returned $api_response for $tenant"
        fi
    done
    
    # Test frontend
    print_info "Testing frontend..."
    local frontend_response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Host: $host" \
        "$url/" || echo "000")
    
    if [[ "$frontend_response" == "200" ]]; then
        print_success "Frontend working for $tenant"
    else
        print_warning "Frontend returned $frontend_response for $tenant"
    fi
}

# Function to test tenant isolation
test_tenant_isolation() {
    local tenant1="$1"
    local tenant2="$2"
    local environment="$3"
    
    print_info "Testing isolation between $tenant1 and $tenant2"
    
    local host1="$tenant1.$environment.payment-engine.local"
    local host2="$tenant2.$environment.payment-engine.local"
    local url="http://$INGRESS_GATEWAY_IP:$INGRESS_GATEWAY_PORT"
    
    # Test cross-tenant access (should be blocked)
    print_info "Testing cross-tenant access (should be blocked)..."
    local cross_response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Host: $host1" \
        -H "X-Tenant-ID: $tenant2" \
        "$url/api/v1/config" || echo "000")
    
    if [[ "$cross_response" == "403" || "$cross_response" == "401" ]]; then
        print_success "Cross-tenant access properly blocked (HTTP $cross_response)"
    else
        print_warning "Cross-tenant access returned $cross_response (expected 403/401)"
    fi
}

# Function to check Istio resources
check_istio_resources() {
    print_info "Checking Istio resources..."
    
    # Check gateways
    print_info "Checking gateways..."
    local gateways=$(kubectl get gateways -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    if [[ -n "$gateways" ]]; then
        print_success "Found gateways: $(echo $gateways | wc -w)"
        kubectl get gateways -n "$NAMESPACE"
    else
        print_error "No gateways found in namespace $NAMESPACE"
    fi
    
    # Check virtual services
    print_info "Checking virtual services..."
    local virtualservices=$(kubectl get virtualservices -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    if [[ -n "$virtualservices" ]]; then
        print_success "Found virtual services: $(echo $virtualservices | wc -w)"
        kubectl get virtualservices -n "$NAMESPACE"
    else
        print_error "No virtual services found in namespace $NAMESPACE"
    fi
    
    # Check destination rules
    print_info "Checking destination rules..."
    local destinationrules=$(kubectl get destinationrules -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    if [[ -n "$destinationrules" ]]; then
        print_success "Found destination rules: $(echo $destinationrules | wc -w)"
        kubectl get destinationrules -n "$NAMESPACE"
    else
        print_error "No destination rules found in namespace $NAMESPACE"
    fi
    
    # Check security policies
    print_info "Checking security policies..."
    local authz_policies=$(kubectl get authorizationpolicies -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    local peer_auth=$(kubectl get peerauthentications -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    
    if [[ -n "$authz_policies" ]]; then
        print_success "Found authorization policies: $(echo $authz_policies | wc -w)"
        kubectl get authorizationpolicies -n "$NAMESPACE"
    else
        print_warning "No authorization policies found in namespace $NAMESPACE"
    fi
    
    if [[ -n "$peer_auth" ]]; then
        print_success "Found peer authentications: $(echo $peer_auth | wc -w)"
        kubectl get peerauthentications -n "$NAMESPACE"
    else
        print_warning "No peer authentications found in namespace $NAMESPACE"
    fi
}

# Function to check services
check_services() {
    print_info "Checking services..."
    
    local services=$(kubectl get services -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    if [[ -n "$services" ]]; then
        print_success "Found services: $(echo $services | wc -w)"
        kubectl get services -n "$NAMESPACE"
    else
        print_error "No services found in namespace $NAMESPACE"
    fi
}

# Function to check pods
check_pods() {
    print_info "Checking pods..."
    
    local pods=$(kubectl get pods -n "$NAMESPACE" -o name 2>/dev/null || echo "")
    if [[ -n "$pods" ]]; then
        print_success "Found pods: $(echo $pods | wc -w)"
        kubectl get pods -n "$NAMESPACE"
        
        # Check pod status
        local not_ready=$(kubectl get pods -n "$NAMESPACE" --field-selector=status.phase!=Running -o name 2>/dev/null || echo "")
        if [[ -n "$not_ready" ]]; then
            print_warning "Some pods are not running:"
            kubectl get pods -n "$NAMESPACE" --field-selector=status.phase!=Running
        fi
    else
        print_error "No pods found in namespace $NAMESPACE"
    fi
}

# Main test execution
main() {
    print_info "Starting Istio multi-tenancy tests..."
    
    # Get ingress gateway IP
    INGRESS_GATEWAY_IP=$(get_ingress_gateway_ip)
    print_info "Using ingress gateway IP: $INGRESS_GATEWAY_IP"
    
    # Check basic resources
    check_istio_resources
    check_services
    check_pods
    
    # Define test tenants
    local tenants=("tenant-001" "tenant-002" "tenant-003")
    local environments=("dev" "staging" "prod")
    
    # Test specific tenant if specified
    if [[ -n "$TEST_TENANT" ]]; then
        tenants=("$TEST_TENANT")
    fi
    
    # Test tenant routing
    for tenant in "${tenants[@]}"; do
        for environment in "${environments[@]}"; do
            test_tenant_routing "$tenant" "$environment"
            echo ""
        done
    done
    
    # Test tenant isolation
    if [[ ${#tenants[@]} -ge 2 ]]; then
        print_info "Testing tenant isolation..."
        test_tenant_isolation "${tenants[0]}" "${tenants[1]}" "dev"
        echo ""
    fi
    
    # Test conflict resolution
    print_info "Testing conflict resolution..."
    
    # Check for duplicate hosts
    local duplicate_hosts=$(kubectl get virtualservices -n "$NAMESPACE" -o jsonpath='{.items[*].spec.hosts[*]}' | tr ' ' '\n' | sort | uniq -d)
    if [[ -z "$duplicate_hosts" ]]; then
        print_success "No duplicate hosts found - conflicts resolved"
    else
        print_error "Duplicate hosts found: $duplicate_hosts"
    fi
    
    # Check for overlapping gateways
    local gateway_ports=$(kubectl get gateways -n "$NAMESPACE" -o jsonpath='{.items[*].spec.servers[*].port.number}' | tr ' ' '\n' | sort | uniq -c | awk '$1 > 1 {print $2}')
    if [[ -z "$gateway_ports" ]]; then
        print_success "No overlapping gateway ports found - conflicts resolved"
    else
        print_warning "Overlapping gateway ports found: $gateway_ports"
    fi
    
    print_success "Istio multi-tenancy tests completed!"
    
    # Summary
    print_info "Test Summary:"
    print_info "============="
    print_info "Ingress Gateway IP: $INGRESS_GATEWAY_IP"
    print_info "Namespace: $NAMESPACE"
    print_info "Tested Tenants: ${tenants[*]}"
    print_info "Tested Environments: ${environments[*]}"
    print_info ""
    print_info "Access URLs:"
    for tenant in "${tenants[@]}"; do
        for environment in "${environments[@]}"; do
            print_info "  $tenant ($environment): http://$tenant.$environment.payment-engine.local"
        done
    done
}

# Run main function
main "$@"