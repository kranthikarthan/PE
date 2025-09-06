package com.paymentengine.corebanking.controller.iso20022;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentengine.corebanking.controller.Iso20022MessageController;
import com.paymentengine.corebanking.service.Iso20022ProcessingService;
import com.paymentengine.shared.dto.iso20022.Pain001Message;
import com.paymentengine.shared.dto.iso20022.Camt055Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ISO 20022 Message Controller
 */
@WebMvcTest(Iso20022MessageController.class)
class Iso20022ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Iso20022ProcessingService iso20022ProcessingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Pain001Message samplePain001;
    private Camt055Message sampleCamt055;

    @BeforeEach
    void setUp() {
        setupSampleMessages();
    }

    @Test
    @DisplayName("Should process pain.001 message successfully")
    @WithMockUser(authorities = {"payment:create"})
    void shouldProcessPain001Successfully() throws Exception {
        // Given
        Map<String, Object> expectedResponse = Map.of(
            "CstmrPmtStsRpt", Map.of(
                "GrpHdr", Map.of(
                    "MsgId", "PAIN002-" + System.currentTimeMillis(),
                    "CreDtTm", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                ),
                "OrgnlGrpInfAndSts", Map.of(
                    "OrgnlMsgId", "MSG-TEST-001",
                    "GrpSts", "ACSC"
                )
            )
        );

        when(iso20022ProcessingService.processPain001(any(Pain001Message.class), any(Map.class)))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePain001)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.CstmrPmtStsRpt").exists())
                .andExpect(jsonPath("$.CstmrPmtStsRpt.GrpHdr.MsgId").exists())
                .andExpect(jsonPath("$.CstmrPmtStsRpt.OrgnlGrpInfAndSts.GrpSts").value("ACSC"));
    }

    @Test
    @DisplayName("Should reject invalid pain.001 message")
    @WithMockUser(authorities = {"payment:create"})
    void shouldRejectInvalidPain001() throws Exception {
        // Given
        when(iso20022ProcessingService.processPain001(any(Pain001Message.class), any(Map.class)))
            .thenThrow(new IllegalArgumentException("Invalid pain.001 message: Missing required fields"));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePain001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_PAYMENT_REQUEST"));
    }

    @Test
    @DisplayName("Should process camt.055 cancellation successfully")
    @WithMockUser(authorities = {"payment:cancel"})
    void shouldProcessCamt055Successfully() throws Exception {
        // Given
        Map<String, Object> expectedResponse = Map.of(
            "cancellationResults", List.of(Map.of(
                "originalEndToEndId", "E2E-TEST-001",
                "cancellationId", "CXL-TEST-001",
                "status", "ACCEPTED",
                "cancellationReason", "Customer requested cancellation",
                "reasonCode", "CUST"
            )),
            "totalCancellations", 1
        );

        when(iso20022ProcessingService.processCamt055(any(Camt055Message.class), any(Map.class)))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/camt055")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCamt055)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancellationResults").isArray())
                .andExpect(jsonPath("$.cancellationResults[0].status").value("ACCEPTED"))
                .andExpected(jsonPath("$.totalCancellations").value(1));
    }

    @Test
    @DisplayName("Should reject unauthorized pain.001 request")
    void shouldRejectUnauthorizedPain001() throws Exception {
        mockMvc.perform(post("/api/v1/iso20022/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePain001)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get supported messages without authentication")
    void shouldGetSupportedMessages() throws Exception {
        mockMvc.perform(get("/api/v1/iso20022/supported-messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerInitiated").exists())
                .andExpect(jsonPath("$.schemeProcessing").exists())
                .andExpect(jsonPath("$.cashManagement").exists())
                .andExpect(jsonPath("$.cashManagement['camt.055.001.03']").exists())
                .andExpect(jsonPath("$.cashManagement['camt.055.001.03'].name").value("Customer Payment Cancellation Request"));
    }

    @Test
    @DisplayName("Should validate pain.001 message")
    @WithMockUser(authorities = {"payment:create"})
    void shouldValidatePain001Message() throws Exception {
        // Given
        Map<String, Object> validationResult = Map.of(
            "valid", true,
            "messageId", "MSG-TEST-001",
            "errors", List.of(),
            "warnings", List.of()
        );

        when(iso20022ProcessingService.validateMessage(eq("pain001"), any(Map.class)))
            .thenReturn(validationResult);

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/validate/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("test", "message"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.messageId").value("MSG-TEST-001"));
    }

    @Test
    @DisplayName("Should get pain.002 status")
    @WithMockUser(authorities = {"transaction:read"})
    void shouldGetPain002Status() throws Exception {
        // Given
        UUID transactionId = UUID.randomUUID();
        Map<String, Object> pain002Response = Map.of(
            "CstmrPmtStsRpt", Map.of(
                "TxInfAndSts", Map.of(
                    "StsId", transactionId.toString(),
                    "TxSts", "ACSC"
                )
            )
        );

        when(iso20022ProcessingService.generatePain002(eq(transactionId), anyString()))
            .thenReturn(pain002Response);

        // When & Then
        mockMvc.perform(get("/api/v1/iso20022/pain002/{transactionId}", transactionId)
                .param("originalMessageId", "MSG-TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CstmrPmtStsRpt.TxInfAndSts.StsId").value(transactionId.toString()))
                .andExpect(jsonPath("$.CstmrPmtStsRpt.TxInfAndSts.TxSts").value("ACSC"));
    }

    @Test
    @DisplayName("Should get batch processing status")
    @WithMockUser(authorities = {"batch:read"})
    void shouldGetBatchStatus() throws Exception {
        // Given
        String batchId = "BULK-TEST-001";
        Map<String, Object> batchStatus = Map.of(
            "batchId", batchId,
            "status", "COMPLETED",
            "totalMessages", 10,
            "successCount", 9,
            "failureCount", 1
        );

        when(iso20022ProcessingService.getBatchStatus(eq(batchId)))
            .thenReturn(batchStatus);

        // When & Then
        mockMvc.perform(get("/api/v1/iso20022/batch/{batchId}/status", batchId))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.batchId").value(batchId))
                .andExpected(jsonPath("$.status").value("COMPLETED"))
                .andExpected(jsonPath("$.successCount").value(9));
    }

    @Test
    @DisplayName("Should handle processing errors gracefully")
    @WithMockUser(authorities = {"payment:create"})
    void shouldHandleProcessingErrors() throws Exception {
        // Given
        when(iso20022ProcessingService.processPain001(any(Pain001Message.class), any(Map.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/iso20022/pain001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePain001)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("PAIN001_PROCESSING_ERROR"));
    }

    @Test
    @DisplayName("Should require proper permissions for cancellation")
    @WithMockUser(authorities = {"payment:read"}) // Wrong permission
    void shouldRequireProperPermissionsForCancellation() throws Exception {
        mockMvc.perform(post("/api/v1/iso20022/camt055")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCamt055)))
                .andExpect(status().isForbidden());
    }

    private void setupSampleMessages() {
        // Setup sample pain.001 message
        samplePain001 = new Pain001Message();
        Pain001Message.CustomerCreditTransferInitiation cctInitiation = new Pain001Message.CustomerCreditTransferInitiation();
        
        // Group Header
        Pain001Message.GroupHeader groupHeader = new Pain001Message.GroupHeader();
        groupHeader.setMessageId("MSG-TEST-001");
        groupHeader.setCreationDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        groupHeader.setNumberOfTransactions("1");
        groupHeader.setControlSum("1000.00");
        
        Party initiatingParty = new Party();
        initiatingParty.setName("Test Corporation");
        groupHeader.setInitiatingParty(initiatingParty);
        
        // Payment Information
        Pain001Message.PaymentInformation paymentInfo = new Pain001Message.PaymentInformation();
        paymentInfo.setPaymentInformationId("PMT-TEST-001");
        paymentInfo.setPaymentMethod("TRF");
        paymentInfo.setRequestedExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        Party debtor = new Party();
        debtor.setName("John Doe");
        paymentInfo.setDebtor(debtor);
        
        Account debtorAccount = new Account();
        Account.AccountIdentification debtorAccountId = new Account.AccountIdentification();
        Account.GenericAccountIdentification genericId = new Account.GenericAccountIdentification();
        genericId.setIdentification("ACC001001");
        debtorAccountId.setOther(genericId);
        debtorAccount.setIdentification(debtorAccountId);
        debtorAccount.setCurrency("USD");
        paymentInfo.setDebtorAccount(debtorAccount);
        
        // Credit Transfer Transaction Information
        CreditTransferTransactionInformation cdtTrfTxInf = new CreditTransferTransactionInformation();
        
        CreditTransferTransactionInformation.PaymentIdentification paymentId = new CreditTransferTransactionInformation.PaymentIdentification();
        paymentId.setEndToEndId("E2E-TEST-001");
        cdtTrfTxInf.setPaymentIdentification(paymentId);
        
        CreditTransferTransactionInformation.Amount amount = new CreditTransferTransactionInformation.Amount();
        CreditTransferTransactionInformation.InstructedAmount instAmount = new CreditTransferTransactionInformation.InstructedAmount();
        instAmount.setCurrency("USD");
        instAmount.setValue(new BigDecimal("1000.00"));
        amount.setInstructedAmount(instAmount);
        cdtTrfTxInf.setAmount(amount);
        
        Party creditor = new Party();
        creditor.setName("Jane Smith");
        cdtTrfTxInf.setCreditor(creditor);
        
        Account creditorAccount = new Account();
        Account.AccountIdentification creditorAccountId = new Account.AccountIdentification();
        Account.GenericAccountIdentification creditorGenericId = new Account.GenericAccountIdentification();
        creditorGenericId.setIdentification("ACC002001");
        creditorAccountId.setOther(creditorGenericId);
        creditorAccount.setIdentification(creditorAccountId);
        cdtTrfTxInf.setCreditorAccount(creditorAccount);
        
        paymentInfo.setCreditTransferTransactionInformation(cdtTrfTxInf);
        cctInitiation.setGroupHeader(groupHeader);
        cctInitiation.setPaymentInformation(paymentInfo);
        samplePain001.setCustomerCreditTransferInitiation(cctInitiation);

        // Setup sample camt.055 message
        sampleCamt055 = new Camt055Message();
        Camt055Message.CustomerPaymentCancellationRequest cxlReq = new Camt055Message.CustomerPaymentCancellationRequest();
        
        Camt055Message.GroupHeader cxlGroupHeader = new Camt055Message.GroupHeader();
        cxlGroupHeader.setMessageId("CANCEL-TEST-001");
        cxlGroupHeader.setCreationDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        cxlGroupHeader.setNumberOfTransactions("1");
        
        Party cxlInitiatingParty = new Party();
        cxlInitiatingParty.setName("Test Corporation");
        cxlGroupHeader.setInitiatingParty(cxlInitiatingParty);
        
        Camt055Message.UnderlyingTransaction underlying = new Camt055Message.UnderlyingTransaction();
        
        Camt055Message.PaymentTransaction paymentTransaction = new Camt055Message.PaymentTransaction();
        paymentTransaction.setCancellationId("CXL-TEST-001");
        paymentTransaction.setOriginalEndToEndId("E2E-TEST-001");
        
        Camt055Message.ActiveCurrencyAndAmount originalAmount = new Camt055Message.ActiveCurrencyAndAmount();
        originalAmount.setCurrency("USD");
        originalAmount.setValue(new BigDecimal("1000.00"));
        paymentTransaction.setOriginalInstructedAmount(originalAmount);
        
        Camt055Message.PaymentCancellationReason reason = new Camt055Message.PaymentCancellationReason();
        Camt055Message.CancellationReason cxlReason = new Camt055Message.CancellationReason();
        cxlReason.setCode("CUST");
        reason.setReason(cxlReason);
        reason.setAdditionalInformation(List.of("Customer requested cancellation"));
        
        paymentTransaction.setCancellationReasonInformation(List.of(reason));
        underlying.setTransactionInformation(List.of(paymentTransaction));
        
        cxlReq.setGroupHeader(cxlGroupHeader);
        cxlReq.setUnderlying(List.of(underlying));
        sampleCamt055.setCustomerPaymentCancellationRequest(cxlReq);
    }
}