# PowerShell build script for React Operations Portal
param(
    [string]$ImageTag = "latest",
    [string]$Registry = ""
)

# Configuration
$IMAGE_NAME = "operations-portal"

Write-Host "Starting build of React Operations Portal..." -ForegroundColor Green

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Check if Docker is available
try {
    docker --version | Out-Null
} catch {
    Write-Error "Docker is not installed or not in PATH"
    exit 1
}

# Check if Docker is running
try {
    docker info | Out-Null
} catch {
    Write-Error "Docker is not running"
    exit 1
}

# Build the Docker image
Write-Status "Building Docker image..."
docker build -t "$IMAGE_NAME`:$ImageTag" .

# Tag for registry if provided
if ($Registry -ne "") {
    Write-Status "Tagging image for registry..."
    docker tag "$IMAGE_NAME`:$ImageTag" "$Registry/$IMAGE_NAME`:$ImageTag"
}

# Run security scan
Write-Status "Running security scan..."
try {
    trivy image "$IMAGE_NAME`:$ImageTag"
} catch {
    Write-Warning "Trivy not found, skipping security scan"
}

# Run tests in container
Write-Status "Running tests in container..."
docker run --rm "$IMAGE_NAME`:$ImageTag" npm test -- --coverage --watchAll=false

# Display image information
Write-Status "Build completed successfully!"
Write-Status "Image: $IMAGE_NAME`:$ImageTag"
if ($Registry -ne "") {
    Write-Status "Registry image: $Registry/$IMAGE_NAME`:$ImageTag"
}

# Display useful commands
Write-Host "Useful commands:" -ForegroundColor Green
Write-Host "  docker run -p 8080:8080 $IMAGE_NAME`:$ImageTag"
Write-Host "  docker push $Registry/$IMAGE_NAME`:$ImageTag"
Write-Host "  docker images | findstr $IMAGE_NAME"
