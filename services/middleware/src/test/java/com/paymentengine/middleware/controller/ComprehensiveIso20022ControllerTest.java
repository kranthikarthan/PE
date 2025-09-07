package com.paymentengine.middleware.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentengine.middleware.service.Iso20022MessageFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration test for ISO 20022 controller
 */
@WebMvcTest(ComprehensiveIso20022Controller.class)
class ComprehensiveIso20022ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Iso20022MessageFlowService messageFlowService;

    @Autowired
    private ObjectMapper objectMapper;

    private Iso20022MessageFlowService.MessageFlowResult mockFlowResult;

    @BeforeEach
    void setUp() {
        mockFlowResult = new Iso20022MessageFlowService.MessageFlowResult(
                "MSG-123456",
                "CORR-123456",
                "SUCCESS",
                "RTP",
                "TXN-123456",
                Map.of("transformed", "message"),
                Map.of("clearing", "response"),
                Map.of("client", "response"),
                null,
                250L,
                Map.of("flow", "PAIN001->PACS008->PACS002->PAIN002")
        );
    }

    @Test
    @WithMockUser(authorities = "iso20022:send")
    void testProcessPain001ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pain001Message = Map.of(
                "CstmrCdtTrfInitn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "MSG-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.processPain001ToClearingSystem(
                any(Map.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockFlowResult));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pain001-to-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pain001Message))
                        .param("tenantId", "demo-bank")
                        .param("paymentType", "RTP")
                        .param("localInstrumentCode", "RTP")
                        .param("responseMode", "IMMEDIATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("MSG-123456"))
                .andExpect(jsonPath("$.correlationId").value("CORR-123456"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.clearingSystemCode").value("RTP"))
                .andExpect(jsonPath("$.transactionId").value("TXN-123456"))
                .andExpect(jsonPath("$.processingTimeMs").value(250))
                .andExpect(jsonPath("$.metadata.flow").value("PAIN001->PACS008->PACS002->PAIN002"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:send")
    void testProcessCamt055ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> camt055Message = Map.of(
                "FIToFIPmtCxlReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CANCEL-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.processCamt055ToClearingSystem(
                any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockFlowResult));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/camt055-to-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camt055Message))
                        .param("tenantId", "demo-bank")
                        .param("originalMessageId", "MSG-123456")
                        .param("responseMode", "IMMEDIATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("MSG-123456"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.clearingSystemCode").value("RTP"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:send")
    void testProcessCamt056ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> camt056Message = Map.of(
                "FIToFIPmtStsReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "STATUS-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.processCamt056ToClearingSystem(
                any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockFlowResult));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/camt056-to-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camt056Message))
                        .param("tenantId", "demo-bank")
                        .param("originalMessageId", "MSG-123456")
                        .param("responseMode", "IMMEDIATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("MSG-123456"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.clearingSystemCode").value("RTP"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:send")
    void testProcessPacs028ToClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pacs028Message = Map.of(
                "FIToFIPaymentStatusRequest", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS028-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.processPacs028ToClearingSystem(
                any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockFlowResult));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pacs028-to-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacs028Message))
                        .param("tenantId", "demo-bank")
                        .param("originalMessageId", "MSG-123456")
                        .param("responseMode", "IMMEDIATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("MSG-123456"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.clearingSystemCode").value("RTP"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:receive")
    void testProcessPacs008FromClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pacs008Message = Map.of(
                "FIToFICstmrCdtTrf", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS008-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> response = Map.of(
                "status", "PROCESSED",
                "messageId", "PACS008-123456",
                "tenantId", "demo-bank",
                "timestamp", "2024-01-15T10:30:00"
        );

        when(messageFlowService.processPacs008FromClearingSystem(any(Map.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pacs008-from-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacs008Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"))
                .andExpect(jsonPath("$.data.messageId").value("PACS008-123456"))
                .andExpect(jsonPath("$.tenantId").value("demo-bank"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:receive")
    void testProcessPacs002FromClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pacs002Message = Map.of(
                "FIToFIPmtStsRpt", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS002-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> response = Map.of(
                "status", "PROCESSED",
                "messageId", "PACS002-123456",
                "tenantId", "demo-bank",
                "timestamp", "2024-01-15T10:30:00"
        );

        when(messageFlowService.processPacs002FromClearingSystem(any(Map.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pacs002-from-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacs002Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"))
                .andExpect(jsonPath("$.data.messageId").value("PACS002-123456"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:receive")
    void testProcessPacs004FromClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> pacs004Message = Map.of(
                "FIToFIPmtRtr", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS004-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> response = Map.of(
                "status", "PROCESSED",
                "messageId", "PACS004-123456",
                "tenantId", "demo-bank",
                "timestamp", "2024-01-15T10:30:00"
        );

        when(messageFlowService.processPacs004FromClearingSystem(any(Map.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pacs004-from-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacs004Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:receive")
    void testProcessCamt054FromClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> camt054Message = Map.of(
                "BkToCstmrDbtCdtNtfctn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CAMT054-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> response = Map.of(
                "status", "PROCESSED",
                "messageId", "CAMT054-123456",
                "tenantId", "demo-bank",
                "timestamp", "2024-01-15T10:30:00"
        );

        when(messageFlowService.processCamt054FromClearingSystem(any(Map.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/camt054-from-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camt054Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:receive")
    void testProcessCamt029FromClearingSystem_Success() throws Exception {
        // Given
        Map<String, Object> camt029Message = Map.of(
                "ReslOfInvstgtn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CAMT029-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> response = Map.of(
                "status", "PROCESSED",
                "messageId", "CAMT029-123456",
                "tenantId", "demo-bank",
                "timestamp", "2024-01-15T10:30:00"
        );

        when(messageFlowService.processCamt029FromClearingSystem(any(Map.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/camt029-from-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camt029Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:transform")
    void testTransformPain001ToPacs008_Success() throws Exception {
        // Given
        Map<String, Object> pain001Message = Map.of(
                "CstmrCdtTrfInitn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "MSG-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> pacs008Message = Map.of(
                "FIToFICstmrCdtTrf", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS008-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.transformPain001ToPacs008(
                any(Map.class), anyString(), anyString(), anyString()))
                .thenReturn(pacs008Message);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/transform/pain001-to-pacs008")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pain001Message))
                        .param("tenantId", "demo-bank")
                        .param("paymentType", "RTP")
                        .param("localInstrumentCode", "RTP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transformedMessage.FIToFICstmrCdtTrf.GrpHdr.MsgId").value("PACS008-123456"))
                .andExpect(jsonPath("$.tenantId").value("demo-bank"))
                .andExpect(jsonPath("$.paymentType").value("RTP"))
                .andExpect(jsonPath("$.localInstrumentCode").value("RTP"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:transform")
    void testTransformCamt055ToPacs007_Success() throws Exception {
        // Given
        Map<String, Object> camt055Message = Map.of(
                "FIToFIPmtCxlReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "CANCEL-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> pacs007Message = Map.of(
                "FIToFIPaymentCancellationRequest", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS007-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.transformCamt055ToPacs007(any(Map.class), anyString()))
                .thenReturn(pacs007Message);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/transform/camt055-to-pacs007")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camt055Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transformedMessage.FIToFIPaymentCancellationRequest.GrpHdr.MsgId").value("PACS007-123456"))
                .andExpect(jsonPath("$.tenantId").value("demo-bank"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:transform")
    void testTransformCamt056ToPacs028_Success() throws Exception {
        // Given
        Map<String, Object> camt056Message = Map.of(
                "FIToFIPmtStsReq", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "STATUS-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> pacs028Message = Map.of(
                "FIToFIPaymentStatusRequest", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "PACS028-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        when(messageFlowService.transformCamt056ToPacs028(any(Map.class), anyString()))
                .thenReturn(pacs028Message);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/transform/camt056-to-pacs028")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(camt056Message))
                        .param("tenantId", "demo-bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transformedMessage.FIToFIPaymentStatusRequest.GrpHdr.MsgId").value("PACS028-123456"))
                .andExpect(jsonPath("$.tenantId").value("demo-bank"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:validate")
    void testValidateIso20022Message_Success() throws Exception {
        // Given
        Map<String, Object> message = Map.of(
                "CstmrCdtTrfInitn", Map.of(
                        "GrpHdr", Map.of(
                                "MsgId", "MSG-123456",
                                "CreDtTm", "2024-01-15T10:30:00",
                                "NbOfTxs", "1"
                        )
                )
        );

        Map<String, Object> validation = Map.of(
                "valid", true,
                "messageType", "pain001"
        );

        when(messageFlowService.validateIso20022Message(any(Map.class), anyString()))
                .thenReturn(validation);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message))
                        .param("messageType", "pain001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.validation.valid").value(true))
                .andExpect(jsonPath("$.validation.messageType").value("pain001"))
                .andExpect(jsonPath("$.messageType").value("pain001"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:validate")
    void testValidateMessageFlow_Success() throws Exception {
        // Given
        Map<String, Object> validation = Map.of(
                "valid", true,
                "from", "pain001",
                "to", "pacs008",
                "direction", "CLIENT_TO_CLEARING"
        );

        when(messageFlowService.validateMessageFlow(anyString(), anyString(), anyString()))
                .thenReturn(validation);

        // When & Then
        mockMvc.perform(get("/api/v1/iso20022/comprehensive/validate-flow")
                        .param("fromMessageType", "pain001")
                        .param("toMessageType", "pacs008")
                        .param("flowDirection", "CLIENT_TO_CLEARING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.validation.valid").value(true))
                .andExpect(jsonPath("$.validation.from").value("pain001"))
                .andExpect(jsonPath("$.validation.to").value("pacs008"))
                .andExpect(jsonPath("$.validation.direction").value("CLIENT_TO_CLEARING"))
                .andExpect(jsonPath("$.fromMessageType").value("pain001"))
                .andExpect(jsonPath("$.toMessageType").value("pacs008"))
                .andExpect(jsonPath("$.flowDirection").value("CLIENT_TO_CLEARING"));
    }

    @Test
    @WithMockUser(authorities = "iso20022:read")
    void testGetMessageFlowHistory_Success() throws Exception {
        // Given
        String correlationId = "CORR-123456";
        Map<String, Object> history = Map.of(
                "correlationId", correlationId,
                "history", java.util.List.of(
                        Map.of("messageType", "pain001", "status", "SENT", "timestamp", "2024-01-15T10:30:00"),
                        Map.of("messageType", "pacs008", "status", "SENT", "timestamp", "2024-01-15T10:30:01"),
                        Map.of("messageType", "pacs002", "status", "RECEIVED", "timestamp", "2024-01-15T10:30:02"),
                        Map.of("messageType", "pain002", "status", "SENT", "timestamp", "2024-01-15T10:30:03")
                )
        );

        when(messageFlowService.getMessageFlowHistory(anyString()))
                .thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/v1/iso20022/comprehensive/flow-history/{correlationId}", correlationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.correlationId").value(correlationId))
                .andExpect(jsonPath("$.data.history").isArray())
                .andExpect(jsonPath("$.data.history[0].messageType").value("pain001"))
                .andExpect(jsonPath("$.data.history[0].status").value("SENT"))
                .andExpect(jsonPath("$.data.history[1].messageType").value("pacs008"))
                .andExpect(jsonPath("$.data.history[2].messageType").value("pacs002"))
                .andExpect(jsonPath("$.data.history[3].messageType").value("pain002"))
                .andExpect(jsonPath("$.correlationId").value(correlationId));
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/iso20022/comprehensive/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("comprehensive-iso20022-service"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.features.pain001Processing").value(true))
                .andExpect(jsonPath("$.features.camt055Processing").value(true))
                .andExpect(jsonPath("$.features.camt056Processing").value(true))
                .andExpect(jsonPath("$.features.pacs028Processing").value(true))
                .andExpect(jsonPath("$.features.pacs008Processing").value(true))
                .andExpect(jsonPath("$.features.pacs002Processing").value(true))
                .andExpect(jsonPath("$.features.pacs004Processing").value(true))
                .andExpect(jsonPath("$.features.camt054Processing").value(true))
                .andExpect(jsonPath("$.features.camt029Processing").value(true))
                .andExpect(jsonPath("$.features.messageTransformation").value(true))
                .andExpect(jsonPath("$.features.messageValidation").value(true))
                .andExpect(jsonPath("$.features.flowTracking").value(true));
    }

    @Test
    @WithMockUser(authorities = "iso20022:send")
    void testProcessPain001ToClearingSystem_Error() throws Exception {
        // Given
        Map<String, Object> pain001Message = Map.of("invalid", "message");

        Iso20022MessageFlowService.MessageFlowResult errorResult = new Iso20022MessageFlowService.MessageFlowResult(
                "MSG-123456",
                "CORR-123456",
                "ERROR",
                null,
                null,
                null,
                null,
                null,
                "Invalid message format",
                100L,
                Map.of("error", "Invalid message format")
        );

        when(messageFlowService.processPain001ToClearingSystem(
                any(Map.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(errorResult));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pain001-to-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pain001Message))
                        .param("tenantId", "demo-bank")
                        .param("paymentType", "RTP")
                        .param("localInstrumentCode", "RTP")
                        .param("responseMode", "IMMEDIATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageId").value("MSG-123456"))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid message format"));
    }

    @Test
    void testProcessPain001ToClearingSystem_Unauthorized() throws Exception {
        // Given
        Map<String, Object> pain001Message = Map.of("test", "message");

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/comprehensive/pain001-to-clearing-system")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pain001Message))
                        .param("tenantId", "demo-bank")
                        .param("paymentType", "RTP")
                        .param("localInstrumentCode", "RTP")
                        .param("responseMode", "IMMEDIATE"))
                .andExpect(status().isUnauthorized());
    }
}