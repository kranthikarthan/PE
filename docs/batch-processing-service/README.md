# Batch Processing Service Documentation

## Overview

The Batch Processing Service is a comprehensive solution for processing large volumes of payment data in batch operations. It provides enterprise-grade capabilities including job management, file format support, SFTP integration, error handling, and performance monitoring.

## Table of Contents

- [Quick Start Guide](#quick-start-guide)
- [API Documentation](#api-documentation)
- [Configuration Guide](#configuration-guide)
- [Deployment Guide](#deployment-guide)
- [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)
- [User Guides](#user-guides)
- [Developer Guide](#developer-guide)

## Quick Start Guide

### Prerequisites

- Java 21+
- Spring Boot 3.2+
- PostgreSQL 14+
- Redis 6+
- Maven 3.8+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/company/payment-engine.git
   cd payment-engine/batch-processing-service
   ```

2. **Configure the database**
   ```bash
   # Create database
   createdb payment_engine_batch
   
   # Run migrations
   mvn flyway:migrate
   ```

3. **Configure Redis**
   ```bash
   # Start Redis server
   redis-server
   ```

4. **Start the service**
   ```bash
   mvn spring-boot:run
   ```

### Basic Usage

1. **Start a batch job**
   ```bash
   curl -X POST http://localhost:8080/batch/jobs/execute \
     -H "Content-Type: application/json" \
     -d '{
       "jobName": "paymentProcessingJob",
       "jobParameters": {
         "inputFile": "payments.csv",
         "tenantId": "tenant1"
       }
     }'
   ```

2. **Monitor job status**
   ```bash
   curl -X GET http://localhost:8080/batch/jobs/status/{jobExecutionId}
   ```

3. **View job history**
   ```bash
   curl -X GET http://localhost:8080/batch/jobs/history?tenantId=tenant1
   ```

## API Documentation

### Base URL
- Production: `https://api.payments.company.com/batch`
- Staging: `https://staging-api.payments.company.com/batch`
- Local: `http://localhost:8080/batch`

### Authentication
All API requests require authentication using JWT tokens:
```bash
Authorization: Bearer <your-jwt-token>
```

### Job Management Endpoints

#### Execute Job
```http
POST /jobs/execute
Content-Type: application/json

{
  "jobName": "paymentProcessingJob",
  "jobParameters": {
    "inputFile": "payments.csv",
    "tenantId": "tenant1",
    "businessUnitId": "bu1",
    "chunkSize": 1000
  }
}
```

**Response:**
```json
{
  "jobExecutionId": 123,
  "jobName": "paymentProcessingJob",
  "status": "STARTED",
  "startTime": "2025-10-19T10:00:00Z"
}
```

#### Get Job Status
```http
GET /jobs/status/{jobExecutionId}
```

**Response:**
```json
{
  "jobExecutionId": 123,
  "jobName": "paymentProcessingJob",
  "status": "RUNNING",
  "startTime": "2025-10-19T10:00:00Z",
  "endTime": null,
  "exitCode": null,
  "exitDescription": null,
  "progressPercentage": 45.5,
  "currentStep": "paymentProcessingStep",
  "recordsProcessed": 4550,
  "recordsFailed": 50,
  "recordsSkipped": 25,
  "totalRecords": 10000,
  "processingRate": 8.5,
  "memoryUsageMb": 512.0,
  "cpuUsagePercentage": 75.5
}
```

#### Stop Job
```http
POST /jobs/{jobExecutionId}/stop
```

#### Restart Job
```http
POST /jobs/{jobExecutionId}/restart
```

#### Get Job History
```http
GET /jobs/history?tenantId={tenantId}&page={page}&size={size}
```

#### Get Job Metrics
```http
GET /jobs/{jobExecutionId}/metrics
```

### Scheduling Endpoints

#### Create Schedule
```http
POST /schedules
Content-Type: application/json

{
  "scheduleId": "daily-payments",
  "jobName": "paymentProcessingJob",
  "scheduleName": "Daily Payment Processing",
  "description": "Process payments daily at 2 AM",
  "cronExpression": "0 0 2 * * ?",
  "timeZone": "UTC",
  "enabled": true,
  "priority": 5,
  "parameters": {
    "inputFile": "daily-payments.csv",
    "tenantId": "tenant1"
  }
}
```

#### Get Schedules
```http
GET /schedules?tenantId={tenantId}
```

#### Update Schedule
```http
PUT /schedules/{scheduleId}
```

#### Delete Schedule
```http
DELETE /schedules/{scheduleId}
```

### Configuration Endpoints

#### Get Job Configuration
```http
GET /configurations/{jobName}?tenantId={tenantId}
```

#### Update Job Configuration
```http
PUT /configurations/{jobName}
```

## Configuration Guide

### Application Properties

#### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_engine_batch
    username: ${DB_USERNAME:batch_user}
    password: ${DB_PASSWORD:batch_password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

#### Redis Configuration
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

#### Batch Processing Configuration
```yaml
batch:
  processing:
    chunk-size: 1000
    page-size: 50
    timeout-seconds: 3600
    retry-count: 3
    max-concurrent-executions: 5
    async: false
```

#### SFTP Configuration
```yaml
batch:
  sftp:
    host: ${SFTP_HOST:localhost}
    port: ${SFTP_PORT:22}
    username: ${SFTP_USERNAME:batch_user}
    password: ${SFTP_PASSWORD:}
    private-key-path: ${SFTP_PRIVATE_KEY_PATH:}
    known-hosts-file: ${SFTP_KNOWN_HOSTS_FILE:}
    connection-pool:
      max-connections: 10
      max-idle-time: 300000
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | `batch_user` |
| `DB_PASSWORD` | Database password | `batch_password` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `SFTP_HOST` | SFTP server host | `localhost` |
| `SFTP_PORT` | SFTP server port | `22` |
| `SFTP_USERNAME` | SFTP username | `batch_user` |
| `SFTP_PASSWORD` | SFTP password | - |
| `SFTP_PRIVATE_KEY_PATH` | SFTP private key path | - |
| `SFTP_KNOWN_HOSTS_FILE` | SFTP known hosts file | - |

## Deployment Guide

### Docker Deployment

1. **Build the Docker image**
   ```bash
   docker build -t payment-engine-batch:latest .
   ```

2. **Run with Docker Compose**
   ```yaml
   version: '3.8'
   services:
     batch-processing:
       image: payment-engine-batch:latest
       ports:
         - "8080:8080"
       environment:
         - DB_USERNAME=batch_user
         - DB_PASSWORD=batch_password
         - REDIS_HOST=redis
       depends_on:
         - postgres
         - redis
     
     postgres:
       image: postgres:14
       environment:
         - POSTGRES_DB=payment_engine_batch
         - POSTGRES_USER=batch_user
         - POSTGRES_PASSWORD=batch_password
       volumes:
         - postgres_data:/var/lib/postgresql/data
     
     redis:
       image: redis:6-alpine
       ports:
         - "6379:6379"
   
   volumes:
     postgres_data:
   ```

### Kubernetes Deployment

1. **Create namespace**
   ```bash
   kubectl create namespace payment-engine
   ```

2. **Deploy PostgreSQL**
   ```bash
   kubectl apply -f k8s/postgres.yaml
   ```

3. **Deploy Redis**
   ```bash
   kubectl apply -f k8s/redis.yaml
   ```

4. **Deploy Batch Processing Service**
   ```bash
   kubectl apply -f k8s/batch-processing.yaml
   ```

### Production Considerations

- **Resource Limits**: Set appropriate CPU and memory limits
- **Health Checks**: Configure liveness and readiness probes
- **Logging**: Implement structured logging with correlation IDs
- **Monitoring**: Set up Prometheus metrics and Grafana dashboards
- **Security**: Use secrets for sensitive configuration
- **Scaling**: Configure horizontal pod autoscaling

## Monitoring and Troubleshooting

### Health Checks

#### Application Health
```bash
curl http://localhost:8080/actuator/health
```

#### Database Health
```bash
curl http://localhost:8080/actuator/health/db
```

#### Redis Health
```bash
curl http://localhost:8080/actuator/health/redis
```

### Metrics

#### Application Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

#### Batch Job Metrics
```bash
curl http://localhost:8080/actuator/metrics/batch.job.executions
```

#### Performance Metrics
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/system.cpu.usage
```

### Logging

#### Application Logs
```bash
# View application logs
kubectl logs -f deployment/batch-processing-service

# View logs with specific level
kubectl logs -f deployment/batch-processing-service | grep ERROR
```

#### Database Logs
```bash
# View database logs
kubectl logs -f deployment/postgres
```

### Common Issues

#### Job Execution Failures
1. **Check job parameters**: Verify input file exists and is accessible
2. **Check database connectivity**: Ensure database is available and accessible
3. **Check memory usage**: Monitor memory consumption during job execution
4. **Check file format**: Verify file format is supported and valid

#### Performance Issues
1. **Check chunk size**: Adjust chunk size based on data volume
2. **Check database performance**: Monitor database query performance
3. **Check network connectivity**: Ensure stable network connection
4. **Check resource limits**: Verify CPU and memory limits are appropriate

#### SFTP Issues
1. **Check credentials**: Verify SFTP username and password
2. **Check network connectivity**: Ensure SFTP server is accessible
3. **Check file permissions**: Verify file read/write permissions
4. **Check firewall rules**: Ensure SFTP port is open

## User Guides

### For Business Users

#### Starting a Batch Job
1. Navigate to the Batch Processing dashboard
2. Click "New Job" button
3. Select job type (Payment Processing)
4. Upload input file (CSV, Excel, XML, or JSON)
5. Configure job parameters
6. Click "Start Job"

#### Monitoring Job Progress
1. Go to "Job Status" page
2. View real-time progress updates
3. Monitor processing statistics
4. Check for any errors or warnings

#### Scheduling Jobs
1. Go to "Schedules" page
2. Click "New Schedule" button
3. Configure schedule details
4. Set cron expression for timing
5. Save schedule

### For System Administrators

#### Managing Job Configurations
1. Go to "Configurations" page
2. Select job type
3. Modify configuration parameters
4. Save changes

#### Monitoring System Health
1. Check health endpoints
2. Review performance metrics
3. Monitor resource usage
4. Set up alerts for critical issues

#### Troubleshooting Issues
1. Review application logs
2. Check database connectivity
3. Verify file system access
4. Monitor network connectivity

## Developer Guide

### Project Structure
```
batch-processing-service/
├── src/main/java/com/payments/batch/
│   ├── api/                    # REST API controllers
│   ├── config/                 # Configuration classes
│   ├── domain/                 # Domain entities
│   ├── error/                  # Error handling
│   ├── format/                 # File format support
│   ├── processor/              # Batch processors
│   ├── reader/                 # Batch readers
│   ├── repository/             # Data repositories
│   ├── service/                # Business services
│   ├── sftp/                   # SFTP integration
│   └── writer/                 # Batch writers
├── src/main/resources/
│   ├── application.yml         # Application configuration
│   └── application-sftp.yml    # SFTP configuration
└── src/test/                   # Test classes
```

### Adding New File Formats

1. **Create format enum**
   ```java
   public enum FileFormat {
       CSV, EXCEL, XML, JSON, NEW_FORMAT
   }
   ```

2. **Implement reader interface**
   ```java
   @Component
   public class NewFormatPaymentItemReader implements PaymentItemReaderInterface {
       // Implementation
   }
   ```

3. **Update factory**
   ```java
   @Component
   public class FileFormatFactory {
       public PaymentItemReaderInterface createReader(FileFormat format) {
           return switch (format) {
               case NEW_FORMAT -> new NewFormatPaymentItemReader();
               // Other cases
           };
       }
   }
   ```

### Adding New Job Types

1. **Create job configuration**
   ```java
   @Configuration
   public class NewJobConfiguration {
       @Bean
       public Job newJob() {
           // Job definition
       }
   }
   ```

2. **Implement processors and writers**
   ```java
   @Component
   public class NewJobProcessor implements ItemProcessor<InputType, OutputType> {
       // Processing logic
   }
   ```

3. **Add API endpoints**
   ```java
   @RestController
   public class NewJobController {
       // API endpoints
   }
   ```

### Testing

#### Unit Tests
```bash
mvn test
```

#### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

#### Performance Tests
```bash
mvn test -Dtest=*PerformanceTest
```

### Code Quality

#### Code Style
```bash
mvn spotless:apply
```

#### Static Analysis
```bash
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check
```

#### Security Scanning
```bash
mvn dependency-check:check
```

## Support

For technical support and questions:
- Email: payments-support@company.com
- Documentation: https://docs.payments.company.com
- Issue Tracker: https://github.com/company/payment-engine/issues

## License

Proprietary - All rights reserved.
