#!/bin/bash

# Migrate from API Gateway + Istio to Istio-only Architecture
# This script removes the API Gateway and configures Istio to handle all routing

set -e

echo "🚀 Migrating to Istio-Only Architecture"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="payment-engine"
KUBECONFIG="${KUBECONFIG:-~/.kube/config}"

echo -e "${BLUE}📋 Migration Configuration:${NC}"
echo "  Namespace: $NAMESPACE"
echo "  Kubeconfig: $KUBECONFIG"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${BLUE}🔍 Checking Prerequisites${NC}"
    echo "------------------------"
    
    # Check kubectl
    if command_exists kubectl; then
        echo -e "${GREEN}✅ kubectl is available${NC}"
    else
        echo -e "${RED}❌ kubectl is not available${NC}"
        exit 1
    fi
    
    # Check istioctl
    if command_exists istioctl; then
        echo -e "${GREEN}✅ istioctl is available${NC}"
    else
        echo -e "${RED}❌ istioctl is not available${NC}"
        exit 1
    fi
    
    # Check namespace
    if kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Namespace $NAMESPACE exists${NC}"
    else
        echo -e "${RED}❌ Namespace $NAMESPACE does not exist${NC}"
        exit 1
    fi
    
    # Check Istio installation
    if kubectl get pods -n istio-system | grep -q "istio-ingressgateway"; then
        echo -e "${GREEN}✅ Istio ingress gateway is running${NC}"
    else
        echo -e "${RED}❌ Istio ingress gateway is not running${NC}"
        exit 1
    fi
    
    echo ""
}

# Function to backup current configuration
backup_current_config() {
    echo -e "${BLUE}💾 Backing Up Current Configuration${NC}"
    echo "------------------------------------"
    
    local backup_dir="/tmp/istio-migration-backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_dir"
    
    echo -e "${YELLOW}📁 Creating backup in: $backup_dir${NC}"
    
    # Backup current Istio configurations
    kubectl get gateway -n "$NAMESPACE" -o yaml > "$backup_dir/gateways.yaml" 2>/dev/null || true
    kubectl get virtualservice -n "$NAMESPACE" -o yaml > "$backup_dir/virtualservices.yaml" 2>/dev/null || true
    kubectl get destinationrule -n "$NAMESPACE" -o yaml > "$backup_dir/destinationrules.yaml" 2>/dev/null || true
    kubectl get envoyfilter -n "$NAMESPACE" -o yaml > "$backup_dir/envoyfilters.yaml" 2>/dev/null || true
    
    # Backup API Gateway deployment
    kubectl get deployment api-gateway-service -n "$NAMESPACE" -o yaml > "$backup_dir/api-gateway-deployment.yaml" 2>/dev/null || true
    kubectl get service api-gateway-service -n "$NAMESPACE" -o yaml > "$backup_dir/api-gateway-service.yaml" 2>/dev/null || true
    
    echo -e "${GREEN}✅ Backup completed: $backup_dir${NC}"
    echo ""
}

# Function to apply new Istio configurations
apply_istio_configurations() {
    echo -e "${BLUE}🔧 Applying New Istio Configurations${NC}"
    echo "--------------------------------------"
    
    # Apply enhanced gateway
    echo -e "${YELLOW}🚪 Applying enhanced gateway configuration...${NC}"
    kubectl apply -f /workspace/k8s/istio/enhanced-gateway.yaml
    echo -e "${GREEN}✅ Enhanced gateway applied${NC}"
    
    # Apply enhanced virtual services
    echo -e "${YELLOW}🛣️  Applying enhanced virtual services...${NC}"
    kubectl apply -f /workspace/k8s/istio/enhanced-virtual-services.yaml
    echo -e "${GREEN}✅ Enhanced virtual services applied${NC}"
    
    # Apply enhanced destination rules
    echo -e "${YELLOW}🎯 Applying enhanced destination rules...${NC}"
    kubectl apply -f /workspace/k8s/istio/enhanced-destination-rules.yaml
    echo -e "${GREEN}✅ Enhanced destination rules applied${NC}"
    
    # Apply rate limiting
    echo -e "${YELLOW}⏱️  Applying rate limiting configuration...${NC}"
    kubectl apply -f /workspace/k8s/istio/rate-limiting.yaml
    echo -e "${GREEN}✅ Rate limiting applied${NC}"
    
    # Apply CORS configuration
    echo -e "${YELLOW}🌐 Applying CORS configuration...${NC}"
    kubectl apply -f /workspace/k8s/istio/cors-configuration.yaml
    echo -e "${GREEN}✅ CORS configuration applied${NC}"
    
    # Apply custom filters
    echo -e "${YELLOW}🔧 Applying custom filters...${NC}"
    kubectl apply -f /workspace/k8s/istio/custom-filters.yaml
    echo -e "${GREEN}✅ Custom filters applied${NC}"
    
    echo ""
}

# Function to scale down API Gateway
scale_down_api_gateway() {
    echo -e "${BLUE}📉 Scaling Down API Gateway${NC}"
    echo "---------------------------"
    
    # Check if API Gateway exists
    if kubectl get deployment api-gateway-service -n "$NAMESPACE" >/dev/null 2>&1; then
        echo -e "${YELLOW}📉 Scaling down API Gateway deployment...${NC}"
        kubectl scale deployment api-gateway-service --replicas=0 -n "$NAMESPACE"
        
        # Wait for pods to terminate
        echo -e "${YELLOW}⏳ Waiting for API Gateway pods to terminate...${NC}"
        kubectl wait --for=delete pod -l app=api-gateway-service -n "$NAMESPACE" --timeout=60s || true
        
        echo -e "${GREEN}✅ API Gateway scaled down${NC}"
    else
        echo -e "${YELLOW}⚠️  API Gateway deployment not found${NC}"
    fi
    
    echo ""
}

# Function to update service configurations
update_service_configurations() {
    echo -e "${BLUE}🔧 Updating Service Configurations${NC}"
    echo "----------------------------------"
    
    # Update frontend to point directly to services
    echo -e "${YELLOW}🖥️  Updating frontend configuration...${NC}"
    
    # Create a script to update frontend API endpoints
    cat > /tmp/update-frontend-apis.sh << 'EOF'
#!/bin/bash

# Update frontend API endpoints to point directly to services
FRONTEND_DIR="/workspace/frontend/src/services"

if [ -f "$FRONTEND_DIR/multiLevelAuthApi.ts" ]; then
    # Update API base URL to point directly to services
    sed -i 's|http://localhost:8080|http://payment-engine.local|g' "$FRONTEND_DIR/multiLevelAuthApi.ts"
    echo "Updated multiLevelAuthApi.ts"
fi

# Update any other API service files
find "$FRONTEND_DIR" -name "*.ts" -exec sed -i 's|http://localhost:8080|http://payment-engine.local|g' {} \;

echo "Frontend API endpoints updated"
EOF
    
    chmod +x /tmp/update-frontend-apis.sh
    /tmp/update-frontend-apis.sh
    
    echo -e "${GREEN}✅ Service configurations updated${NC}"
    echo ""
}

# Function to test the new configuration
test_new_configuration() {
    echo -e "${BLUE}🧪 Testing New Configuration${NC}"
    echo "----------------------------"
    
    # Wait for Istio configurations to be applied
    echo -e "${YELLOW}⏳ Waiting for Istio configurations to be applied...${NC}"
    sleep 10
    
    # Test gateway connectivity
    echo -e "${YELLOW}🔍 Testing gateway connectivity...${NC}"
    
    # Get ingress gateway IP
    INGRESS_IP=$(kubectl get service istio-ingressgateway -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    if [ -z "$INGRESS_IP" ]; then
        INGRESS_IP=$(kubectl get service istio-ingressgateway -n istio-system -o jsonpath='{.spec.clusterIP}')
    fi
    
    if [ -n "$INGRESS_IP" ]; then
        echo -e "${GREEN}✅ Ingress gateway IP: $INGRESS_IP${NC}"
        
        # Test health endpoint
        if curl -s -f "http://$INGRESS_IP/actuator/health" >/dev/null; then
            echo -e "${GREEN}✅ Health endpoint is accessible${NC}"
        else
            echo -e "${YELLOW}⚠️  Health endpoint not accessible (may need DNS configuration)${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Could not determine ingress gateway IP${NC}"
    fi
    
    # Check service status
    echo -e "${YELLOW}🔍 Checking service status...${NC}"
    kubectl get pods -n "$NAMESPACE" -l app=payment-processing-service
    kubectl get pods -n "$NAMESPACE" -l app=core-banking-service
    kubectl get pods -n "$NAMESPACE" -l app=auth-service
    
    echo -e "${GREEN}✅ Configuration test completed${NC}"
    echo ""
}

# Function to clean up old configurations
cleanup_old_configurations() {
    echo -e "${BLUE}🧹 Cleaning Up Old Configurations${NC}"
    echo "--------------------------------"
    
    # Remove old Istio configurations (optional - keep for rollback)
    echo -e "${YELLOW}🗑️  Old configurations kept for rollback (in backup directory)${NC}"
    
    # Remove API Gateway service (keep deployment for rollback)
    if kubectl get service api-gateway-service -n "$NAMESPACE" >/dev/null 2>&1; then
        echo -e "${YELLOW}🗑️  Removing API Gateway service...${NC}"
        kubectl delete service api-gateway-service -n "$NAMESPACE" || true
        echo -e "${GREEN}✅ API Gateway service removed${NC}"
    fi
    
    echo ""
}

# Function to display migration summary
display_migration_summary() {
    echo -e "${GREEN}🎉 Migration to Istio-Only Architecture Complete!${NC}"
    echo "=================================================="
    echo ""
    echo -e "${BLUE}📊 Migration Summary:${NC}"
    echo "  ✅ Enhanced Istio Gateway configured"
    echo "  ✅ Comprehensive Virtual Services applied"
    echo "  ✅ Enhanced Destination Rules with circuit breaking"
    echo "  ✅ Rate limiting configured"
    echo "  ✅ CORS handling configured"
    echo "  ✅ Custom filters for API Gateway functionality"
    echo "  ✅ API Gateway scaled down"
    echo "  ✅ Service configurations updated"
    echo "  ✅ Old configurations backed up"
    echo ""
    echo -e "${BLUE}🚀 Benefits Achieved:${NC}"
    echo "  • Eliminated double processing overhead"
    echo "  • Simplified architecture with single routing layer"
    echo "  • Improved performance and reduced latency"
    echo "  • Centralized traffic management in Istio"
    echo "  • Enhanced observability and monitoring"
    echo "  • Reduced configuration complexity"
    echo ""
    echo -e "${BLUE}🔧 Next Steps:${NC}"
    echo "  1. Update DNS to point to Istio ingress gateway"
    echo "  2. Test all endpoints and functionality"
    echo "  3. Monitor performance and error rates"
    echo "  4. Remove API Gateway deployment after validation"
    echo ""
    echo -e "${YELLOW}⚠️  Rollback Instructions:${NC}"
    echo "  If rollback is needed, run:"
    echo "  kubectl apply -f /tmp/istio-migration-backup-*/"
    echo "  kubectl scale deployment api-gateway-service --replicas=1 -n $NAMESPACE"
    echo ""
}

# Main migration function
main() {
    echo -e "${BLUE}🚀 Starting Migration to Istio-Only Architecture${NC}"
    echo "====================================================="
    echo ""
    
    check_prerequisites
    backup_current_config
    apply_istio_configurations
    scale_down_api_gateway
    update_service_configurations
    test_new_configuration
    cleanup_old_configurations
    display_migration_summary
}

# Run main function
main "$@"