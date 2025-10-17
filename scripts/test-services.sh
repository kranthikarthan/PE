#!/bin/bash

# Payments Engine Service Health Test Script
# This script tests all services to ensure they are working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
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

# Function to test HTTP endpoint
test_endpoint() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}
    
    print_status "Testing $service_name at $url"
    
    if response=$(curl -s -w "%{http_code}" -o /dev/null "$url" 2>/dev/null); then
        if [ "$response" = "$expected_status" ]; then
            print_success "$service_name is healthy (HTTP $response)"
            return 0
        else
            print_error "$service_name returned HTTP $response (expected $expected_status)"
            return 1
        fi
    else
        print_error "$service_name is not responding"
        return 1
    fi
}

# Function to test service health
test_service_health() {
    local service_name=$1
    local port=$2
    
    test_endpoint "$service_name" "http://localhost:$port/actuator/health"
}

# Function to test service info
test_service_info() {
    local service_name=$1
    local port=$2
    
    test_endpoint "$service_name" "http://localhost:$port/actuator/info"
}

echo "🧪 Testing Payments Engine Services..."
echo "======================================"

# Test infrastructure services
print_status "Testing infrastructure services..."

# Test PostgreSQL
print_status "Testing PostgreSQL..."
if docker-compose exec postgres pg_isready -U payments_user -d payments_engine > /dev/null 2>&1; then
    print_success "PostgreSQL is healthy"
else
    print_error "PostgreSQL is not responding"
    exit 1
fi

# Test Redis
print_status "Testing Redis..."
if docker-compose exec redis redis-cli ping > /dev/null 2>&1; then
    print_success "Redis is healthy"
else
    print_error "Redis is not responding"
    exit 1
fi

# Test Kafka
print_status "Testing Kafka..."
if docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    print_success "Kafka is healthy"
else
    print_error "Kafka is not responding"
    exit 1
fi

echo ""
print_status "Testing Payments Engine services..."

# Test all services
services=(
    "Payment Initiation Service:8081"
    "Validation Service:8082"
    "Account Adapter Service:8083"
    "Routing Service:8084"
    "Transaction Processing Service:8085"
    "Saga Orchestrator Service:8086"
)

failed_services=()

for service_info in "${services[@]}"; do
    IFS=':' read -r service_name port <<< "$service_info"
    
    if ! test_service_health "$service_name" "$port"; then
        failed_services+=("$service_name")
    fi
    
    # Test service info endpoint
    test_service_info "$service_name" "$port"
done

echo ""
print_status "Testing monitoring services..."

# Test Prometheus
test_endpoint "Prometheus" "http://localhost:9090" 200

# Test Grafana
test_endpoint "Grafana" "http://localhost:3000" 200

# Test Jaeger
test_endpoint "Jaeger" "http://localhost:16686" 200

echo ""
print_status "Testing API documentation endpoints..."

# Test Swagger UI endpoints
swagger_services=(
    "Payment Initiation Swagger:8081/swagger-ui.html"
    "Validation Swagger:8082/swagger-ui.html"
    "Account Adapter Swagger:8083/swagger-ui.html"
    "Routing Swagger:8084/swagger-ui.html"
    "Transaction Processing Swagger:8085/swagger-ui.html"
    "Saga Orchestrator Swagger:8086/swagger-ui.html"
)

for swagger_info in "${swagger_services[@]}"; do
    IFS=':' read -r service_name url <<< "$swagger_info"
    test_endpoint "$service_name" "http://localhost:$url" 200
done

echo ""
echo "📊 Test Results Summary"
echo "======================"

if [ ${#failed_services[@]} -eq 0 ]; then
    print_success "All services are healthy! 🎉"
    echo ""
    echo "✅ Infrastructure Services: PostgreSQL, Redis, Kafka"
    echo "✅ Payments Engine Services: All 6 services"
    echo "✅ Monitoring Services: Prometheus, Grafana, Jaeger"
    echo "✅ API Documentation: All Swagger UIs"
    echo ""
    echo "🚀 Payments Engine is ready for development!"
    exit 0
else
    print_error "Some services failed health checks:"
    for service in "${failed_services[@]}"; do
        print_error "  - $service"
    done
    echo ""
    print_warning "Check service logs with: docker-compose logs <service-name>"
    exit 1
fi
