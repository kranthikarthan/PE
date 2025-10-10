#!/bin/bash
# Air-Gapped Container Registry Mirror Setup
# This script sets up a local container registry mirror for air-gapped environments

set -e

# Configuration
REGISTRY_HOST="airgap-registry.company.com"
REGISTRY_PORT="5000"
REGISTRY_DATA_DIR="/opt/registry/data"
REGISTRY_CONFIG_DIR="/opt/registry/config"
REGISTRY_CERTS_DIR="/opt/registry/certs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   log_error "This script must be run as root"
   exit 1
fi

log_info "Setting up air-gapped container registry mirror..."

# Create directories
log_info "Creating registry directories..."
mkdir -p $REGISTRY_DATA_DIR
mkdir -p $REGISTRY_CONFIG_DIR
mkdir -p $REGISTRY_CERTS_DIR

# Generate self-signed certificates
log_info "Generating self-signed certificates..."
openssl req -newkey rsa:4096 -nodes -keyout $REGISTRY_CERTS_DIR/domain.key \
    -x509 -days 365 -out $REGISTRY_CERTS_DIR/domain.crt \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=$REGISTRY_HOST"

# Create registry configuration
log_info "Creating registry configuration..."
cat > $REGISTRY_CONFIG_DIR/config.yml << EOF
version: 0.1
log:
  level: info
  fields:
    service: registry
storage:
  filesystem:
    rootdirectory: $REGISTRY_DATA_DIR
  cache:
    blobdescriptor: inmemory
  delete:
    enabled: true
http:
  addr: :$REGISTRY_PORT
  headers:
    X-Content-Type-Options: [nosniff]
  tls:
    certificate: $REGISTRY_CERTS_DIR/domain.crt
    key: $REGISTRY_CERTS_DIR/domain.key
health:
  storagedriver:
    enabled: true
    interval: 10s
    threshold: 3
EOF

# Create Docker Compose file for registry
log_info "Creating Docker Compose configuration..."
cat > /opt/registry/docker-compose.yml << EOF
version: '3.8'

services:
  registry:
    image: registry:2
    container_name: airgap-registry
    restart: always
    ports:
      - "$REGISTRY_PORT:$REGISTRY_PORT"
    volumes:
      - $REGISTRY_DATA_DIR:/var/lib/registry
      - $REGISTRY_CONFIG_DIR:/etc/docker/registry
      - $REGISTRY_CERTS_DIR:/certs
    environment:
      - REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt
      - REGISTRY_HTTP_TLS_KEY=/certs/domain.key
    networks:
      - registry-network

  registry-ui:
    image: joxit/docker-registry-ui:latest
    container_name: registry-ui
    restart: always
    ports:
      - "8080:80"
    environment:
      - REGISTRY_TITLE=Air-Gapped Registry
      - REGISTRY_URL=http://registry:$REGISTRY_PORT
      - DELETE_IMAGES=true
      - SHOW_CONTENT_DIGEST=true
    depends_on:
      - registry
    networks:
      - registry-network

networks:
  registry-network:
    driver: bridge
EOF

# Start registry
log_info "Starting container registry..."
cd /opt/registry
docker-compose up -d

# Wait for registry to be ready
log_info "Waiting for registry to be ready..."
sleep 10

# Test registry
log_info "Testing registry..."
if curl -k -s https://$REGISTRY_HOST:$REGISTRY_PORT/v2/ > /dev/null; then
    log_info "Registry is running successfully!"
else
    log_error "Registry failed to start"
    exit 1
fi

# Create systemd service for registry
log_info "Creating systemd service..."
cat > /etc/systemd/system/airgap-registry.service << EOF
[Unit]
Description=Air-Gapped Container Registry
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/registry
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
systemctl daemon-reload
systemctl enable airgap-registry.service

# Create registry mirror script
log_info "Creating registry mirror script..."
cat > /opt/registry/mirror-registry.sh << 'EOF'
#!/bin/bash
# Script to mirror images from external registry to air-gapped registry

set -e

AIRGAP_REGISTRY="airgap-registry.company.com:5000"
EXTERNAL_REGISTRY="registry-1.docker.io"

# List of images to mirror
IMAGES=(
    "openjdk:17-jdk-slim"
    "node:18-alpine"
    "postgres:15-alpine"
    "redis:7-alpine"
    "nginx:alpine"
    "prom/prometheus:latest"
    "grafana/grafana:latest"
    "jaegertracing/all-in-one:latest"
    "elasticsearch:8.11.0"
    "kibana:8.11.0"
    "logstash:8.11.0"
    "confluentinc/cp-kafka:latest"
    "confluentinc/cp-zookeeper:latest"
    "istio/proxyv2:1.19.0"
    "istio/pilot:1.19.0"
)

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

log_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

mirror_image() {
    local image=$1
    local source_image="$EXTERNAL_REGISTRY/$image"
    local target_image="$AIRGAP_REGISTRY/$image"
    
    log_info "Mirroring $image..."
    
    # Pull image from external registry
    docker pull $source_image
    
    # Tag for air-gapped registry
    docker tag $source_image $target_image
    
    # Push to air-gapped registry
    docker push $target_image
    
    # Clean up
    docker rmi $source_image $target_image
    
    log_info "Successfully mirrored $image"
}

# Mirror all images
for image in "${IMAGES[@]}"; do
    mirror_image $image
done

log_info "Registry mirroring completed!"
EOF

chmod +x /opt/registry/mirror-registry.sh

# Create cleanup script
log_info "Creating cleanup script..."
cat > /opt/registry/cleanup-registry.sh << 'EOF'
#!/bin/bash
# Script to cleanup old images from air-gapped registry

set -e

REGISTRY_HOST="airgap-registry.company.com"
REGISTRY_PORT="5000"
REGISTRY_URL="https://$REGISTRY_HOST:$REGISTRY_PORT"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

# Get list of repositories
repositories=$(curl -k -s "$REGISTRY_URL/v2/_catalog" | jq -r '.repositories[]')

for repo in $repositories; do
    log_info "Cleaning up repository: $repo"
    
    # Get list of tags
    tags=$(curl -k -s "$REGISTRY_URL/v2/$repo/tags/list" | jq -r '.tags[]' 2>/dev/null || echo "")
    
    if [ -n "$tags" ]; then
        # Keep only the latest 5 tags
        echo "$tags" | tail -n +6 | while read tag; do
            if [ -n "$tag" ]; then
                log_info "Deleting old tag: $repo:$tag"
                # Get manifest digest
                digest=$(curl -k -s -I "$REGISTRY_URL/v2/$repo/manifests/$tag" | grep -i "docker-content-digest" | cut -d' ' -f2 | tr -d '\r')
                if [ -n "$digest" ]; then
                    curl -k -X DELETE "$REGISTRY_URL/v2/$repo/manifests/$digest"
                fi
            fi
        done
    fi
done

log_info "Registry cleanup completed!"
EOF

chmod +x /opt/registry/cleanup-registry.sh

# Create cron job for cleanup
log_info "Setting up cleanup cron job..."
(crontab -l 2>/dev/null; echo "0 2 * * 0 /opt/registry/cleanup-registry.sh") | crontab -

log_info "Air-gapped container registry setup completed!"
log_info "Registry URL: https://$REGISTRY_HOST:$REGISTRY_PORT"
log_info "Registry UI: http://$REGISTRY_HOST:8080"
log_info "Data directory: $REGISTRY_DATA_DIR"
log_info "Configuration: $REGISTRY_CONFIG_DIR"
log_info "Certificates: $REGISTRY_CERTS_DIR"

echo ""
log_info "Next steps:"
echo "1. Run /opt/registry/mirror-registry.sh to mirror base images"
echo "2. Configure Docker daemon to use this registry as mirror"
echo "3. Update application build scripts to use air-gapped registry"