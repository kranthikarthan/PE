package com.payments.rtcadapter.service;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcPaymentMessage;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for RtcIso20022Service */
@ExtendWith(MockitoExtension.class)
class RtcIso20022ServiceTest {

  @InjectMocks private RtcIso20022Service rtcIso20022Service;

  private RtcPaymentMessage rtcPaymentMessage;

  @BeforeEach
  void setUp() {
    rtcPaymentMessage =
        RtcPaymentMessage.builder()
            .id(ClearingMessageId.generate())
            .rtcAdapterId("adapter-123")
            .transactionId("TXN-123")
            .messageType("pacs.008")
            .direction("OUTBOUND")
            .messageId("MSG-123")
            .instructionId("INST-123")
            .endToEndId("E2E-123")
            .transactionType("CREDIT")
            .amount(new BigDecimal("1500.00"))
            .currency("ZAR")
            .debtorName("John Doe")
            .debtorAccount("1234567890")
            .debtorBankCode("BANK001")
            .creditorName("Jane Smith")
            .creditorAccount("0987654321")
            .creditorBankCode("BANK002")
            .paymentPurpose("Payment for services")
            .reference("REF-123")
            .status("PENDING")
            .createdAt(Instant.now())
            .build();
  }

  @Test
  void buildPacs008Message_ShouldReturnValidXml_WhenValidPaymentMessage() {
    // When
    String result = rtcIso20022Service.buildPacs008Message(rtcPaymentMessage);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("<?xml"));
    assertTrue(result.contains("<Document"));
    assertTrue(result.contains("</Document>"));
    assertTrue(result.contains("pacs.008"));
    assertTrue(result.contains(rtcPaymentMessage.getTransactionId()));
    assertTrue(result.contains(rtcPaymentMessage.getAmount().toString()));
    assertTrue(result.contains(rtcPaymentMessage.getDebtorName()));
    assertTrue(result.contains(rtcPaymentMessage.getCreditorName()));
  }

  @Test
  void buildPacs008Message_ShouldContainRequiredElements_WhenValidPaymentMessage() {
    // When
    String result = rtcIso20022Service.buildPacs008Message(rtcPaymentMessage);

    // Then
    assertTrue(result.contains("<MsgId>MSG-123</MsgId>"));
    assertTrue(result.contains("<TxId>TXN-123</TxId>"));
    assertTrue(result.contains("<EndToEndId>E2E-123</EndToEndId>"));
    assertTrue(result.contains("<IntrBkSttlmAmt Ccy=\"ZAR\">1500.00</IntrBkSttlmAmt>"));
    assertTrue(result.contains("<Nm>John Doe</Nm>"));
    assertTrue(result.contains("<Nm>Jane Smith</Nm>"));
  }

  @Test
  void parsePacs002Response_ShouldReturnResponse_WhenValidXml() {
    // Given
    String responseXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10\">"
            + "<FIToFIPmtStsRpt>"
            + "<GrpHdr><MsgId>ACK-123</MsgId></GrpHdr>"
            + "<TxInfAndSts>"
            + "<StsId>123</StsId>"
            + "<OrgnlTxId>123</OrgnlTxId>"
            + "<TxSts>ACCP</TxSts>"
            + "<StsRsnInf><Rsn><Cd>NARR</Cd></Rsn></StsRsnInf>"
            + "</TxInfAndSts>"
            + "</FIToFIPmtStsRpt>"
            + "</Document>";

    // When
    RtcIso20022Service.RtcPaymentResponse result =
        rtcIso20022Service.parsePacs002Response(responseXml);

    // Then
    assertNotNull(result);
    assertEquals("ACCP", result.getStatus());
    assertEquals("NARR", result.getReason());
    assertNotNull(result.getProcessedAt());
  }

  @Test
  void parsePacs002Response_ShouldReturnNullValues_WhenInvalidXml() {
    // Given
    String responseXml = "invalid xml";

    // When
    RtcIso20022Service.RtcPaymentResponse result =
        rtcIso20022Service.parsePacs002Response(responseXml);

    // Then
    assertNotNull(result);
    assertNull(result.getStatus());
    assertNull(result.getReason());
    assertNotNull(result.getProcessedAt());
  }

  @Test
  void buildPacs002Acknowledgment_ShouldReturnValidXml_WhenValidInput() {
    // Given
    String originalMessageId = "MSG-123";
    String status = "ACCP";
    String reason = "NARR";

    // When
    String result =
        rtcIso20022Service.buildPacs002Acknowledgment(originalMessageId, status, reason);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("<?xml"));
    assertTrue(result.contains("<Document"));
    assertTrue(result.contains("</Document>"));
    assertTrue(result.contains("pacs.002"));
    assertTrue(result.contains("ACK-MSG-123"));
    assertTrue(result.contains("<TxSts>ACCP</TxSts>"));
    assertTrue(result.contains("<Cd>NARR</Cd>"));
  }

  @Test
  void buildPacs002Acknowledgment_ShouldContainRequiredElements_WhenValidInput() {
    // Given
    String originalMessageId = "MSG-123";
    String status = "ACCP";
    String reason = "NARR";

    // When
    String result =
        rtcIso20022Service.buildPacs002Acknowledgment(originalMessageId, status, reason);

    // Then
    assertTrue(result.contains("<MsgId>ACK-MSG-123</MsgId>"));
    assertTrue(result.contains("<StsId>MSG-123</StsId>"));
    assertTrue(result.contains("<OrgnlTxId>MSG-123</OrgnlTxId>"));
    assertTrue(result.contains("<TxSts>ACCP</TxSts>"));
    assertTrue(result.contains("<Cd>NARR</Cd>"));
  }

  @Test
  void validateIso20022Message_ShouldReturnTrue_WhenValidXml() {
    // Given
    String validXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">"
            + "<FIToFICstmrCdtTrf></FIToFICstmrCdtTrf>"
            + "</Document>";

    // When
    boolean result = rtcIso20022Service.validateIso20022Message(validXml);

    // Then
    assertTrue(result);
  }

  @Test
  void validateIso20022Message_ShouldReturnFalse_WhenInvalidXml() {
    // Given
    String invalidXml = "invalid xml";

    // When
    boolean result = rtcIso20022Service.validateIso20022Message(invalidXml);

    // Then
    assertFalse(result);
  }

  @Test
  void validateIso20022Message_ShouldReturnFalse_WhenNullXml() {
    // When
    boolean result = rtcIso20022Service.validateIso20022Message(null);

    // Then
    assertFalse(result);
  }

  @Test
  void validateIso20022Message_ShouldReturnFalse_WhenEmptyXml() {
    // When
    boolean result = rtcIso20022Service.validateIso20022Message("");

    // Then
    assertFalse(result);
  }

  @Test
  void validateIso20022Message_ShouldReturnFalse_WhenMissingDocumentTag() {
    // Given
    String xmlWithoutDocument =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<FIToFICstmrCdtTrf></FIToFICstmrCdtTrf>";

    // When
    boolean result = rtcIso20022Service.validateIso20022Message(xmlWithoutDocument);

    // Then
    assertFalse(result);
  }

  @Test
  void validateIso20022Message_ShouldReturnFalse_WhenMissingXmlDeclaration() {
    // Given
    String xmlWithoutDeclaration =
        "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">"
            + "<FIToFICstmrCdtTrf></FIToFICstmrCdtTrf>"
            + "</Document>";

    // When
    boolean result = rtcIso20022Service.validateIso20022Message(xmlWithoutDeclaration);

    // Then
    assertFalse(result);
  }
}
