# Multi-stage Dockerfile for Core Banking Service

# Build stage
FROM maven:3.9-openjdk-17-slim AS builder

WORKDIR /app

# Copy shared dependencies first
COPY shared/ /app/shared/
WORKDIR /app/shared
RUN mvn clean install -DskipTests

# Copy core banking source
WORKDIR /app
COPY core-banking/pom.xml /app/
COPY core-banking/src /app/src/

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Install useful utilities
RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r banking && useradd -r -g banking banking

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/core-banking-*.jar /app/core-banking.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R banking:banking /app

# Switch to non-root user
USER banking

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8081/core-banking/actuator/health || exit 1

# Expose ports
EXPOSE 8081 9091

# JVM options optimized for banking workloads
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/"

# Application properties
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar core-banking.jar"]