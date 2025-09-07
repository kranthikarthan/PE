#!/bin/bash

# Test Consolidated Services After Redundancy Resolution
# This script tests the consolidated services to ensure they work correctly

set -e

echo "🧪 Testing Consolidated Services After Redundancy Resolution"
echo "=========================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
API_GATEWAY_URL="http://localhost:8080"
PAYMENT_PROCESSING_URL="http://localhost:8083"
FRONTEND_URL="http://localhost:3000"

# Test data
TENANT_ID="test-tenant-$(date +%s)"
SERVICE_TYPE="payment-processing"
ENDPOINT="/api/v1/payments"
PAYMENT_TYPE="credit-card"

echo -e "${BLUE}📋 Test Configuration:${NC}"
echo "  API Gateway URL: $API_GATEWAY_URL"
echo "  Payment Processing URL: $PAYMENT_PROCESSING_URL"
echo "  Frontend URL: $FRONTEND_URL"
echo "  Test Tenant ID: $TENANT_ID"
echo ""

# Function to check service health
check_service_health() {
    local service_name=$1
    local service_url=$2
    
    echo -e "${YELLOW}🔍 Checking $service_name health...${NC}"
    
    if curl -s -f "$service_url/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ $service_name is healthy${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not healthy${NC}"
        return 1
    fi
}

# Function to test API endpoint
test_api_endpoint() {
    local endpoint_name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_status=$5
    
    echo -e "${YELLOW}🧪 Testing $endpoint_name...${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✅ $endpoint_name: HTTP $response${NC}"
        return 0
    else
        echo -e "${RED}❌ $endpoint_name: Expected HTTP $expected_status, got HTTP $response${NC}"
        cat /tmp/response.json
        return 1
    fi
}

# Function to test service-to-service communication
test_service_communication() {
    echo -e "${BLUE}🔄 Testing Service-to-Service Communication${NC}"
    echo "------------------------------------------------"
    
    # Test 1: API Gateway -> Payment Processing Service (Multi-Level Auth)
    echo -e "${YELLOW}🧪 Testing API Gateway -> Payment Processing (Multi-Level Auth)...${NC}"
    
    test_data='{
        "basicInfo": {
            "tenantId": "'$TENANT_ID'",
            "tenantName": "Test Tenant",
            "environment": "test"
        },
        "multiLevelAuth": {
            "clearingSystemAuth": {
                "authMethod": "JWT",
                "jwtConfig": {
                    "secret": "test-secret",
                    "algorithm": "HS256"
                }
            }
        }
    }'
    
    if test_api_endpoint "API Gateway Enhanced Tenant Setup" "POST" \
        "$API_GATEWAY_URL/api/v1/enhanced-tenant-setup" \
        "$test_data" "200"; then
        echo -e "${GREEN}✅ Service-to-service communication working${NC}"
    else
        echo -e "${RED}❌ Service-to-service communication failed${NC}"
        return 1
    fi
    
    # Test 2: Direct Payment Processing Service (Multi-Level Auth)
    echo -e "${YELLOW}🧪 Testing Direct Payment Processing Service (Multi-Level Auth)...${NC}"
    
    if test_api_endpoint "Payment Processing Multi-Level Auth" "GET" \
        "$PAYMENT_PROCESSING_URL/api/v1/multi-level-auth/resolve/$TENANT_ID?serviceType=$SERVICE_TYPE&endpoint=$ENDPOINT&paymentType=$PAYMENT_TYPE" \
        "" "200"; then
        echo -e "${GREEN}✅ Direct Payment Processing Service working${NC}"
    else
        echo -e "${RED}❌ Direct Payment Processing Service failed${NC}"
        return 1
    fi
    
    # Test 3: Certificate Management
    echo -e "${YELLOW}🧪 Testing Certificate Management...${NC}"
    
    cert_data='{
        "tenantId": "'$TENANT_ID'",
        "certificateType": "SIGNING",
        "keyUsage": ["DIGITAL_SIGNATURE"],
        "extendedKeyUsage": ["CODE_SIGNING"]
    }'
    
    if test_api_endpoint "Certificate Generation" "POST" \
        "$PAYMENT_PROCESSING_URL/api/v1/certificates/generate" \
        "$cert_data" "200"; then
        echo -e "${GREEN}✅ Certificate Management working${NC}"
    else
        echo -e "${RED}❌ Certificate Management failed${NC}"
        return 1
    fi
    
    # Test 4: Outgoing HTTP Service
    echo -e "${YELLOW}🧪 Testing Outgoing HTTP Service...${NC}"
    
    http_data='{
        "tenantId": "'$TENANT_ID'",
        "serviceType": "'$SERVICE_TYPE'",
        "endpoint": "'$ENDPOINT'",
        "paymentType": "'$PAYMENT_TYPE'",
        "url": "https://example.com/api/test",
        "method": "POST",
        "headers": {
            "Content-Type": "application/json"
        },
        "body": "{\"test\": \"data\"}"
    }'
    
    if test_api_endpoint "Outgoing HTTP Call" "POST" \
        "$PAYMENT_PROCESSING_URL/api/v1/outgoing-http/call" \
        "$http_data" "200"; then
        echo -e "${GREEN}✅ Outgoing HTTP Service working${NC}"
    else
        echo -e "${RED}❌ Outgoing HTTP Service failed${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ All service-to-service communication tests passed${NC}"
}

# Function to test frontend integration
test_frontend_integration() {
    echo -e "${BLUE}🖥️  Testing Frontend Integration${NC}"
    echo "----------------------------------------"
    
    # Test 1: Frontend Health
    echo -e "${YELLOW}🧪 Testing Frontend Health...${NC}"
    
    if curl -s -f "$FRONTEND_URL" > /dev/null; then
        echo -e "${GREEN}✅ Frontend is accessible${NC}"
    else
        echo -e "${RED}❌ Frontend is not accessible${NC}"
        return 1
    fi
    
    # Test 2: Frontend API Integration
    echo -e "${YELLOW}🧪 Testing Frontend API Integration...${NC}"
    
    # Test that frontend can call the consolidated API
    if test_api_endpoint "Frontend Multi-Level Auth API" "GET" \
        "$API_GATEWAY_URL/api/v1/multi-level-auth/tenant/$TENANT_ID" \
        "" "200"; then
        echo -e "${GREEN}✅ Frontend API integration working${NC}"
    else
        echo -e "${RED}❌ Frontend API integration failed${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ All frontend integration tests passed${NC}"
}

# Function to test end-to-end flow
test_end_to_end_flow() {
    echo -e "${BLUE}🔄 Testing End-to-End Flow${NC}"
    echo "--------------------------------"
    
    # Test 1: Complete Tenant Setup Flow
    echo -e "${YELLOW}🧪 Testing Complete Tenant Setup Flow...${NC}"
    
    setup_data='{
        "basicInfo": {
            "tenantId": "'$TENANT_ID'",
            "tenantName": "E2E Test Tenant",
            "environment": "test",
            "description": "End-to-end test tenant"
        },
        "multiLevelAuth": {
            "clearingSystemAuth": {
                "authMethod": "JWS",
                "jwsConfig": {
                    "algorithm": "HS256",
                    "secret": "e2e-test-secret"
                }
            },
            "paymentTypeAuth": {
                "credit-card": {
                    "authMethod": "JWT",
                    "jwtConfig": {
                        "secret": "credit-card-secret",
                        "algorithm": "HS512"
                    }
                }
            },
            "downstreamCallAuth": {
                "fraud-system": {
                    "authMethod": "API_KEY",
                    "apiKeyConfig": {
                        "key": "fraud-api-key",
                        "headerName": "X-API-Key"
                    }
                }
            }
        },
        "clientHeaders": {
            "clearingSystemHeaders": {
                "clientId": "e2e-client-id",
                "clientSecret": "e2e-client-secret"
            }
        }
    }'
    
    if test_api_endpoint "Complete Tenant Setup" "POST" \
        "$API_GATEWAY_URL/api/v1/enhanced-tenant-setup" \
        "$setup_data" "200"; then
        echo -e "${GREEN}✅ Complete tenant setup flow working${NC}"
    else
        echo -e "${RED}❌ Complete tenant setup flow failed${NC}"
        return 1
    fi
    
    # Test 2: Multi-Level Auth Configuration Retrieval
    echo -e "${YELLOW}🧪 Testing Multi-Level Auth Configuration Retrieval...${NC}"
    
    if test_api_endpoint "Auth Configuration Retrieval" "GET" \
        "$PAYMENT_PROCESSING_URL/api/v1/multi-level-auth/resolve/$TENANT_ID?serviceType=payment-processing&endpoint=/api/v1/payments&paymentType=credit-card" \
        "" "200"; then
        echo -e "${GREEN}✅ Multi-level auth configuration retrieval working${NC}"
    else
        echo -e "${RED}❌ Multi-level auth configuration retrieval failed${NC}"
        return 1
    fi
    
    # Test 3: Certificate Generation and Validation
    echo -e "${YELLOW}🧪 Testing Certificate Generation and Validation...${NC}"
    
    cert_data='{
        "tenantId": "'$TENANT_ID'",
        "certificateType": "SIGNING",
        "keyUsage": ["DIGITAL_SIGNATURE"],
        "extendedKeyUsage": ["CODE_SIGNING"],
        "validityDays": 365
    }'
    
    if test_api_endpoint "Certificate Generation" "POST" \
        "$PAYMENT_PROCESSING_URL/api/v1/certificates/generate" \
        "$cert_data" "200"; then
        echo -e "${GREEN}✅ Certificate generation working${NC}"
    else
        echo -e "${RED}❌ Certificate generation failed${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ All end-to-end flow tests passed${NC}"
}

# Function to test error handling
test_error_handling() {
    echo -e "${BLUE}⚠️  Testing Error Handling${NC}"
    echo "----------------------------"
    
    # Test 1: Invalid tenant ID
    echo -e "${YELLOW}🧪 Testing Invalid Tenant ID...${NC}"
    
    if test_api_endpoint "Invalid Tenant ID" "GET" \
        "$PAYMENT_PROCESSING_URL/api/v1/multi-level-auth/resolve/invalid-tenant?serviceType=test&endpoint=/test&paymentType=test" \
        "" "404"; then
        echo -e "${GREEN}✅ Error handling for invalid tenant ID working${NC}"
    else
        echo -e "${RED}❌ Error handling for invalid tenant ID failed${NC}"
        return 1
    fi
    
    # Test 2: Invalid certificate data
    echo -e "${YELLOW}🧪 Testing Invalid Certificate Data...${NC}"
    
    invalid_cert_data='{
        "tenantId": "invalid-tenant",
        "certificateType": "INVALID_TYPE"
    }'
    
    if test_api_endpoint "Invalid Certificate Data" "POST" \
        "$PAYMENT_PROCESSING_URL/api/v1/certificates/generate" \
        "$invalid_cert_data" "400"; then
        echo -e "${GREEN}✅ Error handling for invalid certificate data working${NC}"
    else
        echo -e "${RED}❌ Error handling for invalid certificate data failed${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ All error handling tests passed${NC}"
}

# Main test execution
main() {
    echo -e "${BLUE}🚀 Starting Consolidated Services Testing${NC}"
    echo "=============================================="
    echo ""
    
    # Check service health
    echo -e "${BLUE}🏥 Service Health Check${NC}"
    echo "------------------------"
    
    if ! check_service_health "API Gateway" "$API_GATEWAY_URL"; then
        echo -e "${RED}❌ API Gateway is not healthy. Please start the service.${NC}"
        exit 1
    fi
    
    if ! check_service_health "Payment Processing Service" "$PAYMENT_PROCESSING_URL"; then
        echo -e "${RED}❌ Payment Processing Service is not healthy. Please start the service.${NC}"
        exit 1
    fi
    
    echo ""
    
    # Run tests
    test_service_communication
    echo ""
    
    test_frontend_integration
    echo ""
    
    test_end_to_end_flow
    echo ""
    
    test_error_handling
    echo ""
    
    # Summary
    echo -e "${GREEN}🎉 All Tests Completed Successfully!${NC}"
    echo "=========================================="
    echo -e "${GREEN}✅ Service-to-service communication: PASSED${NC}"
    echo -e "${GREEN}✅ Frontend integration: PASSED${NC}"
    echo -e "${GREEN}✅ End-to-end flow: PASSED${NC}"
    echo -e "${GREEN}✅ Error handling: PASSED${NC}"
    echo ""
    echo -e "${BLUE}📊 Test Summary:${NC}"
    echo "  - Consolidated services are working correctly"
    echo "  - Service-to-service communication is functional"
    echo "  - Frontend integration is working"
    echo "  - End-to-end flows are operational"
    echo "  - Error handling is robust"
    echo ""
    echo -e "${GREEN}🚀 System is ready for Istio vs API Gateway redundancy resolution!${NC}"
}

# Run main function
main "$@"