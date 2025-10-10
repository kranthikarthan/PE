package com.paymentengine.shared.service;

import com.paymentengine.shared.dto.iso20022.*;
import com.paymentengine.shared.dto.CreateTransactionRequest;
import com.paymentengine.shared.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test for PAIN.001 to PAIN.002 message flow
 * This test demonstrates the complete ISO 20022 message processing pipeline
 */
class Pain001ToPain002FlowTest {

    private Iso20022MessageService messageService;
    private Pain001Message pain001Message;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        messageService = new Iso20022MessageService();
        pain001Message = createSamplePain001Message();
        transactionResponse = createMockTransactionResponse();
    }

    @Test
    void testCompletePain001ToPain002Flow() {
        System.out.println("🚀 Starting PAIN.001 to PAIN.002 Message Flow Test");
        System.out.println("=" .repeat(60));
        
        // Step 1: Transform PAIN.001 to Transaction Request
        System.out.println("📥 Step 1: Processing PAIN.001 Message");
        CreateTransactionRequest transactionRequest = messageService.transformPain001ToTransactionRequest(pain001Message);
        
        // Verify PAIN.001 processing
        assertNotNull(transactionRequest, "Transaction request should not be null");
        assertNotNull(transactionRequest.getExternalReference(), "External reference should not be null");
        assertNotNull(transactionRequest.getAmount(), "Amount should not be null");
        assertNotNull(transactionRequest.getCurrencyCode(), "Currency code should not be null");
        assertEquals("USD", transactionRequest.getCurrencyCode(), "Currency should be USD");
        assertEquals(0, transactionRequest.getAmount().compareTo(BigDecimal.valueOf(100.00)), "Amount should be 100.00");
        
        System.out.println("✅ PAIN.001 processed successfully");
        System.out.println("   📋 External Reference: " + transactionRequest.getExternalReference());
        System.out.println("   💰 Amount: " + transactionRequest.getAmount());
        System.out.println("   💱 Currency: " + transactionRequest.getCurrencyCode());
        System.out.println("   🏦 From Account: " + transactionRequest.getFromAccount());
        System.out.println("   🏦 To Account: " + transactionRequest.getToAccount());
        System.out.println("   📝 Description: " + transactionRequest.getDescription());
        
        // Step 2: Simulate transaction processing (normally done by core banking)
        System.out.println("\n🔄 Step 2: Simulating Transaction Processing");
        System.out.println("   (In a real system, this would be handled by the core banking service)");
        
        // Step 3: Transform Transaction Response to PAIN.002
        System.out.println("\n📤 Step 3: Generating PAIN.002 Response");
        var pain002Response = messageService.transformTransactionResponseToPain002(transactionResponse, "MSG-12345");
        
        // Verify PAIN.002 processing
        assertNotNull(pain002Response, "PAIN.002 response should not be null");
        assertTrue(pain002Response.containsKey("CstmrPmtStsRpt"), "Should contain Customer Payment Status Report");
        
        @SuppressWarnings("unchecked")
        var cstmrPmtStsRpt = (java.util.Map<String, Object>) pain002Response.get("CstmrPmtStsRpt");
        assertNotNull(cstmrPmtStsRpt, "Customer Payment Status Report should not be null");
        assertTrue(cstmrPmtStsRpt.containsKey("GrpHdr"), "Should contain Group Header");
        assertTrue(cstmrPmtStsRpt.containsKey("OrgnlGrpInfAndSts"), "Should contain Original Group Information");
        assertTrue(cstmrPmtStsRpt.containsKey("PmtInfSts"), "Should contain Payment Information Status");
        
        System.out.println("✅ PAIN.002 generated successfully");
        System.out.println("   📊 Response Structure: " + pain002Response.keySet());
        System.out.println("   📋 Group Header: " + cstmrPmtStsRpt.containsKey("GrpHdr"));
        System.out.println("   📋 Original Group Info: " + cstmrPmtStsRpt.containsKey("OrgnlGrpInfAndSts"));
        System.out.println("   📋 Payment Info Status: " + cstmrPmtStsRpt.containsKey("PmtInfSts"));
        
        System.out.println("\n🎉 PAIN.001 to PAIN.002 Message Flow Test COMPLETED SUCCESSFULLY!");
        System.out.println("=" .repeat(60));
    }

    @Test
    void testMessageValidation() {
        System.out.println("🔍 Testing Message Validation");
        
        // Test null handling
        assertThrows(RuntimeException.class, () -> {
            messageService.transformPain001ToTransactionRequest(null);
        }, "Should throw RuntimeException for null PAIN.001");
        
        assertThrows(RuntimeException.class, () -> {
            messageService.transformTransactionResponseToPain002(null, "MSG-12345");
        }, "Should throw RuntimeException for null Transaction Response");
        
        System.out.println("✅ Message validation working correctly");
    }

    @Test
    void testMessageStructure() {
        System.out.println("🏗️  Testing Message Structure");
        
        // Test PAIN.001 structure
        assertNotNull(pain001Message.getCustomerCreditTransferInitiation(), "Customer Credit Transfer Initiation should not be null");
        assertNotNull(pain001Message.getCustomerCreditTransferInitiation().getGroupHeader(), "Group Header should not be null");
        assertNotNull(pain001Message.getCustomerCreditTransferInitiation().getPaymentInformation(), "Payment Information should not be null");
        
        // Test Transaction Response structure
        assertNotNull(transactionResponse.getTransactionId(), "Transaction ID should not be null");
        assertNotNull(transactionResponse.getStatus(), "Status should not be null");
        assertNotNull(transactionResponse.getAmount(), "Amount should not be null");
        
        System.out.println("✅ Message structure validation passed");
    }

    private Pain001Message createSamplePain001Message() {
        Pain001Message message = new Pain001Message();
        
        // Create Customer Credit Transfer Initiation
        Pain001Message.CustomerCreditTransferInitiation cctInitiation = new Pain001Message.CustomerCreditTransferInitiation();
        
        // Create Group Header
        Pain001Message.GroupHeader groupHeader = new Pain001Message.GroupHeader();
        groupHeader.setMessageId("MSG-" + UUID.randomUUID().toString().substring(0, 8));
        groupHeader.setCreationDateTime("2024-01-15T10:30:00.000Z");
        groupHeader.setNumberOfTransactions("1");
        groupHeader.setControlSum("100.00");
        cctInitiation.setGroupHeader(groupHeader);
        
        // Create Payment Information
        Pain001Message.PaymentInformation paymentInfo = new Pain001Message.PaymentInformation();
        paymentInfo.setPaymentInformationId("PAY-" + UUID.randomUUID().toString().substring(0, 8));
        paymentInfo.setPaymentMethod("TRF");
        paymentInfo.setRequestedExecutionDate("2024-01-15");
        paymentInfo.setNumberOfTransactions("1");
        paymentInfo.setControlSum("100.00");
        
        // Create Payment Type Information
        com.paymentengine.shared.dto.iso20022.PaymentTypeInformation paymentTypeInfo = new com.paymentengine.shared.dto.iso20022.PaymentTypeInformation();
        paymentInfo.setPaymentTypeInformation(paymentTypeInfo);
        
        // Create Debtor
        Party debtor = new Party();
        debtor.setName("Test Debtor");
        debtor.setCountryOfResidence("US");
        paymentInfo.setDebtor(debtor);
        
        // Create Debtor Account
        Account debtorAccount = new Account();
        Account.AccountIdentification debtorAccountId = new Account.AccountIdentification();
        debtorAccountId.setIban("GB82WEST12345698765432");
        debtorAccount.setIdentification(debtorAccountId);
        paymentInfo.setDebtorAccount(debtorAccount);
        
        // Create Credit Transfer Transaction Information
        CreditTransferTransactionInformation cdtTrfTxInf = new CreditTransferTransactionInformation();
        
        // Set Payment Identification
        CommonTypes.PaymentIdentification paymentId = new CommonTypes.PaymentIdentification();
        paymentId.setInstructionId("INST-" + UUID.randomUUID().toString().substring(0, 8));
        paymentId.setEndToEndId("E2E-" + UUID.randomUUID().toString().substring(0, 8));
        cdtTrfTxInf.setPaymentIdentification(paymentId);
        
        // Set Amount
        CreditTransferTransactionInformation.Amount amount = new CreditTransferTransactionInformation.Amount();
        CreditTransferTransactionInformation.InstructedAmount instructedAmount = new CreditTransferTransactionInformation.InstructedAmount();
        instructedAmount.setCurrency("USD");
        instructedAmount.setValue(BigDecimal.valueOf(100.00));
        amount.setInstructedAmount(instructedAmount);
        cdtTrfTxInf.setAmount(amount);
        
        // Set Payment Type Information for Credit Transfer
        com.paymentengine.shared.dto.iso20022.PaymentTypeInformation cdtTrfTxInfPmtTpInf = new com.paymentengine.shared.dto.iso20022.PaymentTypeInformation();
        cdtTrfTxInf.setPaymentTypeInformation(cdtTrfTxInfPmtTpInf);
        
        // Set Charge Bearer
        cdtTrfTxInf.setChargeBearer("OUR");
        
        // Set Creditor
        Party creditor = new Party();
        creditor.setName("Test Creditor");
        creditor.setCountryOfResidence("US");
        cdtTrfTxInf.setCreditor(creditor);
        
        // Set Creditor Account
        Account creditorAccount = new Account();
        Account.AccountIdentification creditorAccountId = new Account.AccountIdentification();
        creditorAccountId.setIban("GB82WEST12345698765432");
        creditorAccount.setIdentification(creditorAccountId);
        cdtTrfTxInf.setCreditorAccount(creditorAccount);
        
        // Set Remittance Information
        CommonTypes.RemittanceInformation remittanceInfo = new CommonTypes.RemittanceInformation();
        remittanceInfo.setUnstructured(new String[]{"Payment for services"});
        cdtTrfTxInf.setRemittanceInformation(remittanceInfo);
        
        paymentInfo.setCreditTransferTransactionInformation(cdtTrfTxInf);
        cctInitiation.setPaymentInformation(paymentInfo);
        message.setCustomerCreditTransferInitiation(cctInitiation);
        
        return message;
    }

    private TransactionResponse createMockTransactionResponse() {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8));
        response.setExternalReference("E2E-" + UUID.randomUUID().toString().substring(0, 8));
        response.setStatus("COMPLETED");
        response.setAmount(BigDecimal.valueOf(100.00));
        response.setCurrencyCode("USD");
        response.setDescription("Payment for services");
        response.setCreatedAt(LocalDateTime.now());
        response.setCompletedAt(LocalDateTime.now());
        return response;
    }
}