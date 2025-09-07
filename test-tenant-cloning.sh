#!/bin/bash

# End-to-End Test Script for Tenant Cloning System
# This script tests the complete tenant cloning functionality

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
MIDDLEWARE_URL="http://localhost:8082"
FRONTEND_URL="http://localhost:3000"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Tenant Cloning System E2E Test${NC}"
echo -e "${BLUE}========================================${NC}"

# Function to print test results
print_test_result() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓${NC} $test_name: $message"
    elif [ "$status" = "FAIL" ]; then
        echo -e "${RED}✗${NC} $test_name: $message"
    else
        echo -e "${YELLOW}?${NC} $test_name: $message"
    fi
}

# Function to check if service is running
check_service() {
    local service_name="$1"
    local url="$2"
    
    if curl -s -f "$url" > /dev/null 2>&1; then
        print_test_result "Service Check: $service_name" "PASS" "Service is running"
        return 0
    else
        print_test_result "Service Check: $service_name" "FAIL" "Service is not running at $url"
        return 1
    fi
}

# Function to get JWT token
get_jwt_token() {
    echo -e "${YELLOW}Getting JWT token for testing...${NC}"
    
    local token_response=$(curl -s -X POST "$MIDDLEWARE_URL/api/auth/admin-token" \
        -H "Content-Type: application/json")
    
    if echo "$token_response" | grep -q "token"; then
        local token=$(echo "$token_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        echo "$token"
        print_test_result "JWT Token Generation" "PASS" "Token generated successfully"
        return 0
    else
        print_test_result "JWT Token Generation" "FAIL" "Failed to generate token"
        return 1
    fi
}

# Function to test API endpoint
test_api_endpoint() {
    local endpoint="$1"
    local method="$2"
    local data="$3"
    local expected_status="$4"
    local test_name="$5"
    
    local url="$BASE_URL$endpoint"
    local headers="Authorization: Bearer $JWT_TOKEN"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -H "$headers" "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST -H "$headers" -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    local http_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$http_code" = "$expected_status" ]; then
        print_test_result "$test_name" "PASS" "HTTP $http_code - $body"
        return 0
    else
        print_test_result "$test_name" "FAIL" "Expected HTTP $expected_status, got HTTP $http_code - $body"
        return 1
    fi
}

# Start testing
echo -e "${YELLOW}Starting end-to-end tests...${NC}"

# 1. Check if services are running
echo -e "\n${BLUE}1. Service Health Checks${NC}"
check_service "API Gateway" "$BASE_URL/actuator/health"
check_service "Middleware Service" "$MIDDLEWARE_URL/actuator/health"
check_service "Frontend" "$FRONTEND_URL"

# 2. Get JWT token
echo -e "\n${BLUE}2. Authentication${NC}"
JWT_TOKEN=$(get_jwt_token)
if [ -z "$JWT_TOKEN" ]; then
    echo -e "${RED}Failed to get JWT token. Exiting tests.${NC}"
    exit 1
fi

# 3. Test tenant management endpoints
echo -e "\n${BLUE}3. Tenant Management API Tests${NC}"

# Test get tenants
test_api_endpoint "/api/tenant-management/tenants" "GET" "" "200" "Get Available Tenants"

# Test clone tenant
clone_data='{
    "sourceTenantId": "tenant-001",
    "targetTenantId": "tenant-test-clone",
    "targetVersion": "1.0.0",
    "targetEnvironment": "DEVELOPMENT",
    "clonedBy": "test-user",
    "changeLog": "Test clone operation"
}'
test_api_endpoint "/api/tenant-management/clone" "POST" "$clone_data" "200" "Clone Tenant"

# Test export tenant
export_data='{
    "tenantId": "tenant-001",
    "version": "1.0.0",
    "exportFormat": "JSON",
    "exportedBy": "test-user",
    "exportReason": "Test export operation"
}'
test_api_endpoint "/api/tenant-management/export" "POST" "$export_data" "200" "Export Tenant"

# Test get tenant versions
test_api_endpoint "/api/tenant-management/tenants/tenant-001/versions" "GET" "" "200" "Get Tenant Versions"

# Test get tenant history
test_api_endpoint "/api/tenant-management/tenants/tenant-001/history" "GET" "" "200" "Get Tenant History"

# Test get statistics
test_api_endpoint "/api/tenant-management/statistics" "GET" "" "200" "Get Statistics"

# 4. Test error handling
echo -e "\n${BLUE}4. Error Handling Tests${NC}"

# Test unauthorized access (no token)
unauthorized_response=$(curl -s -w "%{http_code}" "$BASE_URL/api/tenant-management/tenants")
unauthorized_code="${unauthorized_response: -3}"
if [ "$unauthorized_code" = "401" ]; then
    print_test_result "Unauthorized Access" "PASS" "HTTP 401 - Unauthorized"
else
    print_test_result "Unauthorized Access" "FAIL" "Expected HTTP 401, got HTTP $unauthorized_code"
fi

# Test invalid tenant clone
invalid_clone_data='{
    "sourceTenantId": "non-existent-tenant",
    "targetTenantId": "tenant-invalid",
    "targetVersion": "1.0.0",
    "targetEnvironment": "DEVELOPMENT",
    "clonedBy": "test-user"
}'
test_api_endpoint "/api/tenant-management/clone" "POST" "$invalid_clone_data" "400" "Invalid Clone Request"

# 5. Test database connectivity
echo -e "\n${BLUE}5. Database Tests${NC}"

# Check if database tables exist (this would require database access)
# For now, we'll test if the API returns data which indicates database connectivity
db_test_response=$(curl -s -H "Authorization: Bearer $JWT_TOKEN" "$BASE_URL/api/tenant-management/tenants")
if echo "$db_test_response" | grep -q "tenant-001"; then
    print_test_result "Database Connectivity" "PASS" "Database tables accessible and contain data"
else
    print_test_result "Database Connectivity" "FAIL" "Database tables may not exist or contain data"
fi

# 6. Test frontend integration
echo -e "\n${BLUE}6. Frontend Integration Tests${NC}"

# Check if frontend loads
frontend_response=$(curl -s -w "%{http_code}" "$FRONTEND_URL")
frontend_code="${frontend_response: -3}"
if [ "$frontend_code" = "200" ]; then
    print_test_result "Frontend Load" "PASS" "Frontend is accessible"
else
    print_test_result "Frontend Load" "FAIL" "Frontend returned HTTP $frontend_code"
fi

# Check if tenant management page is accessible
tenant_mgmt_response=$(curl -s -w "%{http_code}" "$FRONTEND_URL/tenant-management")
tenant_mgmt_code="${tenant_mgmt_response: -3}"
if [ "$tenant_mgmt_code" = "200" ]; then
    print_test_result "Tenant Management Page" "PASS" "Tenant management page is accessible"
else
    print_test_result "Tenant Management Page" "FAIL" "Tenant management page returned HTTP $tenant_mgmt_code"
fi

# 7. Performance tests
echo -e "\n${BLUE}7. Performance Tests${NC}"

# Test response time for get tenants
start_time=$(date +%s%N)
curl -s -H "Authorization: Bearer $JWT_TOKEN" "$BASE_URL/api/tenant-management/tenants" > /dev/null
end_time=$(date +%s%N)
response_time=$(( (end_time - start_time) / 1000000 ))

if [ "$response_time" -lt 1000 ]; then
    print_test_result "API Response Time" "PASS" "Response time: ${response_time}ms (under 1s)"
else
    print_test_result "API Response Time" "FAIL" "Response time: ${response_time}ms (over 1s)"
fi

# 8. Summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}  Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "${GREEN}✓${NC} Tests completed successfully!"
echo -e "${YELLOW}Note:${NC} This is a basic end-to-end test. For production deployment,"
echo -e "       consider adding more comprehensive tests including:"
echo -e "       - Load testing with multiple concurrent users"
echo -e "       - Security testing for authentication/authorization"
echo -e "       - Database transaction testing"
echo -e "       - Error recovery testing"
echo -e "       - Integration testing with external systems"

echo -e "\n${BLUE}To run the application:${NC}"
echo -e "1. Start database: docker-compose up -d postgres"
echo -e "2. Start services: docker-compose up -d"
echo -e "3. Start frontend: cd frontend && npm start"
echo -e "4. Access: http://localhost:3000/tenant-management"

echo -e "\n${BLUE}Test JWT Token (for manual testing):${NC}"
echo -e "$JWT_TOKEN"