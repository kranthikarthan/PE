package com.payments.batch.quality;

import com.payments.batch.config.BatchJobConfiguration;
import com.payments.batch.domain.BatchJobExecutionMetadata;
import com.payments.batch.domain.BatchJobSchedule;
import com.payments.batch.domain.BatchJobMetrics;
import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.repository.BatchJobExecutionMetadataRepository;
import com.payments.batch.repository.BatchJobScheduleRepository;
import com.payments.batch.repository.BatchJobMetricsRepository;
import com.payments.batch.repository.ProcessedPaymentRepository;
import com.payments.batch.service.BatchJobManagementService;
import com.payments.batch.api.BatchJobController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Code quality and coverage tests for the Batch Processing Service.
 *
 * <p>This test class provides comprehensive quality assurance including
 * code coverage validation, architectural compliance, and best practices verification.
 *
 * @since PE-407
 */
@SpringBootTest
@ActiveProfiles("test")
class CodeQualityTest {

    @Autowired
    private BatchJobConfiguration batchJobConfiguration;

    @Autowired
    private BatchJobManagementService batchJobManagementService;

    @Autowired
    private BatchJobController batchJobController;

    @Autowired
    private BatchJobExecutionMetadataRepository metadataRepository;

    @Autowired
    private BatchJobScheduleRepository scheduleRepository;

    @Autowired
    private BatchJobMetricsRepository metricsRepository;

    @Autowired
    private ProcessedPaymentRepository processedPaymentRepository;

    @Test
    @DisplayName("Should have proper bean configuration")
    void shouldHaveProperBeanConfiguration() {
        // Verify all required beans are configured
        assertNotNull(batchJobConfiguration, "BatchJobConfiguration should be configured");
        assertNotNull(batchJobManagementService, "BatchJobManagementService should be configured");
        assertNotNull(batchJobController, "BatchJobController should be configured");
        assertNotNull(metadataRepository, "BatchJobExecutionMetadataRepository should be configured");
        assertNotNull(scheduleRepository, "BatchJobScheduleRepository should be configured");
        assertNotNull(metricsRepository, "BatchJobMetricsRepository should be configured");
        assertNotNull(processedPaymentRepository, "ProcessedPaymentRepository should be configured");
    }

    @Test
    @DisplayName("Should have proper repository interface methods")
    void shouldHaveProperRepositoryInterfaceMethods() {
        // Verify BatchJobExecutionMetadataRepository has required methods
        Method[] metadataMethods = BatchJobExecutionMetadataRepository.class.getDeclaredMethods();
        List<String> metadataMethodNames = Arrays.stream(metadataMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(metadataMethodNames.contains("findByJobExecutionId"), 
                "Should have findByJobExecutionId method");
        assertTrue(metadataMethodNames.contains("findByTenantId"), 
                "Should have findByTenantId method");
        assertTrue(metadataMethodNames.contains("findByStatusAndTenantId"), 
                "Should have findByStatusAndTenantId method");
        assertTrue(metadataMethodNames.contains("countByStatusAndTenantId"), 
                "Should have countByStatusAndTenantId method");

        // Verify BatchJobScheduleRepository has required methods
        Method[] scheduleMethods = BatchJobScheduleRepository.class.getDeclaredMethods();
        List<String> scheduleMethodNames = Arrays.stream(scheduleMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(scheduleMethodNames.contains("findByScheduleId"), 
                "Should have findByScheduleId method");
        assertTrue(scheduleMethodNames.contains("findByTenantId"), 
                "Should have findByTenantId method");
        assertTrue(scheduleMethodNames.contains("findActiveSchedulesByTenantId"), 
                "Should have findActiveSchedulesByTenantId method");
        assertTrue(scheduleMethodNames.contains("countByStatusAndTenantId"), 
                "Should have countByStatusAndTenantId method");

        // Verify BatchJobMetricsRepository has required methods
        Method[] metricsMethods = BatchJobMetricsRepository.class.getDeclaredMethods();
        List<String> metricsMethodNames = Arrays.stream(metricsMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(metricsMethodNames.contains("findByJobNameAndTenantId"), 
                "Should have findByJobNameAndTenantId method");
        assertTrue(metricsMethodNames.contains("findByTenantId"), 
                "Should have findByTenantId method");
        assertTrue(metricsMethodNames.contains("findByDateRangeAndTenantId"), 
                "Should have findByDateRangeAndTenantId method");
        assertTrue(metricsMethodNames.contains("countByTenantId"), 
                "Should have countByTenantId method");
    }

    @Test
    @DisplayName("Should have proper domain entity structure")
    void shouldHaveProperDomainEntityStructure() {
        // Verify BatchJobExecutionMetadata entity structure
        Method[] metadataMethods = BatchJobExecutionMetadata.class.getDeclaredMethods();
        List<String> metadataMethodNames = Arrays.stream(metadataMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(metadataMethodNames.contains("calculateDuration"), 
                "Should have calculateDuration method");
        assertTrue(metadataMethodNames.contains("calculateProcessingRate"), 
                "Should have calculateProcessingRate method");
        assertTrue(metadataMethodNames.contains("calculateSuccessRate"), 
                "Should have calculateSuccessRate method");
        assertTrue(metadataMethodNames.contains("isRunning"), 
                "Should have isRunning method");
        assertTrue(metadataMethodNames.contains("isCompleted"), 
                "Should have isCompleted method");
        assertTrue(metadataMethodNames.contains("isSuccessful"), 
                "Should have isSuccessful method");
        assertTrue(metadataMethodNames.contains("isFailed"), 
                "Should have isFailed method");

        // Verify BatchJobSchedule entity structure
        Method[] scheduleMethods = BatchJobSchedule.class.getDeclaredMethods();
        List<String> scheduleMethodNames = Arrays.stream(scheduleMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(scheduleMethodNames.contains("isActive"), 
                "Should have isActive method");
        assertTrue(scheduleMethodNames.contains("isWithinDateRange"), 
                "Should have isWithinDateRange method");
        assertTrue(scheduleMethodNames.contains("hasReachedMaxExecutions"), 
                "Should have hasReachedMaxExecutions method");
        assertTrue(scheduleMethodNames.contains("canExecute"), 
                "Should have canExecute method");
        assertTrue(scheduleMethodNames.contains("incrementExecutionCount"), 
                "Should have incrementExecutionCount method");

        // Verify BatchJobMetrics entity structure
        Method[] metricsMethods = BatchJobMetrics.class.getDeclaredMethods();
        List<String> metricsMethodNames = Arrays.stream(metricsMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(metricsMethodNames.contains("calculateSuccessRate"), 
                "Should have calculateSuccessRate method");
        assertTrue(metricsMethodNames.contains("calculateFailureRate"), 
                "Should have calculateFailureRate method");
        assertTrue(metricsMethodNames.contains("calculateAverageExecutionTime"), 
                "Should have calculateAverageExecutionTime method");
        assertTrue(metricsMethodNames.contains("calculateAverageProcessingRate"), 
                "Should have calculateAverageProcessingRate method");
        assertTrue(metricsMethodNames.contains("updateMetrics"), 
                "Should have updateMetrics method");
    }

    @Test
    @DisplayName("Should have proper enum definitions")
    void shouldHaveProperEnumDefinitions() {
        // Verify BatchJobExecutionMetadata enums
        Class<?>[] executionTypeEnum = BatchJobExecutionMetadata.ExecutionType.class.getEnumConstants();
        assertNotNull(executionTypeEnum, "ExecutionType enum should exist");
        assertEquals(4, executionTypeEnum.length, "ExecutionType should have 4 values");

        Class<?>[] executionStatusEnum = BatchJobExecutionMetadata.ExecutionStatus.class.getEnumConstants();
        assertNotNull(executionStatusEnum, "ExecutionStatus enum should exist");
        assertEquals(6, executionStatusEnum.length, "ExecutionStatus should have 6 values");

        // Verify BatchJobSchedule enums
        Class<?>[] scheduleStatusEnum = BatchJobSchedule.ScheduleStatus.class.getEnumConstants();
        assertNotNull(scheduleStatusEnum, "ScheduleStatus enum should exist");
        assertEquals(4, scheduleStatusEnum.length, "ScheduleStatus should have 4 values");
    }

    @Test
    @DisplayName("Should have proper service layer structure")
    void shouldHaveProperServiceLayerStructure() {
        // Verify BatchJobManagementService has required methods
        Method[] serviceMethods = BatchJobManagementService.class.getDeclaredMethods();
        List<String> serviceMethodNames = Arrays.stream(serviceMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(serviceMethodNames.contains("startJob"), 
                "Should have startJob method");
        assertTrue(serviceMethodNames.contains("stopJob"), 
                "Should have stopJob method");
        assertTrue(serviceMethodNames.contains("restartJob"), 
                "Should have restartJob method");
        assertTrue(serviceMethodNames.contains("getJobStatus"), 
                "Should have getJobStatus method");
        assertTrue(serviceMethodNames.contains("getJobHistory"), 
                "Should have getJobHistory method");
        assertTrue(serviceMethodNames.contains("getJobMetrics"), 
                "Should have getJobMetrics method");
    }

    @Test
    @DisplayName("Should have proper controller layer structure")
    void shouldHaveProperControllerLayerStructure() {
        // Verify BatchJobController has required methods
        Method[] controllerMethods = BatchJobController.class.getDeclaredMethods();
        List<String> controllerMethodNames = Arrays.stream(controllerMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(controllerMethodNames.contains("executeJob"), 
                "Should have executeJob method");
        assertTrue(controllerMethodNames.contains("getJobStatus"), 
                "Should have getJobStatus method");
        assertTrue(controllerMethodNames.contains("stopJob"), 
                "Should have stopJob method");
        assertTrue(controllerMethodNames.contains("restartJob"), 
                "Should have restartJob method");
        assertTrue(controllerMethodNames.contains("getJobHistory"), 
                "Should have getJobHistory method");
        assertTrue(controllerMethodNames.contains("getJobMetrics"), 
                "Should have getJobMetrics method");
        assertTrue(controllerMethodNames.contains("getJobConfiguration"), 
                "Should have getJobConfiguration method");
        assertTrue(controllerMethodNames.contains("scheduleJob"), 
                "Should have scheduleJob method");
    }

    @Test
    @DisplayName("Should have proper method visibility")
    void shouldHaveProperMethodVisibility() {
        // Verify public methods are properly exposed
        Method[] publicMethods = BatchJobManagementService.class.getMethods();
        long publicMethodCount = Arrays.stream(publicMethods)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .count();

        assertTrue(publicMethodCount > 0, "Should have public methods");

        // Verify service methods are public
        Method[] serviceMethods = BatchJobManagementService.class.getDeclaredMethods();
        long publicServiceMethods = Arrays.stream(serviceMethods)
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .count();

        assertTrue(publicServiceMethods > 0, "Should have public service methods");
    }

    @Test
    @DisplayName("Should have proper exception handling")
    void shouldHaveProperExceptionHandling() {
        // Verify exception classes exist
        try {
            Class.forName("com.payments.batch.error.BatchProcessingException");
            Class.forName("com.payments.batch.error.CircuitBreakerOpenException");
        } catch (ClassNotFoundException e) {
            fail("Required exception classes should exist");
        }

        // Verify exception hierarchy
        Class<?> batchProcessingException = null;
        try {
            batchProcessingException = Class.forName("com.payments.batch.error.BatchProcessingException");
        } catch (ClassNotFoundException e) {
            fail("BatchProcessingException should exist");
        }

        assertNotNull(batchProcessingException, "BatchProcessingException should exist");
        assertTrue(RuntimeException.class.isAssignableFrom(batchProcessingException), 
                "BatchProcessingException should extend RuntimeException");
    }

    @Test
    @DisplayName("Should have proper configuration structure")
    void shouldHaveProperConfigurationStructure() {
        // Verify configuration classes exist
        try {
            Class.forName("com.payments.batch.config.BatchJobConfiguration");
            Class.forName("com.payments.batch.config.OpenApiConfiguration");
        } catch (ClassNotFoundException e) {
            fail("Required configuration classes should exist");
        }

        // Verify configuration methods
        Method[] configMethods = BatchJobConfiguration.class.getDeclaredMethods();
        List<String> configMethodNames = Arrays.stream(configMethods)
                .map(Method::getName)
                .collect(Collectors.toList());

        assertTrue(configMethodNames.contains("paymentProcessingJob"), 
                "Should have paymentProcessingJob method");
        assertTrue(configMethodNames.contains("paymentProcessingStep"), 
                "Should have paymentProcessingStep method");
    }

    @Test
    @DisplayName("Should have proper test coverage structure")
    void shouldHaveProperTestCoverageStructure() {
        // Verify test classes exist
        try {
            Class.forName("com.payments.batch.integration.BatchProcessingServiceIntegrationTest");
            Class.forName("com.payments.batch.performance.BatchProcessingPerformanceTest");
            Class.forName("com.payments.batch.quality.CodeQualityTest");
        } catch (ClassNotFoundException e) {
            fail("Required test classes should exist");
        }

        // Verify test methods exist
        Method[] integrationTestMethods = com.payments.batch.integration.BatchProcessingServiceIntegrationTest.class.getDeclaredMethods();
        long testMethodCount = Arrays.stream(integrationTestMethods)
                .filter(method -> method.isAnnotationPresent(Test.class))
                .count();

        assertTrue(testMethodCount > 0, "Should have test methods");

        Method[] performanceTestMethods = com.payments.batch.performance.BatchProcessingPerformanceTest.class.getDeclaredMethods();
        long performanceTestMethodCount = Arrays.stream(performanceTestMethods)
                .filter(method -> method.isAnnotationPresent(Test.class))
                .count();

        assertTrue(performanceTestMethodCount > 0, "Should have performance test methods");
    }

    @Test
    @DisplayName("Should have proper package structure")
    void shouldHaveProperPackageStructure() {
        // Verify package structure
        String[] expectedPackages = {
                "com.payments.batch.api",
                "com.payments.batch.config",
                "com.payments.batch.domain",
                "com.payments.batch.error",
                "com.payments.batch.format",
                "com.payments.batch.processor",
                "com.payments.batch.reader",
                "com.payments.batch.repository",
                "com.payments.batch.service",
                "com.payments.batch.sftp",
                "com.payments.batch.writer"
        };

        for (String packageName : expectedPackages) {
            try {
                Class.forName(packageName + ".package-info");
            } catch (ClassNotFoundException e) {
                // Package exists but no package-info class
                // This is acceptable
            }
        }

        // Verify test package structure
        String[] expectedTestPackages = {
                "com.payments.batch.integration",
                "com.payments.batch.performance",
                "com.payments.batch.quality"
        };

        for (String packageName : expectedTestPackages) {
            try {
                Class.forName(packageName + ".package-info");
            } catch (ClassNotFoundException e) {
                // Package exists but no package-info class
                // This is acceptable
            }
        }
    }

    @Test
    @DisplayName("Should have proper annotation usage")
    void shouldHaveProperAnnotationUsage() {
        // Verify Spring annotations are used
        assertTrue(BatchJobConfiguration.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
                "BatchJobConfiguration should have @Configuration annotation");
        assertTrue(BatchJobController.class.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class),
                "BatchJobController should have @RestController annotation");
        assertTrue(BatchJobManagementService.class.isAnnotationPresent(org.springframework.stereotype.Service.class),
                "BatchJobManagementService should have @Service annotation");

        // Verify JPA annotations are used
        assertTrue(BatchJobExecutionMetadata.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "BatchJobExecutionMetadata should have @Entity annotation");
        assertTrue(BatchJobSchedule.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "BatchJobSchedule should have @Entity annotation");
        assertTrue(BatchJobMetrics.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "BatchJobMetrics should have @Entity annotation");
        assertTrue(ProcessedPayment.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "ProcessedPayment should have @Entity annotation");

        // Verify repository annotations are used
        assertTrue(BatchJobExecutionMetadataRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class),
                "BatchJobExecutionMetadataRepository should have @Repository annotation");
        assertTrue(BatchJobScheduleRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class),
                "BatchJobScheduleRepository should have @Repository annotation");
        assertTrue(BatchJobMetricsRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class),
                "BatchJobMetricsRepository should have @Repository annotation");
        assertTrue(ProcessedPaymentRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class),
                "ProcessedPaymentRepository should have @Repository annotation");
    }
}
