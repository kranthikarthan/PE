package com.paymentengine.middleware.service;

import com.paymentengine.middleware.service.impl.Iso20022MessageFlowServiceImpl;
import com.paymentengine.middleware.service.ClearingSystemRoutingService;
import com.paymentengine.middleware.service.Pain001ToPacs008TransformationService;
import com.paymentengine.middleware.service.SchemeMessageService;
import com.paymentengine.middleware.service.Iso20022FormatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ISO 20022 message flow service
 */
@ExtendWith(MockitoExtension.class)
class Iso20022MessageFlowServiceTest {

    @Mock
    private ClearingSystemRoutingService clearingSystemRoutingService;
    
    @Mock
    private Pain001ToPacs008TransformationService transformationService;
    
    @Mock
    private SchemeMessageService schemeMessageService;
    
    @Mock
    private Iso20022FormatService iso20022FormatService;

    private Iso20022MessageFlowService messageFlowService;

    @BeforeEach
    void setUp() {
        messageFlowService = new Iso20022MessageFlowServiceImpl(
                clearingSystemRoutingService,
                transformationService,
                schemeMessageService,
                iso20022FormatService
        );
    }

    @Test
    void testProcessPain001ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pain001Message = createPain001Message();
        String tenantId = "demo-bank";
        String paymentType = "RTP";
        String localInstrumentCode = "RTP";
        String responseMode = "IMMEDIATE";

        ClearingSystemRoutingService.ClearingSystemRoute route = new ClearingSystemRoutingService.ClearingSystemRoute(
                "RTP", "Real-Time Payments", "scheme-config-123"
        );

        Pain001ToPacs008TransformationService.PaymentInfo paymentInfo = 
                new Pain001ToPacs008TransformationService.PaymentInfo(
                        "MSG-123456", "PMT-123456", "E2E-123456", 1000.00, "USD"
                );

        Map<String, Object> pacs008Message = createPacs008Message();
        Map<String, Object> pacs002Response = createPacs002Response();
        Map<String, Object> pain002Response = createPain002Response();

        when(clearingSystemRoutingService.routeMessage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(route);
        when(transformationService.extractPaymentInfo(any(Map.class)))
                .thenReturn(paymentInfo);
        when(transformationService.transformPain001ToPacs008(any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(pacs008Message);
        when(schemeMessageService.sendMessageToClearingSystem(any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(pacs002Response));

        // When
        CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                messageFlowService.processPain001ToClearingSystem(
                        pain001Message, tenantId, paymentType, localInstrumentCode, responseMode);

        Iso20022MessageFlowService.MessageFlowResult result = future.get();

        // Then
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("MSG-123456", result.getMessageId());
        assertEquals("RTP", result.getClearingSystemCode());
        assertNotNull(result.getTransformedMessage());
        assertNotNull(result.getClearingSystemResponse());
        assertNotNull(result.getClientResponse());
        assertTrue(result.getProcessingTimeMs() > 0);

        verify(clearingSystemRoutingService).routeMessage(tenantId, paymentType, localInstrumentCode, "pacs008");
        verify(transformationService).extractPaymentInfo(pain001Message);
        verify(transformationService).transformPain001ToPacs008(pain001Message, tenantId, paymentType, localInstrumentCode);
    }

    @Test
    void testProcessCamt055ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> camt055Message = createCamt055Message();
        String tenantId = "demo-bank";
        String originalMessageId = "MSG-123456";
        String responseMode = "IMMEDIATE";

        ClearingSystemRoutingService.ClearingSystemRoute route = new ClearingSystemRoutingService.ClearingSystemRoute(
                "RTP", "Real-Time Payments", "scheme-config-123"
        );

        Map<String, Object> pacs007Message = createPacs007Message();
        Map<String, Object> pacs002Response = createPacs002Response();
        Map<String, Object> camt029Response = createCamt029Response();

        when(clearingSystemRoutingService.routeMessage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(route);
        when(schemeMessageService.sendMessageToClearingSystem(any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(pacs002Response));

        // When
        CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                messageFlowService.processCamt055ToClearingSystem(
                        camt055Message, tenantId, originalMessageId, responseMode);

        Iso20022MessageFlowService.MessageFlowResult result = future.get();

        // Then
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("RTP", result.getClearingSystemCode());
        assertNotNull(result.getTransformedMessage());
        assertNotNull(result.getClearingSystemResponse());
        assertNotNull(result.getClientResponse());

        verify(clearingSystemRoutingService).routeMessage(tenantId, "CANCELLATION", null, "pacs007");
    }

    @Test
    void testProcessCamt056ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> camt056Message = createCamt056Message();
        String tenantId = "demo-bank";
        String originalMessageId = "MSG-123456";
        String responseMode = "IMMEDIATE";

        ClearingSystemRoutingService.ClearingSystemRoute route = new ClearingSystemRoutingService.ClearingSystemRoute(
                "RTP", "Real-Time Payments", "scheme-config-123"
        );

        Map<String, Object> pacs028Message = createPacs028Message();
        Map<String, Object> pacs002Response = createPacs002Response();
        Map<String, Object> camt056Response = createCamt056Response();

        when(clearingSystemRoutingService.routeMessage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(route);
        when(schemeMessageService.sendMessageToClearingSystem(any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(pacs002Response));

        // When
        CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                messageFlowService.processCamt056ToClearingSystem(
                        camt056Message, tenantId, originalMessageId, responseMode);

        Iso20022MessageFlowService.MessageFlowResult result = future.get();

        // Then
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("RTP", result.getClearingSystemCode());
        assertNotNull(result.getTransformedMessage());
        assertNotNull(result.getClearingSystemResponse());
        assertNotNull(result.getClientResponse());

        verify(clearingSystemRoutingService).routeMessage(tenantId, "STATUS_REQUEST", null, "pacs028");
    }

    @Test
    void testProcessPacs028ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pacs028Message = createPacs028Message();
        String tenantId = "demo-bank";
        String originalMessageId = "MSG-123456";
        String responseMode = "IMMEDIATE";

        ClearingSystemRoutingService.ClearingSystemRoute route = new ClearingSystemRoutingService.ClearingSystemRoute(
                "RTP", "Real-Time Payments", "scheme-config-123"
        );

        Map<String, Object> pacs002Response = createPacs002Response();

        when(clearingSystemRoutingService.routeMessage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(route);
        when(schemeMessageService.sendMessageToClearingSystem(any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(pacs002Response));

        // When
        CompletableFuture<Iso20022MessageFlowService.MessageFlowResult> future = 
                messageFlowService.processPacs028ToClearingSystem(
                        pacs028Message, tenantId, originalMessageId, responseMode);

        Iso20022MessageFlowService.MessageFlowResult result = future.get();

        // Then
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("RTP", result.getClearingSystemCode());
        assertNotNull(result.getTransformedMessage());
        assertNotNull(result.getClearingSystemResponse());
        assertNotNull(result.getClientResponse());

        verify(clearingSystemRoutingService).routeMessage(tenantId, "STATUS_REQUEST", null, "pacs028");
    }

    @Test
    void testProcessPacs008FromClearingSystem_Success() {
        // Given
        Map<String, Object> pacs008Message = createPacs008Message();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.processPacs008FromClearingSystem(pacs008Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("messageId"));
        assertTrue(result.containsKey("tenantId"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testProcessPacs002FromClearingSystem_Success() {
        // Given
        Map<String, Object> pacs002Message = createPacs002Response();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.processPacs002FromClearingSystem(pacs002Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("messageId"));
        assertTrue(result.containsKey("tenantId"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testProcessPacs004FromClearingSystem_Success() {
        // Given
        Map<String, Object> pacs004Message = createPacs004Message();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.processPacs004FromClearingSystem(pacs004Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("messageId"));
        assertTrue(result.containsKey("tenantId"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testProcessCamt054FromClearingSystem_Success() {
        // Given
        Map<String, Object> camt054Message = createCamt054Message();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.processCamt054FromClearingSystem(camt054Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("messageId"));
        assertTrue(result.containsKey("tenantId"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testProcessCamt029FromClearingSystem_Success() {
        // Given
        Map<String, Object> camt029Message = createCamt029Response();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.processCamt029FromClearingSystem(camt029Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("messageId"));
        assertTrue(result.containsKey("tenantId"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testTransformPain001ToPacs008_Success() {
        // Given
        Map<String, Object> pain001Message = createPain001Message();
        String tenantId = "demo-bank";
        String paymentType = "RTP";
        String localInstrumentCode = "RTP";

        Map<String, Object> pacs008Message = createPacs008Message();

        when(transformationService.transformPain001ToPacs008(any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(pacs008Message);

        // When
        Map<String, Object> result = messageFlowService.transformPain001ToPacs008(
                pain001Message, tenantId, paymentType, localInstrumentCode);

        // Then
        assertNotNull(result);
        assertEquals(pacs008Message, result);

        verify(transformationService).transformPain001ToPacs008(pain001Message, tenantId, paymentType, localInstrumentCode);
    }

    @Test
    void testTransformCamt055ToPacs007_Success() {
        // Given
        Map<String, Object> camt055Message = createCamt055Message();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.transformCamt055ToPacs007(camt055Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("FIToFIPaymentCancellationRequest"));
        assertTrue(result.containsKey("_metadata"));
    }

    @Test
    void testTransformCamt056ToPacs028_Success() {
        // Given
        Map<String, Object> camt056Message = createCamt056Message();
        String tenantId = "demo-bank";

        // When
        Map<String, Object> result = messageFlowService.transformCamt056ToPacs028(camt056Message, tenantId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("FIToFIPaymentStatusRequest"));
        assertTrue(result.containsKey("_metadata"));
    }

    @Test
    void testGeneratePain002Response_Success() {
        // Given
        String originalMessageId = "MSG-123456";
        String transactionId = "TXN-123456";
        String status = "ACSC";
        String reasonCode = "G000";
        String responseMode = "IMMEDIATE";

        // When
        Map<String, Object> result = messageFlowService.generatePain002Response(
                originalMessageId, transactionId, status, reasonCode, responseMode);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("CstmrPmtStsRpt"));
        assertTrue(result.containsKey("_metadata"));

        Map<String, Object> cstmrPmtStsRpt = (Map<String, Object>) result.get("CstmrPmtStsRpt");
        assertTrue(cstmrPmtStsRpt.containsKey("GrpHdr"));
        assertTrue(cstmrPmtStsRpt.containsKey("OrgnlGrpInfAndSts"));
        assertTrue(cstmrPmtStsRpt.containsKey("PmtInfSts"));
    }

    @Test
    void testValidateIso20022Message_Success() {
        // Given
        Map<String, Object> message = createPain001Message();
        String messageType = "pain001";

        // When
        Map<String, Object> result = messageFlowService.validateIso20022Message(message, messageType);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("valid"));
        assertTrue(result.containsKey("messageType"));
        assertEquals(true, result.get("valid"));
        assertEquals(messageType, result.get("messageType"));
    }

    @Test
    void testValidateMessageFlow_Success() {
        // Given
        String fromMessageType = "pain001";
        String toMessageType = "pacs008";
        String flowDirection = "CLIENT_TO_CLEARING";

        // When
        Map<String, Object> result = messageFlowService.validateMessageFlow(fromMessageType, toMessageType, flowDirection);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("valid"));
        assertTrue(result.containsKey("from"));
        assertTrue(result.containsKey("to"));
        assertTrue(result.containsKey("direction"));
        assertEquals(true, result.get("valid"));
        assertEquals(fromMessageType, result.get("from"));
        assertEquals(toMessageType, result.get("to"));
        assertEquals(flowDirection, result.get("direction"));
    }

    @Test
    void testCorrelateMessage_Success() {
        // Given
        String originalMessageId = "MSG-123456";
        String messageType = "pain001";
        String flowDirection = "CLIENT_TO_CLEARING";

        // When
        String result = messageFlowService.correlateMessage(originalMessageId, messageType, flowDirection);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("CORR-"));
    }

    @Test
    void testTrackMessageFlow_Success() {
        // Given
        String correlationId = "CORR-123456";
        String messageType = "pain001";
        String status = "SUCCESS";
        Map<String, Object> metadata = Map.of("tenantId", "demo-bank", "paymentType", "RTP");

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> {
            messageFlowService.trackMessageFlow(correlationId, messageType, status, metadata);
        });
    }

    @Test
    void testGetMessageFlowHistory_Success() {
        // Given
        String correlationId = "CORR-123456";

        // When
        Map<String, Object> result = messageFlowService.getMessageFlowHistory(correlationId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("correlationId"));
        assertTrue(result.containsKey("history"));
        assertEquals(correlationId, result.get("correlationId"));
    }

    // Helper methods to create test messages
    private Map<String, Object> createPain001Message() {
        return Map.of(
                "CstmrCdtTrfInitn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "MSG-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        ),
                        "PmtInf", Map.of(
                                "PmtInfId", "PMT-123456",
                                "PmtMtd", "TRF",
                                "ReqdExctnDt", "2024-01-15"
                        )
                )
        );
    }

    private Map<String, Object> createPacs008Message() {
        return Map.of(
                "FIToFICstmrCdtTrf", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS008-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createPacs002Response() {
        return Map.of(
                "FIToFIPmtStsRpt", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS002-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createPain002Response() {
        return Map.of(
                "CstmrPmtStsRpt", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PAIN002-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createCamt055Message() {
        return Map.of(
                "FIToFIPmtCxlReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CANCEL-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createPacs007Message() {
        return Map.of(
                "FIToFIPaymentCancellationRequest", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS007-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createCamt029Response() {
        return Map.of(
                "ReslOfInvstgtn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CAMT029-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createCamt056Message() {
        return Map.of(
                "FIToFIPmtStsReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "STATUS-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createPacs028Message() {
        return Map.of(
                "FIToFIPaymentStatusRequest", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS028-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createCamt056Response() {
        return Map.of(
                "FIToFIPmtStsReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CAMT056-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createPacs004Message() {
        return Map.of(
                "FIToFIPmtRtr", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS004-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }

    private Map<String, Object> createCamt054Message() {
        return Map.of(
                "BkToCstmrDbtCdtNtfctn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CAMT054-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );
    }
}