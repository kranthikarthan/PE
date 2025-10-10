#!/bin/bash
# Air-Gapped Nexus Repository Setup
# This script sets up a local Nexus repository for offline package management

set -e

# Configuration
NEXUS_HOST="airgap-nexus.company.com"
NEXUS_PORT="8081"
NEXUS_DATA_DIR="/opt/nexus"
NEXUS_VERSION="3.45.0"

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

log_info "Setting up air-gapped Nexus repository..."

# Create nexus user
log_info "Creating nexus user..."
if ! id "nexus" &>/dev/null; then
    useradd -r -s /bin/false nexus
fi

# Create directories
log_info "Creating Nexus directories..."
mkdir -p $NEXUS_DATA_DIR
mkdir -p $NEXUS_DATA_DIR/sonatype-work
mkdir -p $NEXUS_DATA_DIR/nexus
chown -R nexus:nexus $NEXUS_DATA_DIR

# Download and install Nexus
log_info "Downloading Nexus OSS..."
cd /tmp
wget https://download.sonatype.com/nexus/3/nexus-${NEXUS_VERSION}-unix.tar.gz
tar -xzf nexus-${NEXUS_VERSION}-unix.tar.gz
mv nexus-${NEXUS_VERSION} $NEXUS_DATA_DIR/nexus
chown -R nexus:nexus $NEXUS_DATA_DIR/nexus

# Create systemd service
log_info "Creating systemd service..."
cat > /etc/systemd/system/nexus.service << EOF
[Unit]
Description=Nexus Repository Manager
After=network.target

[Service]
Type=forking
LimitNOFILE=65536
ExecStart=$NEXUS_DATA_DIR/nexus/bin/nexus start
ExecStop=$NEXUS_DATA_DIR/nexus/bin/nexus stop
User=nexus
Restart=on-abort
TimeoutSec=600

[Install]
WantedBy=multi-user.target
EOF

# Configure Nexus
log_info "Configuring Nexus..."
cat > $NEXUS_DATA_DIR/nexus/etc/nexus-default.properties << EOF
# Jetty section
application-port=8081
application-host=0.0.0.0
nexus-args=\${jetty.etc}/jetty.xml,\${jetty.etc}/jetty-http.xml,\${jetty.etc}/jetty-requestlog.xml
nexus-context-path=/

# Nexus section
nexus-edition=nexus-pro-edition
nexus-features=\
 nexus-pro-feature

# Nexus section
nexus-edition=nexus-oss-edition
nexus-features=\
 nexus-oss-feature
EOF

# Start Nexus
log_info "Starting Nexus..."
systemctl daemon-reload
systemctl enable nexus.service
systemctl start nexus.service

# Wait for Nexus to start
log_info "Waiting for Nexus to start..."
sleep 60

# Get admin password
ADMIN_PASSWORD=$(cat $NEXUS_DATA_DIR/sonatype-work/nexus3/admin.password 2>/dev/null || echo "admin123")

log_info "Nexus admin password: $ADMIN_PASSWORD"

# Create repository configuration script
log_info "Creating repository configuration script..."
cat > /opt/nexus/configure-repositories.sh << EOF
#!/bin/bash
# Script to configure Nexus repositories for air-gapped environment

set -e

NEXUS_URL="http://$NEXUS_HOST:$NEXUS_PORT"
ADMIN_USER="admin"
ADMIN_PASS="$ADMIN_PASSWORD"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m \$1"
}

# Function to create repository
create_repository() {
    local repo_name=\$1
    local repo_type=\$2
    local repo_format=\$3
    local remote_url=\$4
    
    log_info "Creating repository: \$repo_name"
    
    # Create repository JSON
    cat > /tmp/\${repo_name}.json << REPO_EOF
{
  "name": "\$repo_name",
  "type": "\$repo_type",
  "format": "\$repo_format",
  "url": "\$remote_url",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "proxy": {
    "remoteUrl": "\$remote_url",
    "contentMaxAge": 1440,
    "metadataMaxAge": 1440
  },
  "negativeCache": {
    "enabled": true,
    "timeToLive": 1440
  },
  "httpClient": {
    "blocked": false,
    "autoBlock": true,
    "connection": {
      "useTrustStore": false
    }
  }
}
REPO_EOF

    # Create repository via REST API
    curl -u "\$ADMIN_USER:\$ADMIN_PASS" \
         -X POST \
         -H "Content-Type: application/json" \
         -d @/tmp/\${repo_name}.json \
         "\$NEXUS_URL/service/rest/v1/repositories/\$repo_format/proxy"
    
    rm /tmp/\${repo_name}.json
}

# Create Maven repositories
create_repository "maven-central" "proxy" "maven2" "https://repo1.maven.org/maven2/"
create_repository "maven-spring" "proxy" "maven2" "https://repo.spring.io/release/"
create_repository "maven-spring-milestone" "proxy" "maven2" "https://repo.spring.io/milestone/"

# Create NPM repositories
create_repository "npm-registry" "proxy" "npm" "https://registry.npmjs.org/"

# Create Docker repositories
create_repository "docker-hub" "proxy" "docker" "https://registry-1.docker.io/"

# Create group repositories
log_info "Creating group repositories..."

# Maven group
cat > /tmp/maven-group.json << GROUP_EOF
{
  "name": "maven-public",
  "type": "group",
  "format": "maven2",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "group": {
    "memberNames": ["maven-central", "maven-spring", "maven-spring-milestone"]
  }
}
GROUP_EOF

curl -u "\$ADMIN_USER:\$ADMIN_PASS" \
     -X POST \
     -H "Content-Type: application/json" \
     -d @/tmp/maven-group.json \
     "\$NEXUS_URL/service/rest/v1/repositories/maven2/group"

# NPM group
cat > /tmp/npm-group.json << GROUP_EOF
{
  "name": "npm-proxy",
  "type": "group",
  "format": "npm",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "group": {
    "memberNames": ["npm-registry"]
  }
}
GROUP_EOF

curl -u "\$ADMIN_USER:\$ADMIN_PASS" \
     -X POST \
     -H "Content-Type: application/json" \
     -d @/tmp/npm-group.json \
     "\$NEXUS_URL/service/rest/v1/repositories/npm/group"

# Docker group
cat > /tmp/docker-group.json << GROUP_EOF
{
  "name": "docker-proxy",
  "type": "group",
  "format": "docker",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "group": {
    "memberNames": ["docker-hub"]
  }
}
GROUP_EOF

curl -u "\$ADMIN_USER:\$ADMIN_PASS" \
     -X POST \
     -H "Content-Type: application/json" \
     -d @/tmp/docker-group.json \
     "\$NEXUS_URL/service/rest/v1/repositories/docker/group"

# Clean up
rm -f /tmp/*.json

log_info "Repository configuration completed!"
log_info "Maven repository: http://$NEXUS_HOST:$NEXUS_PORT/repository/maven-public/"
log_info "NPM repository: http://$NEXUS_HOST:$NEXUS_PORT/repository/npm-proxy/"
log_info "Docker repository: http://$NEXUS_HOST:$NEXUS_PORT/repository/docker-proxy/"
EOF

chmod +x /opt/nexus/configure-repositories.sh

# Create package download script
log_info "Creating package download script..."
cat > /opt/nexus/download-packages.sh << 'EOF'
#!/bin/bash
# Script to download packages for offline use

set -e

NEXUS_URL="http://airgap-nexus.company.com:8081"
DOWNLOAD_DIR="/opt/nexus/offline-packages"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

# Create download directory
mkdir -p $DOWNLOAD_DIR

# Download Maven dependencies
log_info "Downloading Maven dependencies..."
cd /workspace/services
mvn dependency:go-offline -Dmaven.repo.local=$DOWNLOAD_DIR/maven
mvn dependency:copy-dependencies -DoutputDirectory=$DOWNLOAD_DIR/maven/lib

# Download NPM packages
log_info "Downloading NPM packages..."
cd /workspace/frontend
npm config set registry $NEXUS_URL/repository/npm-proxy/
npm install --package-lock-only
npm pack --pack-destination $DOWNLOAD_DIR/npm

log_info "Package download completed!"
log_info "Packages saved to: $DOWNLOAD_DIR"
EOF

chmod +x /opt/nexus/download-packages.sh

log_info "Nexus setup completed!"
log_info "Nexus URL: http://$NEXUS_HOST:$NEXUS_PORT"
log_info "Admin user: admin"
log_info "Admin password: $ADMIN_PASSWORD"
log_info "Data directory: $NEXUS_DATA_DIR"

echo ""
log_info "Next steps:"
echo "1. Access Nexus UI at http://$NEXUS_HOST:$NEXUS_PORT"
echo "2. Login with admin/$ADMIN_PASSWORD"
echo "3. Run /opt/nexus/configure-repositories.sh to configure repositories"
echo "4. Run /opt/nexus/download-packages.sh to download packages"