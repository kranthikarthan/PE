package com.payments.samosadapter.service;

import com.payments.samosadapter.domain.SamosPaymentMessage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SAMOS ISO 20022 Service
 *
 * <p>Handles ISO 20022 message generation, validation, and processing for SAMOS integration.
 * Supports pacs.008 (Credit Transfer), pacs.002 (Payment Status Report), and camt.054
 * (Notification).
 */
@Service
@Slf4j
public class SamosIso20022Service {

  private static final String ISO20022_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd";
  private static final String ISO20022_VERSION = "2013-05-01";

  /** Generate ISO 20022 message based on message type */
  public String generateMessage(SamosPaymentMessage.MessageType messageType, Object paymentData) {
    log.debug("Generating ISO 20022 message of type: {}", messageType);

    switch (messageType) {
      case PACS_008:
        return generatePacs008Message(paymentData);
      case PACS_002:
        return generatePacs002Message(paymentData);
      case CAMT_054:
        return generateCamt054Message(paymentData);
      default:
        throw new IllegalArgumentException("Unsupported message type: " + messageType);
    }
  }

  /** Validate ISO 20022 message format */
  public boolean validateMessage(
      String iso20022Payload, SamosPaymentMessage.MessageType messageType) {
    log.debug("Validating ISO 20022 message of type: {}", messageType);

    if (iso20022Payload == null || iso20022Payload.isBlank()) {
      return false;
    }

    // Basic XML validation
    if (!iso20022Payload.trim().startsWith("<?xml")) {
      return false;
    }

    // Message type specific validation
    switch (messageType) {
      case PACS_008:
        return validatePacs008Message(iso20022Payload);
      case PACS_002:
        return validatePacs002Message(iso20022Payload);
      case CAMT_054:
        return validateCamt054Message(iso20022Payload);
      default:
        return false;
    }
  }

  /** Generate payload hash for integrity checking */
  public String generatePayloadHash(String payload) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));

      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to generate payload hash", e);
      throw new RuntimeException("Failed to generate payload hash", e);
    }
  }

  /** Generate pacs.008 (Credit Transfer) message */
  private String generatePacs008Message(Object paymentData) {
    // TODO: Implement actual ISO 20022 pacs.008 message generation
    // This would typically use JAXB or similar to generate XML from payment data
    log.debug("Generating pacs.008 message for payment data: {}", paymentData);

    return String.format(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="%s:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG-%s</MsgId>
                        <CreDtTm>%s</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                        <SttlmInf>
                            <SttlmMtd>INDA</SttlmMtd>
                        </SttlmInf>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <TxId>%s</TxId>
                        </PmtId>
                        <IntrBkSttlmAmt Ccy="ZAR">%s</IntrBkSttlmAmt>
                        <ChrgBr>DEBT</ChrgBr>
                        <Dbtr>
                            <Nm>%s</Nm>
                        </Dbtr>
                        <DbtrAcct>
                            <Id>
                                <IBAN>%s</IBAN>
                            </Id>
                        </DbtrAcct>
                        <DbtrAgt>
                            <FinInstnId>
                                <BICFI>%s</BICFI>
                            </FinInstnId>
                        </DbtrAgt>
                        <Cdtr>
                            <Nm>%s</Nm>
                        </Cdtr>
                        <CdtrAcct>
                            <Id>
                                <IBAN>%s</IBAN>
                            </Id>
                        </CdtrAcct>
                        <CdtrAgt>
                            <FinInstnId>
                                <BICFI>%s</BICFI>
                            </FinInstnId>
                        </CdtrAgt>
                        <RmtInf>
                            <Ustrd>%s</Ustrd>
                        </RmtInf>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """,
        ISO20022_NAMESPACE,
        System.currentTimeMillis(),
        java.time.Instant.now().toString(),
        "TXN-" + System.currentTimeMillis(),
        "1000.00",
        "Debtor Name",
        "ZA12345678901234567890",
        "DEUTZAJJ",
        "Creditor Name",
        "ZA98765432109876543210",
        "NEDZAZAJ",
        "Payment reference");
  }

  /** Generate pacs.002 (Payment Status Report) message */
  private String generatePacs002Message(Object paymentData) {
    log.debug("Generating pacs.002 message for payment data: {}", paymentData);

    return String.format(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="%s:pacs.002.001.10">
                <FIToFIPmtStsRpt>
                    <GrpHdr>
                        <MsgId>STATUS-%s</MsgId>
                        <CreDtTm>%s</CreDtTm>
                        <InstgAgt>
                            <FinInstnId>
                                <BICFI>%s</BICFI>
                            </FinInstnId>
                        </InstgAgt>
                        <InstdAgt>
                            <FinInstnId>
                                <BICFI>%s</BICFI>
                            </FinInstnId>
                        </InstdAgt>
                    </GrpHdr>
                    <TxInfAndSts>
                        <StsId>%s</StsId>
                        <OrgnlGrpInf>
                            <OrgnlMsgId>%s</OrgnlMsgId>
                            <OrgnlMsgNmId>pacs.008.001.08</OrgnlMsgNmId>
                        </OrgnlGrpInf>
                        <TxSts>%s</TxSts>
                        <StsRsnInf>
                            <Orgtr>
                                <Nm>%s</Nm>
                            </Orgtr>
                            <Rsn>
                                <Cd>%s</Cd>
                            </Rsn>
                        </StsRsnInf>
                    </TxInfAndSts>
                </FIToFIPmtStsRpt>
            </Document>
            """,
        ISO20022_NAMESPACE,
        System.currentTimeMillis(),
        java.time.Instant.now().toString(),
        "DEUTZAJJ",
        "NEDZAZAJ",
        "STATUS-" + System.currentTimeMillis(),
        "MSG-" + System.currentTimeMillis(),
        "ACSC",
        "SAMOS",
        "NARR");
  }

  /** Generate camt.054 (Bank to Customer Debit/Credit Notification) message */
  private String generateCamt054Message(Object paymentData) {
    log.debug("Generating camt.054 message for payment data: {}", paymentData);

    return String.format(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="%s:camt.054.001.08">
                <BkToCstmrDbtCdtNtfctn>
                    <GrpHdr>
                        <MsgId>NOTIF-%s</MsgId>
                        <CreDtTm>%s</CreDtTm>
                        <MsgRcpt>
                            <Nm>%s</Nm>
                        </MsgRcpt>
                        <MsgSndr>
                            <Nm>%s</Nm>
                        </MsgSndr>
                    </GrpHdr>
                    <Ntfctn>
                        <Id>%s</Id>
                        <CreDtTm>%s</CreDtTm>
                        <Acct>
                            <Id>
                                <IBAN>%s</IBAN>
                            </Id>
                        </Acct>
                        <Ntry>
                            <Amt Ccy="ZAR">%s</Amt>
                            <CdtDbtInd>%s</CdtDbtInd>
                            <Sts>%s</Sts>
                            <BookgDt>
                                <Dt>%s</Dt>
                            </BookgDt>
                            <ValDt>
                                <Dt>%s</Dt>
                            </ValDt>
                            <AcctSvcrRef>%s</AcctSvcrRef>
                            <BkTxCd>
                                <Prtry>
                                    <Cd>%s</Cd>
                                </Prtry>
                            </BkTxCd>
                        </Ntry>
                    </Ntfctn>
                </BkToCstmrDbtCdtNtfctn>
            </Document>
            """,
        ISO20022_NAMESPACE,
        System.currentTimeMillis(),
        java.time.Instant.now().toString(),
        "Customer Name",
        "Bank Name",
        "NOTIF-" + System.currentTimeMillis(),
        java.time.Instant.now().toString(),
        "ZA12345678901234567890",
        "1000.00",
        "CRDT",
        "BOOK",
        java.time.LocalDate.now().toString(),
        java.time.LocalDate.now().toString(),
        "REF-" + System.currentTimeMillis(),
        "SAMOS");
  }

  /** Validate pacs.008 message */
  private boolean validatePacs008Message(String payload) {
    return payload.contains("FIToFICstmrCdtTrf")
        && payload.contains("CdtTrfTxInf")
        && payload.contains("IntrBkSttlmAmt");
  }

  /** Validate pacs.002 message */
  private boolean validatePacs002Message(String payload) {
    return payload.contains("FIToFIPmtStsRpt")
        && payload.contains("TxInfAndSts")
        && payload.contains("TxSts");
  }

  /** Validate camt.054 message */
  private boolean validateCamt054Message(String payload) {
    return payload.contains("BkToCstmrDbtCdtNtfctn")
        && payload.contains("Ntfctn")
        && payload.contains("Ntry");
  }
}
