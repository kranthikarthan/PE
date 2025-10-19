package com.payments.batch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Batch Processing Service.
 *
 * <p>This configuration provides comprehensive API documentation including
 * service information, contact details, and server configurations.
 *
 * @since PE-407
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI batchProcessingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Engine - Batch Processing Service API")
                        .description("""
                                ## Batch Processing Service API
                                
                                The Batch Processing Service provides comprehensive batch processing capabilities for the Payment Engine, including:
                                
                                ### Core Features
                                - **Job Management**: Start, stop, restart, and monitor batch jobs
                                - **File Format Support**: CSV, Excel, XML, and JSON file processing
                                - **SFTP Integration**: Secure file transfer for batch operations
                                - **Error Handling**: Advanced retry logic and circuit breaker patterns
                                - **Performance Monitoring**: Real-time metrics and performance tracking
                                - **Multi-Tenancy**: Complete tenant isolation and data security
                                
                                ### Job Execution
                                - Execute batch jobs with various file formats
                                - Monitor job progress and performance in real-time
                                - Handle job failures with automatic retry mechanisms
                                - Support for concurrent job execution
                                
                                ### Scheduling
                                - Schedule jobs using cron expressions
                                - Time-based job triggers
                                - Job priority management
                                - Execution limits and constraints
                                
                                ### Performance Metrics
                                - Real-time performance monitoring
                                - Historical metrics and trends
                                - Resource usage tracking (CPU, memory)
                                - Processing rate analysis
                                
                                ### Security
                                - Multi-tenant data isolation
                                - Row-Level Security (RLS) policies
                                - Secure file transfer with SFTP
                                - Comprehensive audit logging
                                
                                ### API Features
                                - RESTful API design
                                - OpenAPI 3.0 specification
                                - Comprehensive error handling
                                - Request/response validation
                                - Rate limiting and throttling
                                
                                ### Integration
                                - Spring Batch integration
                                - Database persistence with JPA
                                - Event-driven architecture
                                - Monitoring and observability
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Payment Engine Team")
                                .email("payments@company.com")
                                .url("https://company.com/payments"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://company.com/license")))
                .servers(List.of(
                        new Server()
                                .url("https://api.payments.company.com/batch")
                                .description("Production Server"),
                        new Server()
                                .url("https://staging-api.payments.company.com/batch")
                                .description("Staging Server"),
                        new Server()
                                .url("http://localhost:8080/batch")
                                .description("Local Development Server")));
    }
}
