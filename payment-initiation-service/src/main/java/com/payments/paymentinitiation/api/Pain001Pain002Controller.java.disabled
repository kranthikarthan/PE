package com.payments.paymentinitiation.api;

import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.service.PaymentProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for ISO 20022 pain.001/pain.002 message processing
 *
 * <p>Provides endpoints for: - Processing pain.001 payment initiation messages - Retrieving payment
 * status as pain.002 messages - Supporting both XML and JSON formats
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/iso20022")
@RequiredArgsConstructor
@Tag(
    name = "ISO 20022 Payment Processing",
    description = "ISO 20022 pain.001/pain.002 message processing")
public class Pain001Pain002Controller {

  private final PaymentProcessingService paymentProcessingService;

  @PostMapping(
      value = "/pain001/process",
      consumes = {MediaType.APPLICATION_XML_VALUE, "application/xml"},
      produces = {MediaType.APPLICATION_XML_VALUE, "application/xml"})
  @Operation(
      summary = "Process pain.001 Payment Initiation Message",
      description =
          "Process ISO 20022 pain.001 Customer Credit Transfer Initiation message and return pain.002 status report")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "pain.001 processed successfully, pain.002 status report returned",
            content =
                @Content(
                    mediaType = "application/xml",
                    schema = @Schema(type = "string", description = "pain.002 XML message"))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pain.001 message format or validation failed",
            content =
                @Content(
                    mediaType = "application/xml",
                    schema = @Schema(type = "string", description = "Error pain.002 XML message"))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/xml",
                    schema = @Schema(type = "string", description = "Error pain.002 XML message")))
      })
  public ResponseEntity<String> processPain001Xml(
      @Parameter(description = "pain.001 XML message", required = true) @RequestBody
          String pain001Xml,
      @Parameter(description = "Correlation ID for tracing", required = true)
          @RequestHeader("X-Correlation-ID")
          String correlationId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId,
      @Parameter(description = "Business Unit ID", required = true)
          @RequestHeader("X-Business-Unit-ID")
          String businessUnitId) {

    log.info(
        "Processing pain.001 XML message for tenant: {}, business unit: {}, correlation: {}",
        tenantId,
        businessUnitId,
        correlationId);

    try {
      TenantContext tenantContext =
          TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();

      String pain002Response =
          paymentProcessingService.processPain001Message(pain001Xml, tenantContext, correlationId);

      log.info("Successfully processed pain.001 XML message, generated pain.002 response");
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(pain002Response);

    } catch (Exception e) {
      log.error("Failed to process pain.001 XML message", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_XML)
          .body(generateErrorPain002Xml(e.getMessage(), correlationId));
    }
  }

  @PostMapping(
      value = "/pain001/process",
      consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json"},
      produces = {MediaType.APPLICATION_JSON_VALUE, "application/json"})
  @Operation(
      summary = "Process pain.001 Payment Initiation Message (JSON)",
      description =
          "Process ISO 20022 pain.001 Customer Credit Transfer Initiation message in JSON format and return pain.002 status report")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "pain.001 processed successfully, pain.002 status report returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string", description = "pain.002 JSON message"))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pain.001 message format or validation failed",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(type = "string", description = "Error pain.002 JSON message"))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string", description = "Error pain.002 JSON message")))
      })
  public ResponseEntity<String> processPain001Json(
      @Parameter(description = "pain.001 JSON message", required = true) @RequestBody
          String pain001Json,
      @Parameter(description = "Correlation ID for tracing", required = true)
          @RequestHeader("X-Correlation-ID")
          String correlationId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId,
      @Parameter(description = "Business Unit ID", required = true)
          @RequestHeader("X-Business-Unit-ID")
          String businessUnitId) {

    log.info(
        "Processing pain.001 JSON message for tenant: {}, business unit: {}, correlation: {}",
        tenantId,
        businessUnitId,
        correlationId);

    try {
      TenantContext tenantContext =
          TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();

      String pain002Response =
          paymentProcessingService.processPain001JsonMessage(
              pain001Json, tenantContext, correlationId);

      log.info("Successfully processed pain.001 JSON message, generated pain.002 response");
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(pain002Response);

    } catch (Exception e) {
      log.error("Failed to process pain.001 JSON message", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_JSON)
          .body(generateErrorPain002Json(e.getMessage(), correlationId));
    }
  }

  @GetMapping(
      value = "/pain002/{paymentId}/status",
      produces = {MediaType.APPLICATION_XML_VALUE, "application/xml"})
  @Operation(
      summary = "Get Payment Status as pain.002 Message",
      description = "Retrieve payment status as ISO 20022 pain.002 Payment Status Report message")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment status retrieved successfully as pain.002 message",
            content =
                @Content(
                    mediaType = "application/xml",
                    schema = @Schema(type = "string", description = "pain.002 XML message"))),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content =
                @Content(
                    mediaType = "application/xml",
                    schema = @Schema(type = "string", description = "Error pain.002 XML message"))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/xml",
                    schema = @Schema(type = "string", description = "Error pain.002 XML message")))
      })
  public ResponseEntity<String> getPaymentStatusAsPain002Xml(
      @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId")
          String paymentId,
      @Parameter(description = "Correlation ID for tracing", required = true)
          @RequestHeader("X-Correlation-ID")
          String correlationId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId,
      @Parameter(description = "Business Unit ID", required = true)
          @RequestHeader("X-Business-Unit-ID")
          String businessUnitId) {

    log.info(
        "Retrieving payment status as pain.002 XML for payment: {}, tenant: {}, correlation: {}",
        paymentId,
        tenantId,
        correlationId);

    try {
      TenantContext tenantContext =
          TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();

      String pain002Response =
          paymentProcessingService.getPaymentStatusAsPain002(
              paymentId, tenantContext, correlationId);

      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(pain002Response);

    } catch (Exception e) {
      log.error("Failed to retrieve payment status as pain.002", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_XML)
          .body(generateErrorPain002Xml(e.getMessage(), correlationId));
    }
  }

  @GetMapping(
      value = "/pain002/{paymentId}/status",
      produces = {MediaType.APPLICATION_JSON_VALUE, "application/json"})
  @Operation(
      summary = "Get Payment Status as pain.002 Message (JSON)",
      description =
          "Retrieve payment status as ISO 20022 pain.002 Payment Status Report message in JSON format")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment status retrieved successfully as pain.002 message",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string", description = "pain.002 JSON message"))),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(type = "string", description = "Error pain.002 JSON message"))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string", description = "Error pain.002 JSON message")))
      })
  public ResponseEntity<String> getPaymentStatusAsPain002Json(
      @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId")
          String paymentId,
      @Parameter(description = "Correlation ID for tracing", required = true)
          @RequestHeader("X-Correlation-ID")
          String correlationId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId,
      @Parameter(description = "Business Unit ID", required = true)
          @RequestHeader("X-Business-Unit-ID")
          String businessUnitId) {

    log.info(
        "Retrieving payment status as pain.002 JSON for payment: {}, tenant: {}, correlation: {}",
        paymentId,
        tenantId,
        correlationId);

    try {
      TenantContext tenantContext =
          TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();

      String pain002Response =
          paymentProcessingService.getPaymentStatusAsPain002(
              paymentId, tenantContext, correlationId);

      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(pain002Response);

    } catch (Exception e) {
      log.error("Failed to retrieve payment status as pain.002", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_JSON)
          .body(generateErrorPain002Json(e.getMessage(), correlationId));
    }
  }

  @PostMapping(
      value = "/pain001/validate",
      consumes = {MediaType.APPLICATION_XML_VALUE, "application/xml"},
      produces = {MediaType.APPLICATION_JSON_VALUE, "application/json"})
  @Operation(
      summary = "Validate pain.001 Message",
      description = "Validate ISO 20022 pain.001 message format and content without processing")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "pain.001 message validation successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "pain.001 message validation failed",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationResponse.class)))
      })
  public ResponseEntity<ValidationResponse> validatePain001Xml(
      @Parameter(description = "pain.001 XML message to validate", required = true) @RequestBody
          String pain001Xml,
      @Parameter(description = "Correlation ID for tracing", required = true)
          @RequestHeader("X-Correlation-ID")
          String correlationId,
      @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
          String tenantId,
      @Parameter(description = "Business Unit ID", required = true)
          @RequestHeader("X-Business-Unit-ID")
          String businessUnitId) {

    log.info(
        "Validating pain.001 XML message for tenant: {}, business unit: {}, correlation: {}",
        tenantId,
        businessUnitId,
        correlationId);

    try {
      TenantContext tenantContext =
          TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();

      // Parse the message to validate it
      paymentProcessingService.processPain001Message(pain001Xml, tenantContext, correlationId);

      ValidationResponse response =
          ValidationResponse.builder()
              .valid(true)
              .message("pain.001 message is valid")
              .correlationId(correlationId)
              .build();

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.warn("pain.001 message validation failed: {}", e.getMessage());

      ValidationResponse response =
          ValidationResponse.builder()
              .valid(false)
              .message("pain.001 message validation failed: " + e.getMessage())
              .correlationId(correlationId)
              .build();

      return ResponseEntity.badRequest().body(response);
    }
  }

  /** Generate error pain.002 XML response */
  private String generateErrorPain002Xml(String errorMessage, String correlationId) {
    return String.format(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.14">
                <PmtStsRpt>
                    <GrpHdr>
                        <MsgId>ERROR-%s</MsgId>
                        <CreDtTm>%s</CreDtTm>
                    </GrpHdr>
                    <TxInfAndSts>
                        <StsId>ERROR-STATUS</StsId>
                        <TxSts>RJCT</TxSts>
                        <StsRsnInf>
                            <Rsn>
                                <Prtry>%s</Prtry>
                            </Rsn>
                        </StsRsnInf>
                    </TxInfAndSts>
                </PmtStsRpt>
            </Document>
            """,
        correlationId, java.time.Instant.now(), errorMessage);
  }

  /** Generate error pain.002 JSON response */
  private String generateErrorPain002Json(String errorMessage, String correlationId) {
    return String.format(
        """
            {
                "Document": {
                    "PmtStsRpt": {
                        "GrpHdr": {
                            "MsgId": "ERROR-%s",
                            "CreDtTm": "%s"
                        },
                        "TxInfAndSts": {
                            "StsId": "ERROR-STATUS",
                            "TxSts": "RJCT",
                            "StsRsnInf": {
                                "Rsn": {
                                    "Prtry": "%s"
                                }
                            }
                        }
                    }
                }
            }
            """,
        correlationId, java.time.Instant.now(), errorMessage);
  }

  /** Validation response DTO */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  @Schema(description = "pain.001 validation response")
  public static class ValidationResponse {
    @Schema(description = "Whether the message is valid")
    private boolean valid;

    @Schema(description = "Validation message")
    private String message;

    @Schema(description = "Correlation ID")
    private String correlationId;
  }
}
