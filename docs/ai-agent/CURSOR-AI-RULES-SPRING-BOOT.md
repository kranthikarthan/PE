# CURSOR AI RULES FOR SPRING BOOT & SOLUTION ARCHITECTURE

## FUNDAMENTAL PRINCIPLES
- Follow clean architecture and SOLID principles
- Prioritize maintainable, testable, and scalable code
- Always consider enterprise-grade patterns and practices
- Focus on performance optimization and resource efficiency
- Implement proper error handling and logging strategies

## JAVA & SPRING BOOT STANDARDS

### Code Style & Structure
- Use Java 21+ features when appropriate (records, pattern matching, virtual threads)
- Follow constructor injection over field injection (@Autowired on constructors only)
- Implement proper validation using Bean Validation (JSR-303/380)
- Use DTOs for API layer, never expose entities directly
- Apply proper exception hierarchy with custom exceptions
- Implement proper logging with structured logging (JSON format preferred)

### Spring Boot Architecture
- Follow layered architecture: Controller → Service → Repository
- Use @RestController for REST APIs with proper HTTP status codes
- Implement proper pagination and sorting for data retrieval
- Apply @Transactional appropriately with proper propagation
- Use Spring Boot configuration properties instead of @Value
- Implement health checks and metrics endpoints
- Apply proper security annotations (@PreAuthorize, @Secured)

### Database & JPA
- Use Spring Data JPA with proper query optimization
- Implement proper entity relationships with lazy loading
- Apply database migrations using Flyway or Liquibase
- Use projections for read-only queries to optimize performance
- Implement proper connection pooling (HikariCP configuration)
- Apply database indexing strategies for query optimization

## MICROSERVICES & DISTRIBUTED SYSTEMS

### Service Design
- Design services around business capabilities
- Implement proper service boundaries with well-defined APIs
- Use event-driven architecture patterns where appropriate
- Apply circuit breaker patterns for resilience
- Implement proper service discovery and load balancing
- Design for failure with proper retry mechanisms and timeouts

### API Design
- Follow RESTful principles with proper resource modeling
- Use OpenAPI/Swagger for API documentation
- Implement proper versioning strategy (URL or header-based)
- Apply proper HTTP methods and status codes
- Use HATEOAS principles for API discoverability
- Implement proper rate limiting and throttling

### Integration Patterns
- Use Spring Cloud for microservices infrastructure
- Implement proper message queue integration (RabbitMQ, Kafka)
- Apply async processing patterns using @Async or reactive streams
- Use Spring Cloud Contract for consumer-driven contract testing
- Implement proper configuration management (Spring Cloud Config)

## KUBERNETES & CONTAINERIZATION

### Container Optimization
- Create multi-stage Docker builds for minimal image size
- Implement proper health checks (readiness, liveness probes)
- Configure proper resource limits and requests
- Use distroless base images for security
- Apply proper layer caching strategies
- Implement graceful shutdown handling

### Kubernetes Integration
- Design applications to be cloud-native and 12-factor compliant
- Implement proper service mesh integration (Istio)
- Use ConfigMaps and Secrets for configuration management
- Apply proper RBAC and security policies
- Implement proper monitoring and observability (Prometheus, Grafana)
- Design for horizontal scaling with stateless services

## TESTING STRATEGIES

### Test Structure
- Follow test pyramid: Unit → Integration → E2E
- Use @SpringBootTest for integration tests with proper test slices
- Implement contract testing with Spring Cloud Contract
- Use TestContainers for integration testing with real databases
- Apply proper mocking strategies with Mockito
- Implement performance testing with JMH for critical paths

### Test Quality
- Achieve minimum 80% code coverage with meaningful tests
- Write tests that are fast, isolated, and deterministic
- Use BDD approach with Given-When-Then structure
- Implement proper test data management
- Apply proper test categorization (@Tag annotations)

## PERFORMANCE & OBSERVABILITY

### Performance Optimization
- Profile applications using async-profiler or similar tools
- Optimize JVM settings for containerized environments
- Implement proper caching strategies (Redis, Caffeine)
- Apply lazy loading and eager fetching appropriately
- Monitor and optimize database query performance
- Use reactive programming for I/O intensive operations

### Monitoring & Logging
- Implement distributed tracing with Spring Cloud Sleuth
- Use structured logging with correlation IDs
- Apply proper metrics collection (Micrometer)
- Implement proper error tracking and alerting
- Use centralized logging with ELK stack or similar
- Apply proper log levels and avoid logging sensitive data

## SECURITY BEST PRACTICES

### Application Security
- Never store secrets in code or configuration files
- Use Spring Security with proper authentication/authorization
- Implement proper input validation and sanitization
- Apply OWASP security guidelines
- Use HTTPS only with proper certificate management
- Implement proper session management and CSRF protection

### API Security
- Use JWT tokens with proper expiration and refresh strategies
- Implement proper rate limiting and DDoS protection
- Apply input validation at API boundaries
- Use API keys or OAuth2 for service-to-service communication
- Implement proper audit logging for security events

## DOCUMENTATION & ARCHITECTURE

### Code Documentation
- Write meaningful JavaDoc for public APIs
- Include architectural decision records (ADRs)
- Maintain up-to-date README with setup instructions
- Document configuration properties and their purposes
- Include sequence diagrams for complex flows
- Maintain API documentation with examples

### Solution Architecture
- Create high-level architecture diagrams (C4 model preferred)
- Document integration patterns and data flows
- Include deployment architecture diagrams
- Maintain component interaction diagrams
- Document non-functional requirements and constraints
- Include disaster recovery and business continuity plans

## CODE GENERATION PREFERENCES

### When Writing Code
- Always include proper error handling with specific exceptions
- Add comprehensive logging at appropriate levels
- Include input validation with meaningful error messages
- Generate complete unit tests for new methods
- Add proper documentation and comments for complex logic
- Consider thread safety and concurrency implications

### When Refactoring
- Maintain backward compatibility unless explicitly requested
- Update tests to reflect changes
- Preserve existing business logic unless improving it
- Add deprecation warnings for removed functionality
- Update documentation to reflect changes
- Consider performance implications of changes

## INTEGRATION WITH TEMPORAL WORKFLOWS
- Design Spring Boot services to integrate with Temporal workflows
- Implement proper activity methods with proper serialization
- Use proper exception handling in Temporal activities
- Apply proper retry policies for Temporal workflows
- Implement proper signal and query handling
- Design for workflow versioning and migration

Remember: Focus on creating maintainable, scalable, and secure Spring Boot applications that follow enterprise patterns and can be easily deployed in Kubernetes environments.
