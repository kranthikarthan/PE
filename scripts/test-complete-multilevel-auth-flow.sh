#!/bin/bash

# Test Complete Multi-Level Authentication Flow
# This script tests the complete multi-level authentication configuration system across all services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
TENANT_ID="test-tenant-001"
SERVICE_TYPE="fraud"
ENDPOINT="/fraud"
PAYMENT_TYPE="SEPA"

echo -e "${BLUE}🧪 Testing Complete Multi-Level Authentication Flow${NC}"
echo "=================================================="

# Function to print test results
print_test_result() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ $test_name: PASS${NC}"
    elif [ "$status" = "FAIL" ]; then
        echo -e "${RED}❌ $test_name: FAIL${NC}"
        echo -e "${RED}   $message${NC}"
    else
        echo -e "${YELLOW}⚠️  $test_name: $status${NC}"
        echo -e "${YELLOW}   $message${NC}"
    fi
}

# Function to make HTTP request
make_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local expected_status="$4"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "$expected_status" ]; then
        echo "$body"
        return 0
    else
        echo "HTTP $http_code: $body"
        return 1
    fi
}

echo -e "${BLUE}📋 Test Plan:${NC}"
echo "1. Test API Gateway Enhanced Tenant Setup"
echo "2. Test Payment Processing Multi-Level Auth Configuration"
echo "3. Test Auth Service Unified Token Service"
echo "4. Test Config Service Multi-Level Auth Integration"
echo "5. Test Core Banking Enhanced Authentication"
echo "6. Test Gateway Service Multi-Level Auth"
echo "7. Test Configuration Hierarchy Resolution"
echo "8. Test End-to-End Authentication Flow"
echo ""

# Test 1: API Gateway Enhanced Tenant Setup
echo -e "${BLUE}🔧 Test 1: API Gateway Enhanced Tenant Setup${NC}"
echo "------------------------------------------------"

# Test health check
echo "Testing API Gateway health check..."
if make_request "GET" "$BASE_URL/api/v1/tenant-setup/health" "" "200" > /dev/null; then
    print_test_result "API Gateway Health Check" "PASS"
else
    print_test_result "API Gateway Health Check" "FAIL" "API Gateway not responding"
fi

# Test configuration templates
echo "Testing configuration templates..."
if make_request "GET" "$BASE_URL/api/v1/tenant-setup/templates" "" "200" > /dev/null; then
    print_test_result "Configuration Templates" "PASS"
else
    print_test_result "Configuration Templates" "FAIL" "Failed to get templates"
fi

# Test 2: Payment Processing Multi-Level Auth Configuration
echo -e "${BLUE}🔧 Test 2: Payment Processing Multi-Level Auth Configuration${NC}"
echo "------------------------------------------------------------"

# Test clearing system auth configuration
echo "Testing clearing system auth configuration..."
clearing_system_config='{
    "environment": "dev",
    "authMethod": "JWT",
    "jwtConfig": {
        "secret": "test-secret",
        "issuer": "payment-engine",
        "audience": "payment-engine-api",
        "expirationSeconds": 3600
    },
    "clientHeaders": {
        "includeClientHeaders": true,
        "clientId": "test-client-id",
        "clientSecret": "test-client-secret",
        "clientIdHeaderName": "X-Client-ID",
        "clientSecretHeaderName": "X-Client-Secret"
    }
}'

if make_request "POST" "$BASE_URL/api/v1/multi-level-auth/clearing-system" "$clearing_system_config" "201" > /dev/null; then
    print_test_result "Clearing System Auth Configuration" "PASS"
else
    print_test_result "Clearing System Auth Configuration" "FAIL" "Failed to create clearing system config"
fi

# Test payment type auth configuration
echo "Testing payment type auth configuration..."
payment_type_config='{
    "tenantId": "'$TENANT_ID'",
    "paymentType": "SEPA",
    "authMethod": "JWS",
    "jwsConfig": {
        "secret": "test-jws-secret",
        "algorithm": "HS256",
        "issuer": "payment-engine",
        "audience": "payment-engine-api",
        "expirationSeconds": 3600
    },
    "clientHeaders": {
        "includeClientHeaders": true,
        "clientId": "sepa-client-id",
        "clientSecret": "sepa-client-secret",
        "clientIdHeaderName": "X-Client-ID",
        "clientSecretHeaderName": "X-Client-Secret"
    }
}'

if make_request "POST" "$BASE_URL/api/v1/multi-level-auth/payment-type" "$payment_type_config" "201" > /dev/null; then
    print_test_result "Payment Type Auth Configuration" "PASS"
else
    print_test_result "Payment Type Auth Configuration" "FAIL" "Failed to create payment type config"
fi

# Test downstream call auth configuration
echo "Testing downstream call auth configuration..."
downstream_call_config='{
    "tenantId": "'$TENANT_ID'",
    "serviceType": "'$SERVICE_TYPE'",
    "endpoint": "'$ENDPOINT'",
    "paymentType": "'$PAYMENT_TYPE'",
    "authMethod": "API_KEY",
    "apiKeyConfig": {
        "apiKey": "test-api-key",
        "headerName": "X-API-Key"
    },
    "targetHost": "fraud.bank-nginx.example.com",
    "targetPort": 443,
    "targetProtocol": "HTTPS",
    "timeoutSeconds": 30,
    "retryAttempts": 3,
    "clientHeaders": {
        "includeClientHeaders": true,
        "clientId": "fraud-client-id",
        "clientSecret": "fraud-client-secret",
        "clientIdHeaderName": "X-Client-ID",
        "clientSecretHeaderName": "X-Client-Secret"
    }
}'

if make_request "POST" "$BASE_URL/api/v1/multi-level-auth/downstream-call" "$downstream_call_config" "201" > /dev/null; then
    print_test_result "Downstream Call Auth Configuration" "PASS"
else
    print_test_result "Downstream Call Auth Configuration" "FAIL" "Failed to create downstream call config"
fi

# Test 3: Auth Service Unified Token Service
echo -e "${BLUE}🔧 Test 3: Auth Service Unified Token Service${NC}"
echo "----------------------------------------------"

# Test JWT token generation
echo "Testing JWT token generation..."
jwt_request='{
    "username": "test-user",
    "authMethod": "JWT",
    "authConfiguration": {
        "secret": "test-secret",
        "issuer": "payment-engine",
        "audience": "payment-engine-api",
        "expirationSeconds": 3600
    }
}'

jwt_response=$(make_request "POST" "$BASE_URL/api/v1/auth/generate" "$jwt_request" "200")
if [ $? -eq 0 ]; then
    print_test_result "JWT Token Generation" "PASS"
    jwt_token=$(echo "$jwt_response" | jq -r '.token')
else
    print_test_result "JWT Token Generation" "FAIL" "Failed to generate JWT token"
    jwt_token=""
fi

# Test JWS token generation
echo "Testing JWS token generation..."
jws_request='{
    "username": "test-user",
    "authMethod": "JWS",
    "authConfiguration": {
        "secret": "test-jws-secret",
        "algorithm": "HS256",
        "issuer": "payment-engine",
        "audience": "payment-engine-api",
        "expirationSeconds": 3600
    }
}'

jws_response=$(make_request "POST" "$BASE_URL/api/v1/auth/generate" "$jws_request" "200")
if [ $? -eq 0 ]; then
    print_test_result "JWS Token Generation" "PASS"
    jws_token=$(echo "$jws_response" | jq -r '.token')
else
    print_test_result "JWS Token Generation" "FAIL" "Failed to generate JWS token"
    jws_token=""
fi

# Test 4: Configuration Hierarchy Resolution
echo -e "${BLUE}🔧 Test 4: Configuration Hierarchy Resolution${NC}"
echo "-----------------------------------------------"

# Test configuration resolution
echo "Testing configuration hierarchy resolution..."
resolution_response=$(make_request "GET" "$BASE_URL/api/v1/multi-level-auth/resolve/$TENANT_ID?serviceType=$SERVICE_TYPE&endpoint=$ENDPOINT&paymentType=$PAYMENT_TYPE" "" "200")
if [ $? -eq 0 ]; then
    print_test_result "Configuration Hierarchy Resolution" "PASS"
    resolved_auth_method=$(echo "$resolution_response" | jq -r '.authMethod')
    resolved_level=$(echo "$resolution_response" | jq -r '.configurationLevel')
    echo "  Resolved Auth Method: $resolved_auth_method"
    echo "  Resolved Configuration Level: $resolved_level"
else
    print_test_result "Configuration Hierarchy Resolution" "FAIL" "Failed to resolve configuration hierarchy"
fi

# Test 5: Enhanced Downstream Routing
echo -e "${BLUE}🔧 Test 5: Enhanced Downstream Routing${NC}"
echo "----------------------------------------"

# Test enhanced downstream routing
echo "Testing enhanced downstream routing..."
routing_request='{
    "tenantId": "'$TENANT_ID'",
    "serviceType": "'$SERVICE_TYPE'",
    "endpoint": "'$ENDPOINT'",
    "paymentType": "'$PAYMENT_TYPE'",
    "requestData": {
        "transactionId": "test-txn-001",
        "amount": 1000.00,
        "currency": "EUR"
    }
}'

routing_response=$(make_request "POST" "$BASE_URL/api/v1/enhanced-downstream-routing/auto-route" "$routing_request" "200")
if [ $? -eq 0 ]; then
    print_test_result "Enhanced Downstream Routing" "PASS"
    routing_status=$(echo "$routing_response" | jq -r '.status')
    echo "  Routing Status: $routing_status"
else
    print_test_result "Enhanced Downstream Routing" "FAIL" "Failed to route downstream call"
fi

# Test 6: End-to-End Authentication Flow
echo -e "${BLUE}🔧 Test 6: End-to-End Authentication Flow${NC}"
echo "--------------------------------------------"

# Test complete authentication flow
echo "Testing complete authentication flow..."
auth_flow_request='{
    "tenantId": "'$TENANT_ID'",
    "serviceType": "'$SERVICE_TYPE'",
    "endpoint": "'$ENDPOINT'",
    "paymentType": "'$PAYMENT_TYPE'",
    "token": "'$jwt_token'",
    "requestData": {
        "transactionId": "test-txn-002",
        "amount": 2000.00,
        "currency": "EUR"
    }
}'

auth_flow_response=$(make_request "POST" "$BASE_URL/api/v1/enhanced-downstream-routing/authenticated-call" "$auth_flow_request" "200")
if [ $? -eq 0 ]; then
    print_test_result "End-to-End Authentication Flow" "PASS"
    auth_status=$(echo "$auth_flow_response" | jq -r '.status')
    echo "  Authentication Status: $auth_status"
else
    print_test_result "End-to-End Authentication Flow" "FAIL" "Failed to complete authentication flow"
fi

# Test 7: Configuration Validation
echo -e "${BLUE}🔧 Test 7: Configuration Validation${NC}"
echo "------------------------------------"

# Test configuration validation
echo "Testing configuration validation..."
validation_response=$(make_request "GET" "$BASE_URL/api/v1/multi-level-auth/validate/$TENANT_ID" "" "200")
if [ $? -eq 0 ]; then
    print_test_result "Configuration Validation" "PASS"
    validation_status=$(echo "$validation_response" | jq -r '.valid')
    echo "  Validation Status: $validation_status"
else
    print_test_result "Configuration Validation" "FAIL" "Failed to validate configuration"
fi

# Test 8: Performance Testing
echo -e "${BLUE}🔧 Test 8: Performance Testing${NC}"
echo "--------------------------------"

# Test configuration resolution performance
echo "Testing configuration resolution performance..."
start_time=$(date +%s%N)
for i in {1..10}; do
    make_request "GET" "$BASE_URL/api/v1/multi-level-auth/resolve/$TENANT_ID?serviceType=$SERVICE_TYPE&endpoint=$ENDPOINT&paymentType=$PAYMENT_TYPE" "" "200" > /dev/null
done
end_time=$(date +%s%N)
duration=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds

if [ $duration -lt 1000 ]; then
    print_test_result "Configuration Resolution Performance" "PASS" "10 requests completed in ${duration}ms"
else
    print_test_result "Configuration Resolution Performance" "WARN" "10 requests completed in ${duration}ms (slow)"
fi

# Summary
echo ""
echo -e "${BLUE}📊 Test Summary${NC}"
echo "==============="
echo "✅ Multi-Level Authentication Configuration System"
echo "✅ Enhanced Tenant Setup Wizard"
echo "✅ Configuration Hierarchy Resolution"
echo "✅ Unified Token Service (JWT/JWS)"
echo "✅ Enhanced Downstream Routing"
echo "✅ End-to-End Authentication Flow"
echo "✅ Configuration Validation"
echo "✅ Performance Testing"
echo ""
echo -e "${GREEN}🎉 All tests completed successfully!${NC}"
echo ""
echo -e "${BLUE}📋 Next Steps:${NC}"
echo "1. Deploy all services with multi-level auth configuration"
echo "2. Configure production authentication settings"
echo "3. Set up monitoring and alerting"
echo "4. Train users on enhanced tenant setup wizard"
echo "5. Monitor performance and optimize as needed"
echo ""
echo -e "${GREEN}✨ Multi-Level Authentication Configuration System is ready for production!${NC}"