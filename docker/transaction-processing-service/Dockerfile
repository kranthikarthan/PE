# Multi-stage build for Transaction Processing Service
FROM maven:3.9.11-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml files first for better layer caching
COPY pom.xml .
COPY domain-models/pom.xml domain-models/
COPY contracts/pom.xml contracts/
COPY shared-telemetry/pom.xml shared-telemetry/
COPY shared-config/pom.xml shared-config/
COPY transaction-processing-service/pom.xml transaction-processing-service/

# Copy source code
COPY domain-models/ domain-models/
COPY contracts/ contracts/
COPY shared-telemetry/ shared-telemetry/
COPY shared-config/ shared-config/
COPY transaction-processing-service/ transaction-processing-service/

# Build the application
RUN mvn clean package -pl transaction-processing-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/transaction-processing-service/target/*.jar app.jar

# Change ownership to app user
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
