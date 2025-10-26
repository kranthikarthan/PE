package com.payments.iso20022.pain002;

import com.payments.iso20022.canonical.CanonicalPaymentModel;
import com.payments.iso20022.service.Iso20022MarshallerService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simplified Builder for ISO 20022 pain.002 messages
 *
 * <p>Builds pain.002 Payment Status Report messages from canonical payment models and status
 * information. This is a minimal implementation that focuses on core functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain002MessageBuilder {

  private final Iso20022MarshallerService marshallerService;

  /** Build pain.002 XML from canonical payment model */
  public String build(CanonicalPaymentModel paymentModel) {
    return buildPain002Xml(paymentModel);
  }

  /** Build pain.002 JSON from canonical payment model */
  public String buildJson(CanonicalPaymentModel paymentModel) {
    return buildPain002Json(paymentModel);
  }

  /** Build pain.002 XML message */
  private String buildPain002Xml(CanonicalPaymentModel paymentModel) {
    log.debug("Building pain.002 XML for payment: {}", paymentModel.getPaymentId());

    try {
      // For now, return a basic XML structure
      // In a real implementation, you would create the full JAXB document
      String xml =
          String.format(
              """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.14">
                    <CstmrPmtStsRpt>
                        <GrpHdr>
                            <MsgId>%s</MsgId>
                            <CreDtTm>%s</CreDtTm>
                        </GrpHdr>
                        <OrgnlGrpInfAndSts>
                            <OrgnlMsgId>%s</OrgnlMsgId>
                            <OrgnlMsgNmId>pain.001.001.12</OrgnlMsgNmId>
                            <GrpSts>%s</GrpSts>
                        </OrgnlGrpInfAndSts>
                        <TxInfAndSts>
                            <StsId>%s</StsId>
                            <OrgnlEndToEndId>%s</OrgnlEndToEndId>
                            <TxSts>%s</TxSts>
                            <StsRsnInf>
                                <Rsn>
                                    <Cd>%s</Cd>
                                </Rsn>
                            </StsRsnInf>
                        </TxInfAndSts>
                    </CstmrPmtStsRpt>
                </Document>
                """,
              paymentModel.getPaymentId(),
              Instant.now().toString(),
              paymentModel.getOriginalMessageId(),
              mapStatusToGroupStatus(paymentModel.getStatus()),
              paymentModel.getTransactionId(),
              paymentModel.getEndToEndId(),
              mapStatusToTransactionStatus(paymentModel.getStatus()),
              mapStatusToReasonCode(paymentModel.getStatus()));

      log.debug("Successfully built pain.002 XML for payment: {}", paymentModel.getPaymentId());
      return xml;

    } catch (Exception e) {
      log.error("Failed to build pain.002 XML for payment: {}", paymentModel.getPaymentId(), e);
      throw new IllegalArgumentException("Failed to build pain.002 XML: " + e.getMessage(), e);
    }
  }

  /** Build pain.002 JSON message */
  private String buildPain002Json(CanonicalPaymentModel paymentModel) {
    log.debug("Building pain.002 JSON for payment: {}", paymentModel.getPaymentId());

    try {
      // Return a basic JSON structure
      String json =
          String.format(
              """
                {
                    "GrpHdr": {
                        "MsgId": "%s",
                        "CreDtTm": "%s"
                    },
                    "OrgnlGrpInfAndSts": {
                        "OrgnlMsgId": "%s",
                        "OrgnlMsgNmId": "pain.001.001.12",
                        "GrpSts": "%s"
                    },
                    "TxInfAndSts": [{
                        "StsId": "%s",
                        "OrgnlEndToEndId": "%s",
                        "TxSts": "%s",
                        "StsRsnInf": {
                            "Rsn": {
                                "Cd": "%s"
                            }
                        }
                    }]
                }
                """,
              paymentModel.getPaymentId(),
              Instant.now().toString(),
              paymentModel.getOriginalMessageId(),
              mapStatusToGroupStatus(paymentModel.getStatus()),
              paymentModel.getTransactionId(),
              paymentModel.getEndToEndId(),
              mapStatusToTransactionStatus(paymentModel.getStatus()),
              mapStatusToReasonCode(paymentModel.getStatus()));

      log.debug("Successfully built pain.002 JSON for payment: {}", paymentModel.getPaymentId());
      return json;

    } catch (Exception e) {
      log.error("Failed to build pain.002 JSON for payment: {}", paymentModel.getPaymentId(), e);
      throw new IllegalArgumentException("Failed to build pain.002 JSON: " + e.getMessage(), e);
    }
  }

  /** Map canonical payment status to group status */
  private String mapStatusToGroupStatus(CanonicalPaymentModel.PaymentStatus status) {
    return switch (status) {
      case PENDING -> "PDNG";
      case ACCEPTED -> "ACCP";
      case PROCESSING -> "PDNG";
      case COMPLETED -> "ACCP";
      case REJECTED -> "RJCT";
      case CANCELLED -> "RJCT";
      case FAILED -> "RJCT";
      case RETURNED -> "RJCT";
      case PARTIALLY_COMPLETED -> "ACCP";
    };
  }

  /** Map canonical payment status to transaction status */
  private String mapStatusToTransactionStatus(CanonicalPaymentModel.PaymentStatus status) {
    return switch (status) {
      case PENDING -> "PDNG";
      case ACCEPTED -> "ACCP";
      case PROCESSING -> "PDNG";
      case COMPLETED -> "ACSC";
      case REJECTED -> "RJCT";
      case CANCELLED -> "CANC";
      case FAILED -> "RJCT";
      case RETURNED -> "RJCT";
      case PARTIALLY_COMPLETED -> "ACSC";
    };
  }

  /** Map canonical payment status to reason code */
  private String mapStatusToReasonCode(CanonicalPaymentModel.PaymentStatus status) {
    return switch (status) {
      case PENDING -> "PDNG";
      case ACCEPTED -> "ACCP";
      case PROCESSING -> "PDNG";
      case COMPLETED -> "ACSC";
      case REJECTED -> "RJCT";
      case CANCELLED -> "CANC";
      case FAILED -> "RJCT";
      case RETURNED -> "RJCT";
      case PARTIALLY_COMPLETED -> "ACSC";
    };
  }
}
