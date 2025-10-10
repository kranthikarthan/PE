# Multi-stage Dockerfile for Auth Service

# Build stage
FROM maven:3.9-openjdk-17-slim AS builder

WORKDIR /app

# Build shared components
COPY shared/ /app/shared/
WORKDIR /app/shared
RUN mvn clean install -DskipTests

# Copy auth service source
WORKDIR /app
COPY auth-service/pom.xml /app/
COPY auth-service/src /app/src/

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r authservice && useradd -r -g authservice authservice

WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/auth-service-*.jar /app/auth-service.jar

RUN mkdir -p /app/logs && chown -R authservice:authservice /app

USER authservice

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/auth/actuator/health || exit 1

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar auth-service.jar"]
