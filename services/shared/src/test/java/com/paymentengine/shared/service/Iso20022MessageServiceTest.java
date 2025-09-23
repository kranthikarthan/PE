package com.paymentengine.shared.service;

import com.paymentengine.shared.dto.iso20022.*;
import com.paymentengine.shared.dto.CreateTransactionRequest;
import com.paymentengine.shared.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ISO 20022 message processing
 */
@ExtendWith(MockitoExtension.class)
class Iso20022MessageServiceTest {

    @InjectMocks
    private Iso20022MessageService iso20022MessageService;

    private Pain001Message pain001Message;
    private TransactionResponse mockTransactionResponse;

    @BeforeEach
    void setUp() {
        // Create a sample PAIN.001 message
        pain001Message = createSamplePain001Message();
        
        // Create a mock transaction response
        mockTransactionResponse = createMockTransactionResponse();
    }

    @Test
    void testTransformPain001ToTransactionRequest_ShouldCreateValidRequest() {
        // Act
        CreateTransactionRequest result = iso20022MessageService.transformPain001ToTransactionRequest(pain001Message);

        // Assert
        assertNotNull(result, "CreateTransactionRequest should not be null");
        assertNotNull(result.getExternalReference(), "External reference should not be null");
        assertNotNull(result.getAmount(), "Amount should not be null");
        assertNotNull(result.getCurrencyCode(), "Currency code should not be null");
        assertEquals("USD", result.getCurrencyCode(), "Currency should be USD");
        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(100.00)), "Amount should be 100.00");
    }

    @Test
    void testTransformTransactionResponseToPain002_ShouldCreateValidResponse() {
        // Act
        Map<String, Object> result = iso20022MessageService.transformTransactionResponseToPain002(mockTransactionResponse, "MSG-12345");

        // Assert
        assertNotNull(result, "PAIN.002 response should not be null");
        assertTrue(result.containsKey("CstmrPmtStsRpt"), "Should contain Customer Payment Status Report");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cstmrPmtStsRpt = (Map<String, Object>) result.get("CstmrPmtStsRpt");
        assertNotNull(cstmrPmtStsRpt, "Customer Payment Status Report should not be null");
        assertTrue(cstmrPmtStsRpt.containsKey("GrpHdr"), "Should contain Group Header");
        assertTrue(cstmrPmtStsRpt.containsKey("OrgnlGrpInfAndSts"), "Should contain Original Group Information");
        assertTrue(cstmrPmtStsRpt.containsKey("PmtInfSts"), "Should contain Payment Information Status");
    }

    @Test
    void testTransformPain001ToTransactionRequest_WithNullInput_ShouldHandleGracefully() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            iso20022MessageService.transformPain001ToTransactionRequest(null);
        });
    }

    @Test
    void testTransformTransactionResponseToPain002_WithNullInput_ShouldHandleGracefully() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            iso20022MessageService.transformTransactionResponseToPain002(null, "MSG-12345");
        });
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