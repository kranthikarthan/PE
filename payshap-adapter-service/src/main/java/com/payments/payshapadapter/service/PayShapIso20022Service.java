package com.payments.payshapadapter.service;

import com.payments.payshapadapter.domain.PayShapPaymentMessage;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for handling PayShap ISO 20022 message processing */
@Service
@Slf4j
public class PayShapIso20022Service {

  /** Build pacs.008 message for PayShap payments */
  public String buildPacs008Message(PayShapPaymentMessage paymentMessage) {
    log.debug(
        "Building pacs.008 message for PayShap payment: {}", paymentMessage.getTransactionId());

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">");
    xml.append("<FIToFICstmrCdtTrf>");
    xml.append("<GrpHdr>");
    xml.append("<MsgId>").append(paymentMessage.getMessageId()).append("</MsgId>");
    xml.append("<CreDtTm>").append(Instant.now()).append("</CreDtTm>");
    xml.append("<NbOfTxs>1</NbOfTxs>");
    xml.append("<SttlmInf>");
    xml.append("<SttlmMtd>INDA</SttlmMtd>");
    xml.append("</SttlmInf>");
    xml.append("</GrpHdr>");
    xml.append("<CdtTrfTxInf>");
    xml.append("<PmtId>");
    xml.append("<TxId>").append(paymentMessage.getTransactionId()).append("</TxId>");
    xml.append("<EndToEndId>").append(paymentMessage.getEndToEndId()).append("</EndToEndId>");
    xml.append("</PmtId>");
    xml.append("<IntrBkSttlmAmt Ccy=\"")
        .append(paymentMessage.getCurrency())
        .append("\">")
        .append(paymentMessage.getAmount())
        .append("</IntrBkSttlmAmt>");
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
    xml.append("<RmtInf>");
    xml.append("<Ustrd>").append(paymentMessage.getPaymentPurpose()).append("</Ustrd>");
    xml.append("</RmtInf>");
    xml.append("</CdtTrfTxInf>");
    xml.append("</FIToFICstmrCdtTrf>");
    xml.append("</Document>");

    return xml.toString();
  }

  /** Parse pacs.002 response for PayShap payments */
  public PayShapPaymentResponse parsePacs002Response(String responseXml) {
    log.debug("Parsing pacs.002 response for PayShap payment");

    try {
      // Simple XML parsing - in production, use proper XML parser
      String status = extractValue(responseXml, "<TxSts>", "</TxSts>");
      String reason = extractValue(responseXml, "<Cd>", "</Cd>");

      return PayShapPaymentResponse.builder()
          .status(status)
          .reason(reason)
          .processedAt(Instant.now())
          .build();
    } catch (Exception e) {
      log.error("Error parsing pacs.002 response", e);
      return PayShapPaymentResponse.builder()
          .status(null)
          .reason(null)
          .processedAt(Instant.now())
          .build();
    }
  }

  /** Build pacs.002 acknowledgment for PayShap payments */
  public String buildPacs002Acknowledgment(String originalMessageId, String status, String reason) {
    log.debug("Building pacs.002 acknowledgment for PayShap payment: {}", originalMessageId);

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10\">");
    xml.append("<FIToFIPmtStsRpt>");
    xml.append("<GrpHdr>");
    xml.append("<MsgId>ACK-").append(originalMessageId).append("</MsgId>");
    xml.append("<CreDtTm>").append(Instant.now()).append("</CreDtTm>");
    xml.append("</GrpHdr>");
    xml.append("<TxInfAndSts>");
    xml.append("<StsId>").append(originalMessageId).append("</StsId>");
    xml.append("<OrgnlTxId>").append(originalMessageId).append("</OrgnlTxId>");
    xml.append("<TxSts>").append(status).append("</TxSts>");
    xml.append("<StsRsnInf>");
    xml.append("<Rsn>");
    xml.append("<Cd>").append(reason).append("</Cd>");
    xml.append("</Rsn>");
    xml.append("</StsRsnInf>");
    xml.append("</TxInfAndSts>");
    xml.append("</FIToFIPmtStsRpt>");
    xml.append("</Document>");

    return xml.toString();
  }

  /** Validate ISO 20022 message */
  public boolean validateIso20022Message(String xml) {
    if (xml == null || xml.trim().isEmpty()) {
      return false;
    }

    return xml.contains("<?xml") && xml.contains("<Document") && xml.contains("</Document>");
  }

  /** Extract value from XML between tags */
  private String extractValue(String xml, String startTag, String endTag) {
    int startIndex = xml.indexOf(startTag);
    if (startIndex == -1) {
      return null;
    }
    startIndex += startTag.length();
    int endIndex = xml.indexOf(endTag, startIndex);
    if (endIndex == -1) {
      return null;
    }
    return xml.substring(startIndex, endIndex);
  }

  /** PayShap Payment Response DTO */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayShapPaymentResponse {
    private String status;
    private String reason;
    private Instant processedAt;
  }
}
