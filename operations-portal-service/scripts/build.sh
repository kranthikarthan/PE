#!/bin/bash

# Build script for React Operations Portal
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="operations-portal"
IMAGE_TAG=${1:-latest}
REGISTRY=${2:-""}

echo -e "${GREEN}Starting build of React Operations Portal...${NC}"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    print_error "Docker is not running"
    exit 1
fi

# Build the Docker image
print_status "Building Docker image..."
docker build -t $IMAGE_NAME:$IMAGE_TAG .

# Tag for registry if provided
if [ ! -z "$REGISTRY" ]; then
    print_status "Tagging image for registry..."
    docker tag $IMAGE_NAME:$IMAGE_TAG $REGISTRY/$IMAGE_NAME:$IMAGE_TAG
fi

# Run security scan
print_status "Running security scan..."
if command -v trivy &> /dev/null; then
    trivy image $IMAGE_NAME:$IMAGE_TAG
else
    print_warning "Trivy not found, skipping security scan"
fi

# Run tests in container
print_status "Running tests in container..."
docker run --rm $IMAGE_NAME:$IMAGE_TAG npm test -- --coverage --watchAll=false

# Display image information
print_status "Build completed successfully!"
print_status "Image: $IMAGE_NAME:$IMAGE_TAG"
if [ ! -z "$REGISTRY" ]; then
    print_status "Registry image: $REGISTRY/$IMAGE_NAME:$IMAGE_TAG"
fi

# Display useful commands
echo -e "${GREEN}Useful commands:${NC}"
echo "  docker run -p 8080:8080 $IMAGE_NAME:$IMAGE_TAG"
echo "  docker push $REGISTRY/$IMAGE_NAME:$IMAGE_TAG"
echo "  docker images | grep $IMAGE_NAME"
