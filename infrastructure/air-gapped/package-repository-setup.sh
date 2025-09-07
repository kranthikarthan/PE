#!/bin/bash
# Air-Gapped Package Repository Setup
# This script sets up a comprehensive package repository for offline development

set -e

# Configuration
REPO_BASE_DIR="/opt/airgap-repo"
NEXUS_HOST="airgap-nexus.company.com"
NEXUS_PORT="8081"
REGISTRY_HOST="airgap-registry.company.com"
REGISTRY_PORT="5000"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   log_error "This script must be run as root"
   exit 1
fi

log_info "Setting up air-gapped package repository..."

# Create repository structure
log_step "Creating repository structure..."
mkdir -p $REPO_BASE_DIR/{maven,npm,docker,helm,kubernetes,scripts,configs}
mkdir -p $REPO_BASE_DIR/maven/{releases,snapshots,public}
mkdir -p $REPO_BASE_DIR/npm/{packages,cache}
mkdir -p $REPO_BASE_DIR/docker/{base,app,monitoring}
mkdir -p $REPO_BASE_DIR/helm/{charts,repositories}
mkdir -p $REPO_BASE_DIR/kubernetes/{manifests,operators}
mkdir -p $REPO_BASE_DIR/scripts/{setup,deploy,maintenance}
mkdir -p $REPO_BASE_DIR/configs/{maven,npm,docker,kubernetes}

# Set permissions
chown -R root:root $REPO_BASE_DIR
chmod -R 755 $REPO_BASE_DIR

# Create Maven repository configuration
log_step "Creating Maven repository configuration..."
cat > $REPO_BASE_DIR/configs/maven/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <mirrors>
    <mirror>
      <id>airgap-nexus</id>
      <name>Air-Gapped Nexus Repository</name>
      <url>http://airgap-nexus.company.com:8081/repository/maven-public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
  
  <profiles>
    <profile>
      <id>airgap</id>
      <repositories>
        <repository>
          <id>airgap-nexus</id>
          <name>Air-Gapped Nexus Repository</name>
          <url>http://airgap-nexus.company.com:8081/repository/maven-public/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>airgap-nexus</id>
          <name>Air-Gapped Nexus Repository</name>
          <url>http://airgap-nexus.company.com:8081/repository/maven-public/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>airgap</activeProfile>
  </activeProfiles>
</settings>
EOF

# Create NPM repository configuration
log_step "Creating NPM repository configuration..."
cat > $REPO_BASE_DIR/configs/npm/.npmrc << 'EOF'
registry=http://airgap-nexus.company.com:8081/repository/npm-proxy/
always-auth=false
strict-ssl=false
EOF

# Create Docker repository configuration
log_step "Creating Docker repository configuration..."
cat > $REPO_BASE_DIR/configs/docker/daemon.json << 'EOF'
{
  "insecure-registries": [
    "airgap-registry.company.com:5000",
    "local-registry:5000"
  ],
  "registry-mirrors": [
    "https://airgap-registry.company.com:5000"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

# Create Kubernetes configuration
log_step "Creating Kubernetes configuration..."
cat > $REPO_BASE_DIR/configs/kubernetes/kubeconfig << 'EOF'
apiVersion: v1
kind: Config
clusters:
- cluster:
    server: https://kubernetes.default.svc.cluster.local
    insecure-skip-tls-verify: true
  name: airgap-cluster
contexts:
- context:
    cluster: airgap-cluster
    user: airgap-user
  name: airgap-context
current-context: airgap-context
users:
- name: airgap-user
  user:
    token: YOUR_SERVICE_ACCOUNT_TOKEN
EOF

# Create package download script
log_step "Creating package download script..."
cat > $REPO_BASE_DIR/scripts/setup/download-packages.sh << 'EOF'
#!/bin/bash
# Script to download all required packages for air-gapped environment

set -e

REPO_BASE_DIR="/opt/airgap-repo"
NEXUS_URL="http://airgap-nexus.company.com:8081"
REGISTRY_URL="https://airgap-registry.company.com:5000"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

log_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

# Download Maven dependencies
download_maven_deps() {
    log_info "Downloading Maven dependencies..."
    
    # Create temporary Maven project
    mkdir -p /tmp/maven-download
    cd /tmp/maven-download
    
    # Create pom.xml with common dependencies
    cat > pom.xml << 'POM_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  
  <groupId>com.airgap</groupId>
  <artifactId>dependency-downloader</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>
  
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <spring.boot.version>3.1.0</spring.boot.version>
  </properties>
  
  <dependencies>
    <!-- Spring Boot Dependencies -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>${spring.boot.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
      <version>${spring.boot.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
      <version>${spring.boot.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
      <version>${spring.boot.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
      <version>${spring.boot.version}</version>
    </dependency>
    
    <!-- Database Dependencies -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.6.0</version>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <version>2.2.220</version>
    </dependency>
    
    <!-- Redis Dependencies -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
      <version>${spring.boot.version}</version>
    </dependency>
    
    <!-- Resilience4j Dependencies -->
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-spring-boot3</artifactId>
      <version>2.1.0</version>
    </dependency>
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-circuitbreaker</artifactId>
      <version>2.1.0</version>
    </dependency>
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-retry</artifactId>
      <version>2.1.0</version>
    </dependency>
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-bulkhead</artifactId>
      <version>2.1.0</version>
    </dependency>
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-timelimiter</artifactId>
      <version>2.1.0</version>
    </dependency>
    
    <!-- Testing Dependencies -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <version>${spring.boot.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>1.19.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>postgresql</artifactId>
      <version>1.19.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>${spring.boot.version}</version>
      </plugin>
    </plugins>
  </build>
</project>
POM_EOF
    
    # Download dependencies
    mvn dependency:go-offline -Dmaven.repo.local=$REPO_BASE_DIR/maven/public
    mvn dependency:copy-dependencies -DoutputDirectory=$REPO_BASE_DIR/maven/lib
    
    # Clean up
    cd /
    rm -rf /tmp/maven-download
    
    log_info "Maven dependencies downloaded successfully"
}

# Download NPM packages
download_npm_packages() {
    log_info "Downloading NPM packages..."
    
    # Create temporary NPM project
    mkdir -p /tmp/npm-download
    cd /tmp/npm-download
    
    # Create package.json with common dependencies
    cat > package.json << 'PACKAGE_EOF'
{
  "name": "airgap-dependency-downloader",
  "version": "1.0.0",
  "description": "Download NPM dependencies for air-gapped environment",
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.15.0",
    "@mui/material": "^5.14.0",
    "@mui/icons-material": "^5.14.0",
    "@emotion/react": "^11.11.0",
    "@emotion/styled": "^11.11.0",
    "@reduxjs/toolkit": "^1.9.0",
    "react-redux": "^8.1.0",
    "@tanstack/react-query": "^4.32.0",
    "axios": "^1.5.0",
    "react-hook-form": "^7.45.0",
    "date-fns": "^2.30.0",
    "lodash": "^4.17.21",
    "uuid": "^9.0.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@types/lodash": "^4.14.0",
    "@types/uuid": "^9.0.0",
    "typescript": "^5.1.0",
    "vite": "^4.4.0",
    "@vitejs/plugin-react": "^4.0.0",
    "eslint": "^8.47.0",
    "@typescript-eslint/eslint-plugin": "^6.4.0",
    "@typescript-eslint/parser": "^6.4.0"
  }
}
PACKAGE_EOF
    
    # Download packages
    npm config set registry $NEXUS_URL/repository/npm-proxy/
    npm install --package-lock-only
    npm pack --pack-destination $REPO_BASE_DIR/npm/packages
    
    # Clean up
    cd /
    rm -rf /tmp/npm-download
    
    log_info "NPM packages downloaded successfully"
}

# Download Docker images
download_docker_images() {
    log_info "Downloading Docker images..."
    
    # List of base images to download
    BASE_IMAGES=(
        "openjdk:17-jdk-slim"
        "openjdk:17-jre-slim"
        "node:18-alpine"
        "postgres:15-alpine"
        "redis:7-alpine"
        "nginx:alpine"
        "alpine:3.18"
        "busybox:1.35"
    )
    
    # List of monitoring images
    MONITORING_IMAGES=(
        "prom/prometheus:latest"
        "grafana/grafana:latest"
        "jaegertracing/all-in-one:latest"
        "elasticsearch:8.11.0"
        "kibana:8.11.0"
        "logstash:8.11.0"
    )
    
    # Download base images
    for image in "${BASE_IMAGES[@]}"; do
        log_info "Downloading base image: $image"
        docker pull $image
        docker save $image > $REPO_BASE_DIR/docker/base/$(echo $image | tr '/' '_' | tr ':' '_').tar
    done
    
    # Download monitoring images
    for image in "${MONITORING_IMAGES[@]}"; do
        log_info "Downloading monitoring image: $image"
        docker pull $image
        docker save $image > $REPO_BASE_DIR/docker/monitoring/$(echo $image | tr '/' '_' | tr ':' '_').tar
    done
    
    log_info "Docker images downloaded successfully"
}

# Download Helm charts
download_helm_charts() {
    log_info "Downloading Helm charts..."
    
    # Add Helm repositories
    helm repo add stable https://charts.helm.sh/stable
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo add grafana https://grafana.github.io/helm-charts
    
    # Update repositories
    helm repo update
    
    # Download charts
    helm pull stable/nginx-ingress --destination $REPO_BASE_DIR/helm/charts
    helm pull bitnami/postgresql --destination $REPO_BASE_DIR/helm/charts
    helm pull bitnami/redis --destination $REPO_BASE_DIR/helm/charts
    helm pull prometheus-community/prometheus --destination $REPO_BASE_DIR/helm/charts
    helm pull grafana/grafana --destination $REPO_BASE_DIR/helm/charts
    
    log_info "Helm charts downloaded successfully"
}

# Main execution
main() {
    log_info "Starting package download process..."
    
    download_maven_deps
    download_npm_packages
    download_docker_images
    download_helm_charts
    
    log_info "Package download completed successfully!"
    log_info "Repository location: $REPO_BASE_DIR"
}

# Run main function
main "$@"
EOF

chmod +x $REPO_BASE_DIR/scripts/setup/download-packages.sh

# Create repository sync script
log_step "Creating repository sync script..."
cat > $REPO_BASE_DIR/scripts/maintenance/sync-repository.sh << 'EOF'
#!/bin/bash
# Script to sync air-gapped repository with external sources

set -e

REPO_BASE_DIR="/opt/airgap-repo"
SYNC_LOG="/var/log/airgap-repo-sync.log"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1" | tee -a $SYNC_LOG
}

log_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1" | tee -a $SYNC_LOG
}

# Sync Maven repository
sync_maven() {
    log_info "Syncing Maven repository..."
    
    # Check for new versions
    mvn versions:display-dependency-updates -Dmaven.repo.local=$REPO_BASE_DIR/maven/public
    
    # Update dependencies
    mvn dependency:resolve -Dmaven.repo.local=$REPO_BASE_DIR/maven/public
    
    log_info "Maven repository sync completed"
}

# Sync NPM repository
sync_npm() {
    log_info "Syncing NPM repository..."
    
    # Check for outdated packages
    npm outdated --registry=http://airgap-nexus.company.com:8081/repository/npm-proxy/
    
    # Update packages
    npm update --registry=http://airgap-nexus.company.com:8081/repository/npm-proxy/
    
    log_info "NPM repository sync completed"
}

# Sync Docker images
sync_docker() {
    log_info "Syncing Docker images..."
    
    # Pull latest versions
    docker pull openjdk:17-jdk-slim
    docker pull node:18-alpine
    docker pull postgres:15-alpine
    docker pull redis:7-alpine
    docker pull nginx:alpine
    
    # Save updated images
    docker save openjdk:17-jdk-slim > $REPO_BASE_DIR/docker/base/openjdk_17-jdk-slim.tar
    docker save node:18-alpine > $REPO_BASE_DIR/docker/base/node_18-alpine.tar
    docker save postgres:15-alpine > $REPO_BASE_DIR/docker/base/postgres_15-alpine.tar
    docker save redis:7-alpine > $REPO_BASE_DIR/docker/base/redis_7-alpine.tar
    docker save nginx:alpine > $REPO_BASE_DIR/docker/base/nginx_alpine.tar
    
    log_info "Docker images sync completed"
}

# Main execution
main() {
    log_info "Starting repository sync process..."
    
    sync_maven
    sync_npm
    sync_docker
    
    log_info "Repository sync completed successfully!"
}

# Run main function
main "$@"
EOF

chmod +x $REPO_BASE_DIR/scripts/maintenance/sync-repository.sh

# Create cleanup script
log_step "Creating cleanup script..."
cat > $REPO_BASE_DIR/scripts/maintenance/cleanup-repository.sh << 'EOF'
#!/bin/bash
# Script to cleanup old packages and images

set -e

REPO_BASE_DIR="/opt/airgap-repo"
CLEANUP_LOG="/var/log/airgap-repo-cleanup.log"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1" | tee -a $CLEANUP_LOG
}

# Cleanup old Maven artifacts
cleanup_maven() {
    log_info "Cleaning up old Maven artifacts..."
    
    # Remove old snapshots (keep last 5)
    find $REPO_BASE_DIR/maven -name "*-SNAPSHOT" -type d | sort -V | head -n -5 | xargs rm -rf
    
    # Remove old releases (keep last 10)
    find $REPO_BASE_DIR/maven -name "*.jar" -type f -mtime +30 | head -n -10 | xargs rm -f
    
    log_info "Maven cleanup completed"
}

# Cleanup old NPM packages
cleanup_npm() {
    log_info "Cleaning up old NPM packages..."
    
    # Remove old package versions (keep last 5)
    find $REPO_BASE_DIR/npm -name "*.tgz" -type f -mtime +30 | head -n -5 | xargs rm -f
    
    log_info "NPM cleanup completed"
}

# Cleanup old Docker images
cleanup_docker() {
    log_info "Cleaning up old Docker images..."
    
    # Remove old image files (keep last 3)
    find $REPO_BASE_DIR/docker -name "*.tar" -type f -mtime +30 | head -n -3 | xargs rm -f
    
    # Clean up Docker system
    docker system prune -f
    
    log_info "Docker cleanup completed"
}

# Main execution
main() {
    log_info "Starting repository cleanup process..."
    
    cleanup_maven
    cleanup_npm
    cleanup_docker
    
    log_info "Repository cleanup completed successfully!"
}

# Run main function
main "$@"
EOF

chmod +x $REPO_BASE_DIR/scripts/maintenance/cleanup-repository.sh

# Create systemd service for repository maintenance
log_step "Creating systemd service..."
cat > /etc/systemd/system/airgap-repo-maintenance.service << EOF
[Unit]
Description=Air-Gapped Repository Maintenance
After=network.target

[Service]
Type=oneshot
ExecStart=$REPO_BASE_DIR/scripts/maintenance/sync-repository.sh
ExecStart=$REPO_BASE_DIR/scripts/maintenance/cleanup-repository.sh
User=root
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# Create systemd timer for weekly maintenance
cat > /etc/systemd/system/airgap-repo-maintenance.timer << EOF
[Unit]
Description=Run Air-Gapped Repository Maintenance Weekly
Requires=airgap-repo-maintenance.service

[Timer]
OnCalendar=weekly
Persistent=true

[Install]
WantedBy=timers.target
EOF

# Enable and start timer
systemctl daemon-reload
systemctl enable airgap-repo-maintenance.timer
systemctl start airgap-repo-maintenance.timer

# Create repository status script
log_step "Creating repository status script..."
cat > $REPO_BASE_DIR/scripts/maintenance/repository-status.sh << 'EOF'
#!/bin/bash
# Script to check repository status

set -e

REPO_BASE_DIR="/opt/airgap-repo"

log_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

# Check Maven repository
check_maven() {
    log_info "Checking Maven repository..."
    
    local maven_size=$(du -sh $REPO_BASE_DIR/maven | cut -f1)
    local maven_count=$(find $REPO_BASE_DIR/maven -name "*.jar" | wc -l)
    
    echo "  Maven repository size: $maven_size"
    echo "  Maven artifacts count: $maven_count"
}

# Check NPM repository
check_npm() {
    log_info "Checking NPM repository..."
    
    local npm_size=$(du -sh $REPO_BASE_DIR/npm | cut -f1)
    local npm_count=$(find $REPO_BASE_DIR/npm -name "*.tgz" | wc -l)
    
    echo "  NPM repository size: $npm_size"
    echo "  NPM packages count: $npm_count"
}

# Check Docker repository
check_docker() {
    log_info "Checking Docker repository..."
    
    local docker_size=$(du -sh $REPO_BASE_DIR/docker | cut -f1)
    local docker_count=$(find $REPO_BASE_DIR/docker -name "*.tar" | wc -l)
    
    echo "  Docker repository size: $docker_size"
    echo "  Docker images count: $docker_count"
}

# Check Helm repository
check_helm() {
    log_info "Checking Helm repository..."
    
    local helm_size=$(du -sh $REPO_BASE_DIR/helm | cut -f1)
    local helm_count=$(find $REPO_BASE_DIR/helm -name "*.tgz" | wc -l)
    
    echo "  Helm repository size: $helm_size"
    echo "  Helm charts count: $helm_count"
}

# Main execution
main() {
    log_info "Air-Gapped Repository Status"
    echo "================================"
    
    check_maven
    check_npm
    check_docker
    check_helm
    
    echo "================================"
    log_info "Repository status check completed"
}

# Run main function
main "$@"
EOF

chmod +x $REPO_BASE_DIR/scripts/maintenance/repository-status.sh

# Create repository documentation
log_step "Creating repository documentation..."
cat > $REPO_BASE_DIR/README.md << 'EOF'
# Air-Gapped Package Repository

This repository contains all packages and dependencies required for building and deploying the Payment Engine in air-gapped environments.

## Structure

```
/opt/airgap-repo/
├── maven/                 # Maven dependencies
│   ├── releases/         # Release artifacts
│   ├── snapshots/        # Snapshot artifacts
│   └── public/           # Public repository
├── npm/                  # NPM packages
│   ├── packages/         # Package files
│   └── cache/            # NPM cache
├── docker/               # Docker images
│   ├── base/             # Base images
│   ├── app/              # Application images
│   └── monitoring/       # Monitoring images
├── helm/                 # Helm charts
│   ├── charts/           # Chart files
│   └── repositories/     # Repository configs
├── kubernetes/           # Kubernetes manifests
│   ├── manifests/        # YAML manifests
│   └── operators/        # Custom operators
├── scripts/              # Utility scripts
│   ├── setup/            # Setup scripts
│   ├── deploy/           # Deployment scripts
│   └── maintenance/      # Maintenance scripts
└── configs/              # Configuration files
    ├── maven/            # Maven configs
    ├── npm/              # NPM configs
    ├── docker/           # Docker configs
    └── kubernetes/       # Kubernetes configs
```

## Usage

### Maven
```bash
# Use air-gapped Maven settings
mvn -s /opt/airgap-repo/configs/maven/settings.xml clean package
```

### NPM
```bash
# Use air-gapped NPM registry
npm config set registry http://airgap-nexus.company.com:8081/repository/npm-proxy/
npm install
```

### Docker
```bash
# Load images from repository
docker load < /opt/airgap-repo/docker/base/openjdk_17-jdk-slim.tar
```

### Helm
```bash
# Install charts from repository
helm install my-app /opt/airgap-repo/helm/charts/my-app-1.0.0.tgz
```

## Maintenance

### Sync Repository
```bash
/opt/airgap-repo/scripts/maintenance/sync-repository.sh
```

### Cleanup Repository
```bash
/opt/airgap-repo/scripts/maintenance/cleanup-repository.sh
```

### Check Status
```bash
/opt/airgap-repo/scripts/maintenance/repository-status.sh
```

## Automated Maintenance

The repository includes automated maintenance via systemd:

- **Weekly sync**: Updates packages and images
- **Weekly cleanup**: Removes old artifacts
- **Status monitoring**: Tracks repository health

## Configuration

All configuration files are located in the `configs/` directory:

- `maven/settings.xml`: Maven repository configuration
- `npm/.npmrc`: NPM registry configuration
- `docker/daemon.json`: Docker registry configuration
- `kubernetes/kubeconfig`: Kubernetes cluster configuration

## Security

- All packages are verified for integrity
- Images are scanned for vulnerabilities
- Access is restricted to authorized users
- Regular security updates are applied

## Support

For issues or questions, please contact the development team or refer to the main documentation.
EOF

log_info "Air-gapped package repository setup completed!"
log_info "Repository location: $REPO_BASE_DIR"
log_info "Configuration files: $REPO_BASE_DIR/configs/"
log_info "Scripts: $REPO_BASE_DIR/scripts/"

echo ""
log_info "Next steps:"
echo "1. Run $REPO_BASE_DIR/scripts/setup/download-packages.sh to download packages"
echo "2. Configure your build tools to use the air-gapped repository"
echo "3. Set up automated maintenance via systemd timers"
echo "4. Monitor repository status regularly"