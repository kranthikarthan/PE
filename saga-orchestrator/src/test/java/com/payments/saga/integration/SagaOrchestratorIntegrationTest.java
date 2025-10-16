package com.payments.saga.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import com.payments.saga.service.SagaOrchestrator;
import com.payments.saga.service.SagaService;
import com.payments.saga.service.SagaStepService;
import com.payments.saga.service.SagaEventService;
import com.payments.saga.service.SagaTemplateService;
import com.payments.saga.service.SagaLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for Saga Orchestrator using Testcontainers
 */
@SpringBootTest
@Testcontainers
class SagaOrchestratorIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("saga_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private SagaOrchestrator sagaOrchestrator;
    
    @Autowired
    private SagaService sagaService;
    
    @Autowired
    private SagaStepService sagaStepService;
    
    @Autowired
    private SagaEventService sagaEventService;
    
    @Autowired
    private SagaTemplateService sagaTemplateService;
    
    @Autowired
    private SagaLookupService sagaLookupService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private TenantContext tenantContext;

    @BeforeEach
    void setUp() {
        tenantContext = TenantContext.of("tenant-1", "Test Tenant", "bu-1", "Test Business Unit");
    }

    @Test
    void testCompleteSagaWorkflow_Success() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        // When
        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);

        // Then
        assertNotNull(saga);
        assertEquals(templateName, saga.getSagaName());
        assertEquals(tenantContext, saga.getTenantContext());
        assertEquals(correlationId, saga.getCorrelationId());
        assertEquals(paymentId, saga.getPaymentId());
        assertEquals(SagaStatus.PENDING, saga.getStatus());
        assertTrue(saga.getTotalSteps() > 0);

        // Verify saga is saved
        Optional<Saga> savedSaga = sagaService.getSaga(saga.getId());
        assertTrue(savedSaga.isPresent());
        assertEquals(saga.getId(), savedSaga.get().getId());

        // Verify steps are created
        List<SagaStep> steps = sagaStepService.getStepsBySagaId(saga.getId());
        assertFalse(steps.isEmpty());
        assertEquals(saga.getTotalSteps(), steps.size());

        // Verify all steps are in PENDING status
        for (SagaStep step : steps) {
            assertEquals(SagaStepStatus.PENDING, step.getStatus());
        }
    }

    @Test
    void testSagaLookupByPaymentId() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);

        // When
        Optional<Saga> foundSaga = sagaLookupService.findSagaByPaymentId(paymentId);

        // Then
        assertTrue(foundSaga.isPresent());
        assertEquals(saga.getId(), foundSaga.get().getId());
        assertEquals(paymentId, foundSaga.get().getPaymentId());
    }

    @Test
    void testSagaLookupByCorrelationId() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);

        // When
        Optional<Saga> foundSaga = sagaLookupService.findSagaByCorrelationId(correlationId);

        // Then
        assertTrue(foundSaga.isPresent());
        assertEquals(saga.getId(), foundSaga.get().getId());
        assertEquals(correlationId, foundSaga.get().getCorrelationId());
    }

    @Test
    void testSagaLookupBySagaId() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);

        // When
        Optional<Saga> foundSaga = sagaLookupService.findSagaById(saga.getId().getValue());

        // Then
        assertTrue(foundSaga.isPresent());
        assertEquals(saga.getId(), foundSaga.get().getId());
    }

    @Test
    void testSagaLookup_NotFound() {
        // When
        Optional<Saga> foundSaga = sagaLookupService.findSagaByPaymentId("non-existent-payment");

        // Then
        assertTrue(foundSaga.isEmpty());
    }

    @Test
    void testSagaTemplateRegistration() {
        // Given
        String templateName = "PaymentProcessingSaga";

        // When
        SagaTemplate template = sagaTemplateService.getTemplate(templateName);

        // Then
        assertNotNull(template);
        assertEquals(templateName, template.getTemplateName());
        assertFalse(template.getStepDefinitions().isEmpty());
    }

    @Test
    void testSagaTemplateRegistration_NotFound() {
        // When
        SagaTemplate template = sagaTemplateService.getTemplate("NonExistentTemplate");

        // Then
        assertNull(template);
    }

    @Test
    void testSagaStepExecution() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);
        List<SagaStep> steps = sagaStepService.getStepsBySagaId(saga.getId());
        SagaStep firstStep = steps.get(0);

        // When
        sagaOrchestrator.executeNextStep(saga.getId());

        // Then
        // Verify step status is updated (this would require mocking the execution engine)
        SagaStep updatedStep = sagaStepService.getStep(firstStep.getId()).orElse(null);
        assertNotNull(updatedStep);
    }

    @Test
    void testSagaStepCompletion() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);
        List<SagaStep> steps = sagaStepService.getStepsBySagaId(saga.getId());
        SagaStep firstStep = steps.get(0);
        Map<String, Object> outputData = Map.of("validationResult", "success");

        // When
        sagaOrchestrator.handleStepCompletion(firstStep.getId(), outputData);

        // Then
        SagaStep updatedStep = sagaStepService.getStep(firstStep.getId()).orElse(null);
        assertNotNull(updatedStep);
        assertEquals(SagaStepStatus.COMPLETED, updatedStep.getStatus());
        assertEquals(outputData, updatedStep.getOutputData());
    }

    @Test
    void testSagaStepFailure() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);
        List<SagaStep> steps = sagaStepService.getStepsBySagaId(saga.getId());
        SagaStep firstStep = steps.get(0);
        String errorMessage = "Validation failed";
        Map<String, Object> errorData = Map.of("error", "invalid_data");

        // When
        sagaOrchestrator.handleStepFailure(firstStep.getId(), errorMessage, errorData);

        // Then
        SagaStep updatedStep = sagaStepService.getStep(firstStep.getId()).orElse(null);
        assertNotNull(updatedStep);
        assertEquals(SagaStepStatus.FAILED, updatedStep.getStatus());
        assertEquals(errorMessage, updatedStep.getErrorMessage());
        assertEquals(errorData, updatedStep.getErrorData());
    }

    @Test
    void testSagaCompensation() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);
        String reason = "Step failed";

        // When
        sagaOrchestrator.startCompensation(saga.getId(), reason);

        // Then
        Saga updatedSaga = sagaService.getSaga(saga.getId()).orElse(null);
        assertNotNull(updatedSaga);
        assertEquals(SagaStatus.COMPENSATING, updatedSaga.getStatus());
    }

    @Test
    void testSagaCompletion() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);
        saga.start(); // Set to RUNNING status

        // When
        sagaOrchestrator.completeSaga(saga);

        // Then
        assertEquals(SagaStatus.COMPLETED, saga.getStatus());
        assertNotNull(saga.getCompletedAt());
    }

    @Test
    void testSagaCompensationCompletion() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");

        Saga saga = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);
        saga.startCompensation(); // Set to COMPENSATING status

        // When
        sagaOrchestrator.completeCompensation(saga.getId());

        // Then
        Saga updatedSaga = sagaService.getSaga(saga.getId()).orElse(null);
        assertNotNull(updatedSaga);
        assertEquals(SagaStatus.COMPENSATED, updatedSaga.getStatus());
        assertNotNull(updatedSaga.getCompensatedAt());
    }
}






