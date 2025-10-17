package com.payments.rtcadapter.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcPaymentMessage;
import com.payments.rtcadapter.domain.RtcTransactionLog;
import com.payments.rtcadapter.service.RtcPaymentProcessingService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for RtcPaymentProcessingController */
@ExtendWith(MockitoExtension.class)
class RtcPaymentProcessingControllerTest {

  @Mock private RtcPaymentProcessingService rtcPaymentProcessingService;

  @InjectMocks private RtcPaymentProcessingController rtcPaymentProcessingController;

  private ClearingAdapterId adapterId;
  private ClearingMessageId messageId;
  private RtcPaymentMessage rtcPaymentMessage;
  private RtcPaymentProcessingController.ProcessRtcPaymentRequest processRequest;
  private RtcPaymentProcessingController.ProcessPaymentResponseRequest responseRequest;

  @BeforeEach
  void setUp() {
    adapterId = ClearingAdapterId.generate();
    messageId = ClearingMessageId.generate();

    rtcPaymentMessage =
        RtcPaymentMessage.builder()
            .id(messageId)
            .rtcAdapterId(adapterId.toString())
            .transactionId("TXN-123")
            .messageType("pacs.008")
            .direction("OUTBOUND")
            .amount(new BigDecimal("1500.00"))
            .currency("ZAR")
            .status("PENDING")
            .build();

    processRequest = new RtcPaymentProcessingController.ProcessRtcPaymentRequest();
    processRequest.setAdapterId(adapterId.toString());
    processRequest.setTransactionId("TXN-123");
    processRequest.setMessageType("pacs.008");
    processRequest.setDirection("OUTBOUND");
    processRequest.setMessageId("MSG-123");
    processRequest.setInstructionId("INST-123");
    processRequest.setEndToEndId("E2E-123");
    processRequest.setTransactionType("CREDIT");
    processRequest.setAmount(new BigDecimal("1500.00"));
    processRequest.setCurrency("ZAR");
    processRequest.setDebtorName("John Doe");
    processRequest.setDebtorAccount("1234567890");
    processRequest.setDebtorBankCode("BANK001");
    processRequest.setCreditorName("Jane Smith");
    processRequest.setCreditorAccount("0987654321");
    processRequest.setCreditorBankCode("BANK002");
    processRequest.setPaymentPurpose("Payment for services");
    processRequest.setReference("REF-123");

    responseRequest = new RtcPaymentProcessingController.ProcessPaymentResponseRequest();
    responseRequest.setResponseCode("00");
    responseRequest.setResponseMessage("Success");
    responseRequest.setSuccess(true);
  }

  @Test
  void processRtcPayment_ShouldReturnPaymentMessage_WhenValidRequest() {
    // Given
    when(rtcPaymentProcessingService.processRtcPayment(
            any(ClearingAdapterId.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(BigDecimal.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.processRtcPayment(processRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("TXN-123", response.getBody().getTransactionId());
    verify(rtcPaymentProcessingService)
        .processRtcPayment(
            any(ClearingAdapterId.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(BigDecimal.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class));
  }

  @Test
  void submitPayment_ShouldReturnPaymentMessage_WhenValidMessageId() {
    // Given
    when(rtcPaymentProcessingService.submitPayment(any(ClearingMessageId.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.submitPayment(messageId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(messageId, response.getBody().getId());
    verify(rtcPaymentProcessingService).submitPayment(any(ClearingMessageId.class));
  }

  @Test
  void processPaymentResponse_ShouldReturnPaymentMessage_WhenValidRequest() {
    // Given
    when(rtcPaymentProcessingService.processPaymentResponse(
            any(ClearingMessageId.class), any(String.class), any(String.class), any(Boolean.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.processPaymentResponse(
            messageId.toString(), responseRequest);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(rtcPaymentProcessingService)
        .processPaymentResponse(
            any(ClearingMessageId.class), any(String.class), any(String.class), any(Boolean.class));
  }

  @Test
  void getPaymentMessage_ShouldReturnPaymentMessage_WhenMessageExists() {
    // Given
    when(rtcPaymentProcessingService.getPaymentMessage(any(ClearingMessageId.class)))
        .thenReturn(Optional.of(rtcPaymentMessage));

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.getPaymentMessage(messageId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(messageId, response.getBody().getId());
    verify(rtcPaymentProcessingService).getPaymentMessage(any(ClearingMessageId.class));
  }

  @Test
  void getPaymentMessage_ShouldReturnNotFound_WhenMessageNotExists() {
    // Given
    when(rtcPaymentProcessingService.getPaymentMessage(any(ClearingMessageId.class)))
        .thenReturn(Optional.empty());

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.getPaymentMessage(messageId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rtcPaymentProcessingService).getPaymentMessage(any(ClearingMessageId.class));
  }

  @Test
  void getPaymentMessagesByAdapter_ShouldReturnMessages_WhenAdapterExists() {
    // Given
    List<RtcPaymentMessage> messages = List.of(rtcPaymentMessage);
    when(rtcPaymentProcessingService.getPaymentMessagesByAdapter(any(ClearingAdapterId.class)))
        .thenReturn(messages);

    // When
    ResponseEntity<List<RtcPaymentMessage>> response =
        rtcPaymentProcessingController.getPaymentMessagesByAdapter(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(rtcPaymentProcessingService).getPaymentMessagesByAdapter(any(ClearingAdapterId.class));
  }

  @Test
  void getPaymentMessageByTransactionId_ShouldReturnPaymentMessage_WhenTransactionExists() {
    // Given
    when(rtcPaymentProcessingService.getPaymentMessageByTransactionId(any(String.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.getPaymentMessageByTransactionId("TXN-123");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("TXN-123", response.getBody().getTransactionId());
    verify(rtcPaymentProcessingService).getPaymentMessageByTransactionId("TXN-123");
  }

  @Test
  void getPaymentMessageByTransactionId_ShouldReturnNotFound_WhenTransactionNotExists() {
    // Given
    when(rtcPaymentProcessingService.getPaymentMessageByTransactionId(any(String.class)))
        .thenReturn(null);

    // When
    ResponseEntity<RtcPaymentMessage> response =
        rtcPaymentProcessingController.getPaymentMessageByTransactionId("TXN-123");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rtcPaymentProcessingService).getPaymentMessageByTransactionId("TXN-123");
  }

  @Test
  void getPaymentMessagesByStatus_ShouldReturnMessages_WhenStatusExists() {
    // Given
    List<RtcPaymentMessage> messages = List.of(rtcPaymentMessage);
    when(rtcPaymentProcessingService.getPaymentMessagesByStatus(any(String.class)))
        .thenReturn(messages);

    // When
    ResponseEntity<List<RtcPaymentMessage>> response =
        rtcPaymentProcessingController.getPaymentMessagesByStatus("PENDING");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(rtcPaymentProcessingService).getPaymentMessagesByStatus("PENDING");
  }

  @Test
  void getPaymentMessagesByAdapterAndStatus_ShouldReturnMessages_WhenAdapterAndStatusExist() {
    // Given
    List<RtcPaymentMessage> messages = List.of(rtcPaymentMessage);
    when(rtcPaymentProcessingService.getPaymentMessagesByAdapterAndStatus(
            any(ClearingAdapterId.class), any(String.class)))
        .thenReturn(messages);

    // When
    ResponseEntity<List<RtcPaymentMessage>> response =
        rtcPaymentProcessingController.getPaymentMessagesByAdapterAndStatus(
            adapterId.toString(), "PENDING");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(rtcPaymentProcessingService)
        .getPaymentMessagesByAdapterAndStatus(any(ClearingAdapterId.class), any(String.class));
  }

  @Test
  void getTransactionLogsByAdapter_ShouldReturnLogs_WhenAdapterExists() {
    // Given
    List<RtcTransactionLog> logs = List.of();
    when(rtcPaymentProcessingService.getTransactionLogsByAdapter(any(ClearingAdapterId.class)))
        .thenReturn(logs);

    // When
    ResponseEntity<List<RtcTransactionLog>> response =
        rtcPaymentProcessingController.getTransactionLogsByAdapter(adapterId.toString());

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(rtcPaymentProcessingService).getTransactionLogsByAdapter(any(ClearingAdapterId.class));
  }

  @Test
  void getTransactionLogsByTransactionId_ShouldReturnLogs_WhenTransactionExists() {
    // Given
    List<RtcTransactionLog> logs = List.of();
    when(rtcPaymentProcessingService.getTransactionLogsByTransactionId(any(String.class)))
        .thenReturn(logs);

    // When
    ResponseEntity<List<RtcTransactionLog>> response =
        rtcPaymentProcessingController.getTransactionLogsByTransactionId("TXN-123");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(rtcPaymentProcessingService).getTransactionLogsByTransactionId("TXN-123");
  }
}
