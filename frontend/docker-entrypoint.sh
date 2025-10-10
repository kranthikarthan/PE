#!/bin/sh

# Docker entrypoint script for React frontend

set -e

# Function to log messages
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

log "Starting Payment Engine Frontend..."

# Check if we're running in Kubernetes
if [ -n "$KUBERNETES_SERVICE_HOST" ]; then
    log "Running in Kubernetes environment"
    export REACT_APP_API_BASE_URL="/api"
else
    log "Running in Docker environment"
    export REACT_APP_API_BASE_URL="${REACT_APP_API_BASE_URL:-http://localhost:8080/api}"
fi

# Set environment variables for runtime
export REACT_APP_VERSION="${REACT_APP_VERSION:-1.0.0}"
export REACT_APP_ENVIRONMENT="${REACT_APP_ENVIRONMENT:-production}"

log "Environment configuration:"
log "  API Base URL: $REACT_APP_API_BASE_URL"
log "  Version: $REACT_APP_VERSION"
log "  Environment: $REACT_APP_ENVIRONMENT"

# Replace environment variables in built files (if needed)
# This is useful for runtime configuration without rebuilding
if [ -f /usr/share/nginx/html/static/js/*.js ]; then
    log "Replacing environment variables in JavaScript files..."
    find /usr/share/nginx/html/static/js -name "*.js" -exec sed -i \
        -e "s|REACT_APP_API_BASE_URL_PLACEHOLDER|$REACT_APP_API_BASE_URL|g" \
        -e "s|REACT_APP_VERSION_PLACEHOLDER|$REACT_APP_VERSION|g" \
        -e "s|REACT_APP_ENVIRONMENT_PLACEHOLDER|$REACT_APP_ENVIRONMENT|g" \
        {} \;
fi

# Test nginx configuration
log "Testing nginx configuration..."
nginx -t

# Start nginx
log "Starting nginx..."
exec "$@"