package com.paymentengine.corebanking.service;

import com.paymentengine.shared.dto.iso20022.Pain002Message;
import com.paymentengine.shared.service.KafkaResponseService;
import com.paymentengine.shared.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KafkaResponseService
 */
@ExtendWith(MockitoExtension.class)
class KafkaResponseServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private KafkaResponseService kafkaResponseService;

    @BeforeEach
    void setUp() {
        kafkaResponseService = new KafkaResponseService(kafkaTemplate, objectMapper);
        TenantContext.setCurrentTenant("test-bank");
    }

    @Test
    void testPublishPain002Response_WithPaymentTypeSpecificTopic() {
        // Arrange
        Pain002Message pain002 = createTestPain002Message();
        String paymentType = "RTP";
        String originalMessageId = "MSG-20240115-001";
        
        Map<String, Object> responseConfig = new HashMap<>();
        responseConfig.put("usePaymentTypeSpecificTopic", true);
        responseConfig.put("topicPattern", "payment-engine.{tenantId}.responses.{paymentType}.pain002");
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        
        // Act
        CompletableFuture<SendResult<String, Object>> result = kafkaResponseService.publishPain002Response(
            pain002, paymentType, originalMessageId, responseConfig);
        
        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(eq("payment-engine.test-bank.responses.rtp.pain002"), eq(originalMessageId), any());
    }

    @Test
    void testPublishPain002Response_WithExplicitTopic() {
        // Arrange
        Pain002Message pain002 = createTestPain002Message();
        String paymentType = "ACH_CREDIT";
        String originalMessageId = "MSG-20240115-002";
        
        Map<String, Object> responseConfig = new HashMap<>();
        responseConfig.put("kafkaTopicName", "custom-response-topic");
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        
        // Act
        CompletableFuture<SendResult<String, Object>> result = kafkaResponseService.publishPain002Response(
            pain002, paymentType, originalMessageId, responseConfig);
        
        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(eq("custom-response-topic"), eq(originalMessageId), any());
    }

    @Test
    void testCreatePaymentTypeResponseTopic() {
        // Act
        String topicName = kafkaResponseService.createPaymentTypeResponseTopic("regional-bank", "WIRE_TRANSFER");
        
        // Assert
        assertEquals("payment-engine.regional-bank.responses.wire_transfer.pain002", topicName);
    }

    @Test
    void testCreateDefaultResponseTopic() {
        // Act
        String topicName = kafkaResponseService.createDefaultResponseTopic("fintech-corp");
        
        // Assert
        assertEquals("payment-engine.fintech-corp.responses.pain002", topicName);
    }

    @Test
    void testValidateKafkaResponseConfig_ValidConfig() {
        // Arrange
        Map<String, Object> validConfig = new HashMap<>();
        validConfig.put("usePaymentTypeSpecificTopic", true);
        
        // Act
        boolean isValid = kafkaResponseService.validateKafkaResponseConfig(validConfig);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateKafkaResponseConfig_InvalidConfig() {
        // Arrange
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("kafkaTopicName", "");
        
        // Act
        boolean isValid = kafkaResponseService.validateKafkaResponseConfig(invalidConfig);
        
        // Assert
        assertFalse(isValid);
    }

    @Test
    void testPublishStatusUpdate() {
        // Arrange
        String tenantId = "test-bank";
        String paymentType = "RTP";
        String transactionId = "TXN-20240115-001";
        String status = "COMPLETED";
        String statusReason = "Payment processed successfully";
        Map<String, Object> additionalData = Map.of("processingTime", "2.3s");
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        
        // Act
        CompletableFuture<SendResult<String, Object>> result = kafkaResponseService.publishStatusUpdate(
            tenantId, paymentType, transactionId, status, statusReason, additionalData);
        
        // Assert
        assertNotNull(result);
        verify(kafkaTemplate).send(eq("payment-engine.test-bank.status-updates.rtp"), eq(transactionId), any());
    }

    @Test
    void testGetKafkaResponseConfig() {
        // Act
        Map<String, Object> config = kafkaResponseService.getKafkaResponseConfig("test-bank", "RTP");
        
        // Assert
        assertNotNull(config);
        assertTrue((Boolean) config.get("usePaymentTypeSpecificTopic"));
        assertEquals("payment-engine.{tenantId}.responses.{paymentType}.pain002", config.get("paymentTypeTopicPattern"));
        assertEquals("HIGH", config.get("priority"));
    }

    private Pain002Message createTestPain002Message() {
        Pain002Message.GroupHeader groupHeader = new Pain002Message.GroupHeader(
            "PAIN002-TEST-001",
            LocalDateTime.now(),
            new com.paymentengine.shared.dto.iso20022.Party("Test Bank", null, null)
        );
        
        Pain002Message.OriginalGroupInformationAndStatus originalGroupInfo = 
            new Pain002Message.OriginalGroupInformationAndStatus(
                "MSG-20240115-001",
                "pain.001.001.03",
                "ACCP"
            );
        
        Pain002Message.TransactionInformationAndStatus txInfo = 
            new Pain002Message.TransactionInformationAndStatus(
                "E2E-20240115-001",
                "ACCP"
            );
        
        return new Pain002Message(groupHeader, originalGroupInfo, List.of(txInfo));
    }
}