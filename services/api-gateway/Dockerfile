# Multi-stage Dockerfile for API Gateway

# Build stage
FROM maven:3.9-openjdk-17-slim AS builder

WORKDIR /app

# Copy shared dependencies first
COPY shared/ /app/shared/
WORKDIR /app/shared
RUN mvn clean install -DskipTests

# Copy API Gateway source
WORKDIR /app
COPY api-gateway/pom.xml /app/
COPY api-gateway/src /app/src/

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Install useful utilities
RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r gateway && useradd -r -g gateway gateway

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/api-gateway-*.jar /app/api-gateway.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R gateway:gateway /app

# Switch to non-root user
USER gateway

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application properties
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar api-gateway.jar"]