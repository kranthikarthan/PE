package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.shared.dto.iso20022.Pain001Message;
import com.paymentengine.shared.dto.CreateTransactionRequest;
import com.paymentengine.shared.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ISO 20022 controller
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Iso20022ControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testHealthEndpoint() {
        String url = "http://localhost:" + port + "/payment-processing/api/v1/iso20022/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("UP"));
    }

    @Test
    void testPain001Processing() {
        // Create a simple PAIN.001 message
        Pain001Message pain001Message = createSamplePain001Message();
        
        String url = "http://localhost:" + port + "/payment-processing/api/v1/iso20022/pain001/process";
        ResponseEntity<CreateTransactionRequest> response = restTemplate.postForEntity(url, pain001Message, CreateTransactionRequest.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getExternalReference());
        assertNotNull(response.getBody().getAmount());
        assertNotNull(response.getBody().getCurrencyCode());
    }

    @Test
    void testCompleteFlow() {
        // Create a simple PAIN.001 message
        Pain001Message pain001Message = createSamplePain001Message();
        
        String url = "http://localhost:" + port + "/payment-processing/api/v1/iso20022/flow/complete";
        ResponseEntity<String> response = restTemplate.postForEntity(url, pain001Message, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("CstmrPmtStsRpt"));
    }

    private Pain001Message createSamplePain001Message() {
        Pain001Message message = new Pain001Message();
        Pain001Message.CustomerCreditTransferInitiation cctInitiation = new Pain001Message.CustomerCreditTransferInitiation();
        Pain001Message.GroupHeader groupHeader = new Pain001Message.GroupHeader();
        groupHeader.setMessageId("MSG-12345");
        groupHeader.setCreationDateTime("2024-01-15T10:30:00.000Z");
        groupHeader.setNumberOfTransactions("1");
        groupHeader.setControlSum("100.00");
        cctInitiation.setGroupHeader(groupHeader);

        Pain001Message.PaymentInformation paymentInfo = new Pain001Message.PaymentInformation();
        paymentInfo.setPaymentInformationId("PAY-12345");
        paymentInfo.setPaymentMethod("TRF");
        paymentInfo.setRequestedExecutionDate("2024-01-15");
        paymentInfo.setNumberOfTransactions("1");
        paymentInfo.setControlSum("100.00");
        com.paymentengine.shared.dto.iso20022.PaymentTypeInformation paymentTypeInfo = new com.paymentengine.shared.dto.iso20022.PaymentTypeInformation();
        paymentInfo.setPaymentTypeInformation(paymentTypeInfo);

        com.paymentengine.shared.dto.iso20022.Party debtor = new com.paymentengine.shared.dto.iso20022.Party();
        debtor.setName("Test Debtor");
        debtor.setCountryOfResidence("US");
        paymentInfo.setDebtor(debtor);

        com.paymentengine.shared.dto.iso20022.Account debtorAccount = new com.paymentengine.shared.dto.iso20022.Account();
        com.paymentengine.shared.dto.iso20022.Account.AccountIdentification debtorAccountId = new com.paymentengine.shared.dto.iso20022.Account.AccountIdentification();
        debtorAccountId.setIban("GB82WEST12345698765432");
        debtorAccount.setIdentification(debtorAccountId);
        paymentInfo.setDebtorAccount(debtorAccount);

        com.paymentengine.shared.dto.iso20022.CreditTransferTransactionInformation cdtTrfTxInf = new com.paymentengine.shared.dto.iso20022.CreditTransferTransactionInformation();
        com.paymentengine.shared.dto.iso20022.CommonTypes.PaymentIdentification paymentId = new com.paymentengine.shared.dto.iso20022.CommonTypes.PaymentIdentification();
        paymentId.setInstructionId("INST-12345");
        paymentId.setEndToEndId("E2E-12345");
        cdtTrfTxInf.setPaymentIdentification(paymentId);

        com.paymentengine.shared.dto.iso20022.CreditTransferTransactionInformation.Amount amount = new com.paymentengine.shared.dto.iso20022.CreditTransferTransactionInformation.Amount();
        com.paymentengine.shared.dto.iso20022.CreditTransferTransactionInformation.InstructedAmount instructedAmount = new com.paymentengine.shared.dto.iso20022.CreditTransferTransactionInformation.InstructedAmount();
        instructedAmount.setCurrency("USD");
        instructedAmount.setValue(java.math.BigDecimal.valueOf(100.00));
        amount.setInstructedAmount(instructedAmount);
        cdtTrfTxInf.setAmount(amount);

        com.paymentengine.shared.dto.iso20022.PaymentTypeInformation cdtTrfTxInfPmtTpInf = new com.paymentengine.shared.dto.iso20022.PaymentTypeInformation();
        cdtTrfTxInf.setPaymentTypeInformation(cdtTrfTxInfPmtTpInf);
        cdtTrfTxInf.setChargeBearer("OUR");

        com.paymentengine.shared.dto.iso20022.Party creditor = new com.paymentengine.shared.dto.iso20022.Party();
        creditor.setName("Test Creditor");
        creditor.setCountryOfResidence("US");
        cdtTrfTxInf.setCreditor(creditor);

        com.paymentengine.shared.dto.iso20022.Account creditorAccount = new com.paymentengine.shared.dto.iso20022.Account();
        com.paymentengine.shared.dto.iso20022.Account.AccountIdentification creditorAccountId = new com.paymentengine.shared.dto.iso20022.Account.AccountIdentification();
        creditorAccountId.setIban("GB82WEST12345698765432");
        creditorAccount.setIdentification(creditorAccountId);
        cdtTrfTxInf.setCreditorAccount(creditorAccount);

        com.paymentengine.shared.dto.iso20022.CommonTypes.RemittanceInformation remittanceInfo = new com.paymentengine.shared.dto.iso20022.CommonTypes.RemittanceInformation();
        remittanceInfo.setUnstructured(new String[]{"Payment for services"});
        cdtTrfTxInf.setRemittanceInformation(remittanceInfo);

        paymentInfo.setCreditTransferTransactionInformation(cdtTrfTxInf);
        cctInitiation.setPaymentInformation(paymentInfo);
        message.setCustomerCreditTransferInitiation(cctInitiation);
        return message;
    }
}