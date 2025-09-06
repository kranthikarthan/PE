# Multi-stage Dockerfile for Middleware Service

# Build stage
FROM maven:3.9-openjdk-17-slim AS builder

WORKDIR /app

# Copy shared dependencies first
COPY shared/ /app/shared/
WORKDIR /app/shared
RUN mvn clean install -DskipTests

# Copy middleware source
WORKDIR /app
COPY middleware/pom.xml /app/
COPY middleware/src /app/src/

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
RUN groupadd -r middleware && useradd -r -g middleware middleware

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/middleware-*.jar /app/middleware.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R middleware:middleware /app

# Switch to non-root user
USER middleware

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/middleware/actuator/health || exit 1

# Expose ports
EXPOSE 8082 9092

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application properties
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar middleware.jar"]