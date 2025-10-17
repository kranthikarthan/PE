package com.payments.rtcadapter.service;

import com.payments.rtcadapter.domain.RtcPaymentMessage;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for handling RTC ISO 20022 message processing */
@Service
@RequiredArgsConstructor
@Slf4j
public class RtcIso20022Service {

  /** Build ISO 20022 pacs.008 message for RTC payment */
  public String buildPacs008Message(RtcPaymentMessage paymentMessage) {
    log.info(
        "Building ISO 20022 pacs.008 message for RTC payment: {}",
        paymentMessage.getTransactionId());

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">");
    xml.append("<FIToFICstmrCdtTrf>");

    // Group Header
    xml.append("<GrpHdr>");
    xml.append("<MsgId>").append(paymentMessage.getMessageId()).append("</MsgId>");
    xml.append("<CreDtTm>").append(Instant.now().toString()).append("</CreDtTm>");
    xml.append("<NbOfTxs>1</NbOfTxs>");
    xml.append("<SttlmInf>");
    xml.append("<SttlmMtd>INDA</SttlmMtd>");
    xml.append("</SttlmInf>");
    xml.append("</GrpHdr>");

    // Credit Transfer Transaction Information
    xml.append("<CdtTrfTxInf>");
    xml.append("<PmtId>");
    xml.append("<TxId>").append(paymentMessage.getTransactionId()).append("</TxId>");
    xml.append("<EndToEndId>").append(paymentMessage.getEndToEndId()).append("</EndToEndId>");
    xml.append("</PmtId>");

    // Payment Type Information
    xml.append("<PmtTpInf>");
    xml.append("<SvcLvl>");
    xml.append("<Cd>SEPA</Cd>");
    xml.append("</SvcLvl>");
    xml.append("<LclInstrm>");
    xml.append("<Cd>RTC</Cd>");
    xml.append("</LclInstrm>");
    xml.append("</PmtTpInf>");

    // Amount
    xml.append("<IntrBkSttlmAmt Ccy=\"").append(paymentMessage.getCurrency()).append("\">");
    xml.append(paymentMessage.getAmount().toString());
    xml.append("</IntrBkSttlmAmt>");

    // Debtor Information
    xml.append("<Dbtr>");
    xml.append("<Nm>").append(paymentMessage.getDebtorName()).append("</Nm>");
    xml.append("</Dbtr>");
    xml.append("<DbtrAcct>");
    xml.append("<Id>");
    xml.append("<Othr>");
    xml.append("<Id>").append(paymentMessage.getDebtorAccount()).append("</Id>");
    xml.append("</Othr>");
    xml.append("</Id>");
    xml.append("</DbtrAcct>");
    xml.append("<DbtrAgt>");
    xml.append("<FinInstnId>");
    xml.append("<BICFI>").append(paymentMessage.getDebtorBankCode()).append("</BICFI>");
    xml.append("</FinInstnId>");
    xml.append("</DbtrAgt>");

    // Creditor Information
    xml.append("<Cdtr>");
    xml.append("<Nm>").append(paymentMessage.getCreditorName()).append("</Nm>");
    xml.append("</Cdtr>");
    xml.append("<CdtrAcct>");
    xml.append("<Id>");
    xml.append("<Othr>");
    xml.append("<Id>").append(paymentMessage.getCreditorAccount()).append("</Id>");
    xml.append("</Othr>");
    xml.append("</Id>");
    xml.append("</CdtrAcct>");
    xml.append("<CdtrAgt>");
    xml.append("<FinInstnId>");
    xml.append("<BICFI>").append(paymentMessage.getCreditorBankCode()).append("</BICFI>");
    xml.append("</FinInstnId>");
    xml.append("</CdtrAgt>");

    // Remittance Information
    if (paymentMessage.getPaymentPurpose() != null) {
      xml.append("<RmtInf>");
      xml.append("<Ustrd>").append(paymentMessage.getPaymentPurpose()).append("</Ustrd>");
      xml.append("</RmtInf>");
    }

    xml.append("</CdtTrfTxInf>");
    xml.append("</FIToFICstmrCdtTrf>");
    xml.append("</Document>");

    log.info(
        "ISO 20022 pacs.008 message built successfully for RTC payment: {}",
        paymentMessage.getTransactionId());
    return xml.toString();
  }

  /** Parse ISO 20022 pacs.002 response message */
  public RtcPaymentResponse parsePacs002Response(String responseXml) {
    log.info("Parsing ISO 20022 pacs.002 response message");

    // Simple XML parsing - in production, use proper XML parser
    String status = extractValue(responseXml, "Sts");
    String reason = extractValue(responseXml, "Rsn");
    String responseCode = extractValue(responseXml, "Cd");
    String responseMessage = extractValue(responseXml, "AddtlInf");

    return RtcPaymentResponse.builder()
        .status(status)
        .reason(reason)
        .responseCode(responseCode)
        .responseMessage(responseMessage)
        .processedAt(Instant.now())
        .build();
  }

  /** Build ISO 20022 pacs.002 acknowledgment message */
  public String buildPacs002Acknowledgment(String originalMessageId, String status, String reason) {
    log.info("Building ISO 20022 pacs.002 acknowledgment for message: {}", originalMessageId);

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10\">");
    xml.append("<FIToFIPmtStsRpt>");

    // Group Header
    xml.append("<GrpHdr>");
    xml.append("<MsgId>").append("ACK-" + originalMessageId).append("</MsgId>");
    xml.append("<CreDtTm>").append(Instant.now().toString()).append("</CreDtTm>");
    xml.append("<NbOfTxs>1</NbOfTxs>");
    xml.append("</GrpHdr>");

    // Transaction Information and Status
    xml.append("<TxInfAndSts>");
    xml.append("<StsId>").append(originalMessageId).append("</StsId>");
    xml.append("<OrgnlTxId>").append(originalMessageId).append("</OrgnlTxId>");
    xml.append("<TxSts>").append(status).append("</TxSts>");
    if (reason != null) {
      xml.append("<StsRsnInf>");
      xml.append("<Rsn>");
      xml.append("<Cd>").append(reason).append("</Cd>");
      xml.append("</Rsn>");
      xml.append("</StsRsnInf>");
    }
    xml.append("</TxInfAndSts>");

    xml.append("</FIToFIPmtStsRpt>");
    xml.append("</Document>");

    log.info(
        "ISO 20022 pacs.002 acknowledgment built successfully for message: {}", originalMessageId);
    return xml.toString();
  }

  /** Validate ISO 20022 message format */
  public boolean validateIso20022Message(String messageXml) {
    if (messageXml == null || messageXml.trim().isEmpty()) {
      return false;
    }

    // Basic validation - check for required elements
    return messageXml.contains("<?xml")
        && messageXml.contains("<Document")
        && messageXml.contains("</Document>");
  }

  /** Extract value from XML using simple string parsing */
  private String extractValue(String xml, String tagName) {
    String startTag = "<" + tagName + ">";
    String endTag = "</" + tagName + ">";

    int startIndex = xml.indexOf(startTag);
    if (startIndex == -1) {
      return null;
    }

    int endIndex = xml.indexOf(endTag, startIndex);
    if (endIndex == -1) {
      return null;
    }

    return xml.substring(startIndex + startTag.length(), endIndex);
  }

  /** RTC Payment Response DTO */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class RtcPaymentResponse {
    private String status;
    private String reason;
    private String responseCode;
    private String responseMessage;
    private Instant processedAt;
  }
}
