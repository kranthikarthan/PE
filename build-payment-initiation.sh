#!/bin/bash

# Build Payment Initiation Service
echo "Building Payment Initiation Service..."

# Set JAVA_HOME if not set
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}

# Try to find Maven
MAVEN_CMD=""
if command -v mvn &> /dev/null; then
    MAVEN_CMD="mvn"
elif [ -f "/usr/bin/mvn" ]; then
    MAVEN_CMD="/usr/bin/mvn"
elif [ -f "/usr/local/bin/mvn" ]; then
    MAVEN_CMD="/usr/local/bin/mvn"
else
    echo "Maven not found. Trying to install..."
    # Try to install Maven
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y maven
        MAVEN_CMD="mvn"
    elif command -v yum &> /dev/null; then
        sudo yum install -y maven
        MAVEN_CMD="mvn"
    else
        echo "Cannot install Maven automatically. Please install Maven manually."
        exit 1
    fi
fi

echo "Using Maven: $MAVEN_CMD"

# Build shared modules first
echo "Building shared modules..."
cd /workspace

# Build domain-models
echo "Building domain-models..."
$MAVEN_CMD -f domain-models/pom.xml clean install -DskipTests -Dspotless.skip=true

# Build contracts
echo "Building contracts..."
$MAVEN_CMD -f contracts/pom.xml clean install -DskipTests -Dspotless.skip=true

# Build payment-initiation-service
echo "Building payment-initiation-service..."
cd payment-initiation-service
$MAVEN_CMD clean compile -DskipTests

echo "Build completed!"