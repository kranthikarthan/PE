#!/bin/bash

# Validate Consolidation - Check that redundant services have been removed
# and consolidated services are properly configured

set -e

echo "🔍 Validating Service Consolidation"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if file exists
check_file_exists() {
    local file_path=$1
    local description=$2
    
    if [ -f "$file_path" ]; then
        echo -e "${GREEN}✅ $description: EXISTS${NC}"
        return 0
    else
        echo -e "${RED}❌ $description: MISSING${NC}"
        return 1
    fi
}

# Function to check if file does not exist (should be removed)
check_file_removed() {
    local file_path=$1
    local description=$2
    
    if [ ! -f "$file_path" ]; then
        echo -e "${GREEN}✅ $description: REMOVED (as expected)${NC}"
        return 0
    else
        echo -e "${RED}❌ $description: STILL EXISTS (should be removed)${NC}"
        return 1
    fi
}

# Function to check service configuration
check_service_config() {
    local service_name=$1
    local config_file=$2
    
    echo -e "${YELLOW}🔍 Checking $service_name configuration...${NC}"
    
    if [ -f "$config_file" ]; then
        echo -e "${GREEN}✅ $service_name configuration exists${NC}"
        
        # Check if it has RestTemplate configuration
        if grep -q "RestTemplate" "$config_file"; then
            echo -e "${GREEN}✅ $service_name has RestTemplate configuration${NC}"
        else
            echo -e "${YELLOW}⚠️  $service_name may need RestTemplate configuration${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ $service_name configuration missing${NC}"
        return 1
    fi
}

echo -e "${BLUE}📋 Validating Removed Services${NC}"
echo "--------------------------------"

# Check that duplicate services have been removed
echo -e "${YELLOW}🧪 Checking removed services...${NC}"

# Config Service - should be removed
check_file_removed "/workspace/services/config-service/src/main/java/com/paymentengine/config/service/MultiLevelAuthConfigurationService.java" "Config Service MultiLevelAuthConfigurationService"
check_file_removed "/workspace/services/config-service/src/main/java/com/paymentengine/config/service/ConfigurationHierarchyService.java" "Config Service ConfigurationHierarchyService"
check_file_removed "/workspace/services/config-service/src/main/java/com/paymentengine/config/dto/MultiLevelAuthConfigurationDTO.java" "Config Service MultiLevelAuthConfigurationDTO"
check_file_removed "/workspace/services/config-service/src/main/java/com/paymentengine/config/dto/ConfigurationHierarchyDTO.java" "Config Service ConfigurationHierarchyDTO"
check_file_removed "/workspace/services/config-service/src/main/java/com/paymentengine/config/dto/ResolvedAuthConfigurationDTO.java" "Config Service ResolvedAuthConfigurationDTO"

# Core Banking - should be removed
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/service/MultiLevelAuthConfigurationService.java" "Core Banking MultiLevelAuthConfigurationService"
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/service/EnhancedAuthenticationService.java" "Core Banking EnhancedAuthenticationService"
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/service/CertificateManagementService.java" "Core Banking CertificateManagementService"
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/controller/CertificateManagementController.java" "Core Banking CertificateManagementController"
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/dto/MultiLevelAuthConfigurationDTO.java" "Core Banking MultiLevelAuthConfigurationDTO"
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/dto/ConfigurationHierarchyDTO.java" "Core Banking ConfigurationHierarchyDTO"
check_file_removed "/workspace/services/core-banking/src/main/java/com/paymentengine/corebanking/dto/ResolvedAuthConfigurationDTO.java" "Core Banking ResolvedAuthConfigurationDTO"

# Gateway Service - should be removed
check_file_removed "/workspace/services/gateway/src/main/java/com/paymentengine/gateway/service/MultiLevelAuthConfigurationService.java" "Gateway Service MultiLevelAuthConfigurationService"
check_file_removed "/workspace/services/gateway/src/main/java/com/paymentengine/gateway/service/EnhancedAuthenticationService.java" "Gateway Service EnhancedAuthenticationService"
check_file_removed "/workspace/services/gateway/src/main/java/com/paymentengine/gateway/dto/MultiLevelAuthConfigurationDTO.java" "Gateway Service MultiLevelAuthConfigurationDTO"
check_file_removed "/workspace/services/gateway/src/main/java/com/paymentengine/gateway/dto/ConfigurationHierarchyDTO.java" "Gateway Service ConfigurationHierarchyDTO"
check_file_removed "/workspace/services/gateway/src/main/java/com/paymentengine/gateway/dto/ResolvedAuthConfigurationDTO.java" "Gateway Service ResolvedAuthConfigurationDTO"

# API Gateway - should be removed
check_file_removed "/workspace/services/api-gateway/src/main/java/com/paymentengine/gateway/service/OutgoingHttpService.java" "API Gateway OutgoingHttpService"
check_file_removed "/workspace/services/api-gateway/src/main/java/com/paymentengine/gateway/service/CertificateManagementService.java" "API Gateway CertificateManagementService"
check_file_removed "/workspace/services/api-gateway/src/main/java/com/paymentengine/gateway/controller/CertificateManagementController.java" "API Gateway CertificateManagementController"

# Frontend - should be removed
check_file_removed "/workspace/frontend/src/services/tenantAuthApi.ts" "Frontend tenantAuthApi"
check_file_removed "/workspace/frontend/src/components/tenant/TenantSetupWizard.tsx" "Frontend TenantSetupWizard"
check_file_removed "/workspace/frontend/src/components/tenant/TenantAuthConfiguration.tsx" "Frontend TenantAuthConfiguration"

echo ""
echo -e "${BLUE}📋 Validating Consolidated Services${NC}"
echo "------------------------------------"

# Check that consolidated services exist
echo -e "${YELLOW}🧪 Checking consolidated services...${NC}"

# Payment Processing Service - should exist (single source of truth)
check_file_exists "/workspace/services/payment-processing/src/main/java/com/paymentengine/paymentprocessing/service/MultiLevelAuthConfigurationService.java" "Payment Processing MultiLevelAuthConfigurationService"
check_file_exists "/workspace/services/payment-processing/src/main/java/com/paymentengine/paymentprocessing/service/OutgoingHttpService.java" "Payment Processing OutgoingHttpService"
check_file_exists "/workspace/services/payment-processing/src/main/java/com/paymentengine/paymentprocessing/service/CertificateManagementService.java" "Payment Processing CertificateManagementService"

# API Gateway - should exist (updated to use service-to-service communication)
check_file_exists "/workspace/services/api-gateway/src/main/java/com/paymentengine/gateway/service/MultiLevelAuthConfigurationService.java" "API Gateway MultiLevelAuthConfigurationService (updated)"

# Shared Service - should exist (new service client)
check_file_exists "/workspace/services/shared/src/main/java/com/paymentengine/shared/service/PaymentProcessingServiceClient.java" "Shared PaymentProcessingServiceClient"

# Frontend - should exist (consolidated components)
check_file_exists "/workspace/frontend/src/services/multiLevelAuthApi.ts" "Frontend multiLevelAuthApi"
check_file_exists "/workspace/frontend/src/components/tenant/EnhancedTenantSetupWizard.tsx" "Frontend EnhancedTenantSetupWizard"
check_file_exists "/workspace/frontend/src/components/multiLevelAuth/MultiLevelAuthConfigurationManager.tsx" "Frontend MultiLevelAuthConfigurationManager"

echo ""
echo -e "${BLUE}📋 Validating Service Configuration${NC}"
echo "----------------------------------"

# Check service configurations
check_service_config "API Gateway" "/workspace/services/api-gateway/src/main/java/com/paymentengine/gateway/service/MultiLevelAuthConfigurationService.java"

echo ""
echo -e "${BLUE}📋 Validating Frontend Integration${NC}"
echo "--------------------------------"

# Check that ModernTenantManagement has been updated
if [ -f "/workspace/frontend/src/components/tenant/ModernTenantManagement.tsx" ]; then
    echo -e "${GREEN}✅ ModernTenantManagement exists${NC}"
    
    # Check that it imports the correct components
    if grep -q "EnhancedTenantSetupWizard" "/workspace/frontend/src/components/tenant/ModernTenantManagement.tsx"; then
        echo -e "${GREEN}✅ ModernTenantManagement imports EnhancedTenantSetupWizard${NC}"
    else
        echo -e "${RED}❌ ModernTenantManagement missing EnhancedTenantSetupWizard import${NC}"
    fi
    
    if grep -q "MultiLevelAuthConfigurationManager" "/workspace/frontend/src/components/tenant/ModernTenantManagement.tsx"; then
        echo -e "${GREEN}✅ ModernTenantManagement imports MultiLevelAuthConfigurationManager${NC}"
    else
        echo -e "${RED}❌ ModernTenantManagement missing MultiLevelAuthConfigurationManager import${NC}"
    fi
    
    # Check that old components are not imported
    if ! grep -q "import.*TenantSetupWizard" "/workspace/frontend/src/components/tenant/ModernTenantManagement.tsx"; then
        echo -e "${GREEN}✅ ModernTenantManagement no longer imports TenantSetupWizard${NC}"
    else
        echo -e "${RED}❌ ModernTenantManagement still imports TenantSetupWizard${NC}"
    fi
    
    if ! grep -q "TenantAuthConfiguration" "/workspace/frontend/src/components/tenant/ModernTenantManagement.tsx"; then
        echo -e "${GREEN}✅ ModernTenantManagement no longer imports TenantAuthConfiguration${NC}"
    else
        echo -e "${RED}❌ ModernTenantManagement still imports TenantAuthConfiguration${NC}"
    fi
else
    echo -e "${RED}❌ ModernTenantManagement missing${NC}"
fi

echo ""
echo -e "${BLUE}📊 Consolidation Validation Summary${NC}"
echo "====================================="

# Count remaining services
echo -e "${YELLOW}📈 Service Count Analysis:${NC}"

# Count MultiLevelAuthConfigurationService implementations
multilevel_count=$(find /workspace/services -name "MultiLevelAuthConfigurationService.java" 2>/dev/null | wc -l)
echo "  MultiLevelAuthConfigurationService implementations: $multilevel_count (should be 2: Payment Processing + API Gateway)"

# Count OutgoingHttpService implementations
outgoing_count=$(find /workspace/services -name "OutgoingHttpService.java" 2>/dev/null | wc -l)
echo "  OutgoingHttpService implementations: $outgoing_count (should be 1: Payment Processing only)"

# Count CertificateManagementService implementations
cert_count=$(find /workspace/services -name "CertificateManagementService.java" 2>/dev/null | wc -l)
echo "  CertificateManagementService implementations: $cert_count (should be 1: Payment Processing only)"

# Count frontend API services
frontend_api_count=$(find /workspace/frontend/src/services -name "*Auth*.ts" 2>/dev/null | wc -l)
echo "  Frontend Auth API services: $frontend_api_count (should be 1: multiLevelAuthApi only)"

# Count frontend components
frontend_comp_count=$(find /workspace/frontend/src/components -name "*Tenant*" -o -name "*Auth*" | grep -E "\.(tsx|ts)$" | wc -l)
echo "  Frontend Auth/Tenant components: $frontend_comp_count (should be reduced)"

echo ""
echo -e "${GREEN}🎉 Consolidation Validation Complete!${NC}"
echo "====================================="

if [ "$multilevel_count" -eq 2 ] && [ "$outgoing_count" -eq 1 ] && [ "$cert_count" -eq 1 ] && [ "$frontend_api_count" -eq 1 ]; then
    echo -e "${GREEN}✅ Consolidation successful - all services properly consolidated${NC}"
    echo -e "${GREEN}✅ Redundant services removed${NC}"
    echo -e "${GREEN}✅ Single sources of truth established${NC}"
    echo -e "${GREEN}✅ Service-to-service communication configured${NC}"
    echo -e "${GREEN}✅ Frontend components consolidated${NC}"
    echo ""
    echo -e "${BLUE}🚀 System is ready for Istio vs API Gateway redundancy resolution!${NC}"
    exit 0
else
    echo -e "${RED}❌ Consolidation incomplete - some services may still be duplicated${NC}"
    echo -e "${YELLOW}⚠️  Please review the validation results above${NC}"
    exit 1
fi