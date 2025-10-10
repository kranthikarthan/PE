#!/bin/bash

# Wait for services to be ready script
# Waits for all infrastructure services to be healthy before proceeding

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

# Function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local host=$2
    local port=$3
    local max_attempts=${4:-30}
    local attempt=1

    print_status "Waiting for $service_name to be ready on $host:$port..."

    while [ $attempt -le $max_attempts ]; do
        if nc -z "$host" "$port" 2>/dev/null; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts: $service_name not ready yet, waiting 5 seconds..."
        sleep 5
        attempt=$((attempt + 1))
    done

    print_error "$service_name failed to start within $((max_attempts * 5)) seconds"
    return 1
}

# Function to wait for HTTP service to be ready
wait_for_http_service() {
    local service_name=$1
    local url=$2
    local max_attempts=${3:-30}
    local attempt=1

    print_status "Waiting for $service_name to be ready at $url..."

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" >/dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts: $service_name not ready yet, waiting 5 seconds..."
        sleep 5
        attempt=$((attempt + 1))
    done

    print_error "$service_name failed to start within $((max_attempts * 5)) seconds"
    return 1
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
if ! command_exists nc; then
    print_error "netcat (nc) is required but not installed"
    exit 1
fi

if ! command_exists curl; then
    print_error "curl is required but not installed"
    exit 1
fi

print_status "=================================================================="
print_status "           Payment Engine - Wait for Services Script            "
print_status "=================================================================="
echo ""

# Wait for PostgreSQL
print_status "Checking PostgreSQL..."
wait_for_service "PostgreSQL" "localhost" "5432" 30

# Wait for Redis
print_status "Checking Redis..."
wait_for_service "Redis" "localhost" "6379" 30

# Wait for Zookeeper
print_status "Checking Zookeeper..."
wait_for_service "Zookeeper" "localhost" "2181" 30

# Wait for Kafka
print_status "Checking Kafka..."
wait_for_service "Kafka" "localhost" "9092" 30

# Wait for Elasticsearch
print_status "Checking Elasticsearch..."
wait_for_http_service "Elasticsearch" "http://localhost:9200" 30

# Additional health checks
print_status "Performing additional health checks..."

# Check PostgreSQL is accepting connections
print_status "Verifying PostgreSQL connection..."
if pg_isready -h localhost -p 5432 -U payment_user >/dev/null 2>&1; then
    print_success "PostgreSQL is accepting connections"
else
    print_warning "PostgreSQL connection check failed, but service is running"
fi

# Check Redis is responding
print_status "Verifying Redis connection..."
if redis-cli -h localhost -p 6379 ping 2>/dev/null | grep -q PONG; then
    print_success "Redis is responding to commands"
else
    print_warning "Redis command check failed, but service is running"
fi

# Check Elasticsearch cluster health
print_status "Verifying Elasticsearch health..."
if curl -f -s "http://localhost:9200/_cluster/health" | grep -q '"status":"green\|yellow"'; then
    print_success "Elasticsearch cluster is healthy"
else
    print_warning "Elasticsearch health check failed, but service is running"
fi

print_success "=================================================================="
print_success "All infrastructure services are ready!"
print_success "You can now start the application services."
print_success "=================================================================="

# Display service URLs
echo ""
print_status "Service URLs:"
echo "  PostgreSQL:      localhost:5432 (payment_user/payment_pass)"
echo "  Redis:           localhost:6379"
echo "  Kafka:           localhost:9092"
echo "  Zookeeper:       localhost:2181"
echo "  Elasticsearch:   http://localhost:9200"
echo "  Kibana:          http://localhost:5601"
echo ""

exit 0