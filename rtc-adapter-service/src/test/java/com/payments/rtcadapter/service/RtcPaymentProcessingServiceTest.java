package com.payments.rtcadapter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.domain.RtcPaymentMessage;
import com.payments.rtcadapter.domain.RtcTransactionLog;
import com.payments.rtcadapter.repository.RtcAdapterRepository;
import com.payments.rtcadapter.repository.RtcPaymentMessageRepository;
import com.payments.rtcadapter.repository.RtcTransactionLogRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for RtcPaymentProcessingService */
@ExtendWith(MockitoExtension.class)
class RtcPaymentProcessingServiceTest {

  @Mock private RtcAdapterRepository rtcAdapterRepository;

  @Mock private RtcPaymentMessageRepository rtcPaymentMessageRepository;

  @Mock private RtcTransactionLogRepository rtcTransactionLogRepository;

  @InjectMocks private RtcPaymentProcessingService rtcPaymentProcessingService;

  private ClearingAdapterId adapterId;
  private ClearingMessageId messageId;
  private RtcAdapter rtcAdapter;
  private RtcPaymentMessage rtcPaymentMessage;

  @BeforeEach
  void setUp() {
    adapterId = ClearingAdapterId.generate();
    messageId = ClearingMessageId.generate();

    rtcAdapter =
        RtcAdapter.builder()
            .id(adapterId)
            .adapterName("Test RTC Adapter")
            .status(AdapterOperationalStatus.ACTIVE)
            .build();

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
  }

  @Test
  void processRtcPayment_ShouldCreatePaymentMessage_WhenValidInput() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));
    when(rtcPaymentMessageRepository.save(any(RtcPaymentMessage.class)))
        .thenReturn(rtcPaymentMessage);
    when(rtcAdapterRepository.save(any(RtcAdapter.class))).thenReturn(rtcAdapter);

    // When
    RtcPaymentMessage result =
        rtcPaymentProcessingService.processRtcPayment(
            adapterId,
            "TXN-123",
            "pacs.008",
            "OUTBOUND",
            "MSG-123",
            "INST-123",
            "E2E-123",
            "CREDIT",
            new BigDecimal("1500.00"),
            "ZAR",
            "John Doe",
            "1234567890",
            "BANK001",
            "Jane Smith",
            "0987654321",
            "BANK002",
            "Payment",
            "REF-123");

    // Then
    assertNotNull(result);
    assertEquals("TXN-123", result.getTransactionId());
    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcPaymentMessageRepository).save(any(RtcPaymentMessage.class));
    verify(rtcAdapterRepository).save(any(RtcAdapter.class));
  }

  @Test
  void processRtcPayment_ShouldThrowException_WhenAdapterNotExists() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () ->
            rtcPaymentProcessingService.processRtcPayment(
                adapterId,
                "TXN-123",
                "pacs.008",
                "OUTBOUND",
                "MSG-123",
                "INST-123",
                "E2E-123",
                "CREDIT",
                new BigDecimal("1500.00"),
                "ZAR",
                "John Doe",
                "1234567890",
                "BANK001",
                "Jane Smith",
                "0987654321",
                "BANK002",
                "Payment",
                "REF-123"));

    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcPaymentMessageRepository, never()).save(any(RtcPaymentMessage.class));
  }

  @Test
  void processRtcPayment_ShouldThrowException_WhenAdapterIsInactive() {
    // Given
    rtcAdapter.setStatus(AdapterOperationalStatus.INACTIVE);
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () ->
            rtcPaymentProcessingService.processRtcPayment(
                adapterId,
                "TXN-123",
                "pacs.008",
                "OUTBOUND",
                "MSG-123",
                "INST-123",
                "E2E-123",
                "CREDIT",
                new BigDecimal("1500.00"),
                "ZAR",
                "John Doe",
                "1234567890",
                "BANK001",
                "Jane Smith",
                "0987654321",
                "BANK002",
                "Payment",
                "REF-123"));

    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcPaymentMessageRepository, never()).save(any(RtcPaymentMessage.class));
  }

  @Test
  void processRtcPayment_ShouldThrowException_WhenAmountExceedsLimit() {
    // Given
    when(rtcAdapterRepository.findById(adapterId)).thenReturn(Optional.of(rtcAdapter));

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () ->
            rtcPaymentProcessingService.processRtcPayment(
                adapterId,
                "TXN-123",
                "pacs.008",
                "OUTBOUND",
                "MSG-123",
                "INST-123",
                "E2E-123",
                "CREDIT",
                new BigDecimal("6000.00"),
                "ZAR",
                "John Doe",
                "1234567890",
                "BANK001",
                "Jane Smith",
                "0987654321",
                "BANK002",
                "Payment",
                "REF-123"));

    verify(rtcAdapterRepository).findById(adapterId);
    verify(rtcPaymentMessageRepository, never()).save(any(RtcPaymentMessage.class));
  }

  @Test
  void submitPayment_ShouldMarkAsSubmitted_WhenPaymentExists() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId))
        .thenReturn(Optional.of(rtcPaymentMessage));
    when(rtcPaymentMessageRepository.save(any(RtcPaymentMessage.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    RtcPaymentMessage result = rtcPaymentProcessingService.submitPayment(messageId);

    // Then
    assertNotNull(result);
    assertEquals("SUBMITTED", result.getStatus());
    verify(rtcPaymentMessageRepository).findById(messageId);
    verify(rtcPaymentMessageRepository).save(any(RtcPaymentMessage.class));
  }

  @Test
  void submitPayment_ShouldThrowException_WhenPaymentNotExists() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> rtcPaymentProcessingService.submitPayment(messageId));

    verify(rtcPaymentMessageRepository).findById(messageId);
    verify(rtcPaymentMessageRepository, never()).save(any(RtcPaymentMessage.class));
  }

  @Test
  void processPaymentResponse_ShouldMarkAsCompleted_WhenSuccess() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId))
        .thenReturn(Optional.of(rtcPaymentMessage));
    when(rtcPaymentMessageRepository.save(any(RtcPaymentMessage.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    RtcPaymentMessage result =
        rtcPaymentProcessingService.processPaymentResponse(messageId, "00", "Success", true);

    // Then
    assertNotNull(result);
    assertEquals("COMPLETED", result.getStatus());
    assertEquals("00", result.getResponseCode());
    assertEquals("Success", result.getResponseMessage());
    verify(rtcPaymentMessageRepository).findById(messageId);
    verify(rtcPaymentMessageRepository).save(any(RtcPaymentMessage.class));
  }

  @Test
  void processPaymentResponse_ShouldMarkAsFailed_WhenFailure() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId))
        .thenReturn(Optional.of(rtcPaymentMessage));
    when(rtcPaymentMessageRepository.save(any(RtcPaymentMessage.class)))
        .thenReturn(rtcPaymentMessage);

    // When
    RtcPaymentMessage result =
        rtcPaymentProcessingService.processPaymentResponse(messageId, "99", "Failed", false);

    // Then
    assertNotNull(result);
    assertEquals("FAILED", result.getStatus());
    assertEquals("99", result.getResponseCode());
    assertEquals("Failed", result.getResponseMessage());
    verify(rtcPaymentMessageRepository).findById(messageId);
    verify(rtcPaymentMessageRepository).save(any(RtcPaymentMessage.class));
  }

  @Test
  void processPaymentResponse_ShouldThrowException_WhenPaymentNotExists() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> rtcPaymentProcessingService.processPaymentResponse(messageId, "00", "Success", true));

    verify(rtcPaymentMessageRepository).findById(messageId);
    verify(rtcPaymentMessageRepository, never()).save(any(RtcPaymentMessage.class));
  }

  @Test
  void getPaymentMessage_ShouldReturnPayment_WhenExists() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId))
        .thenReturn(Optional.of(rtcPaymentMessage));

    // When
    Optional<RtcPaymentMessage> result = rtcPaymentProcessingService.getPaymentMessage(messageId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(messageId, result.get().getId());
    verify(rtcPaymentMessageRepository).findById(messageId);
  }

  @Test
  void getPaymentMessage_ShouldReturnEmpty_WhenNotExists() {
    // Given
    when(rtcPaymentMessageRepository.findById(messageId)).thenReturn(Optional.empty());

    // When
    Optional<RtcPaymentMessage> result = rtcPaymentProcessingService.getPaymentMessage(messageId);

    // Then
    assertFalse(result.isPresent());
    verify(rtcPaymentMessageRepository).findById(messageId);
  }

  @Test
  void getPaymentMessagesByAdapter_ShouldReturnMessages_WhenAdapterExists() {
    // Given
    List<RtcPaymentMessage> messages = List.of(rtcPaymentMessage);
    when(rtcPaymentMessageRepository.findByAdapterId(adapterId.toString())).thenReturn(messages);

    // When
    List<RtcPaymentMessage> result =
        rtcPaymentProcessingService.getPaymentMessagesByAdapter(adapterId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(rtcPaymentMessageRepository).findByAdapterId(adapterId.toString());
  }

  @Test
  void getPaymentMessageByTransactionId_ShouldReturnMessage_WhenExists() {
    // Given
    when(rtcPaymentMessageRepository.findByTransactionId("TXN-123")).thenReturn(rtcPaymentMessage);

    // When
    RtcPaymentMessage result =
        rtcPaymentProcessingService.getPaymentMessageByTransactionId("TXN-123");

    // Then
    assertNotNull(result);
    assertEquals("TXN-123", result.getTransactionId());
    verify(rtcPaymentMessageRepository).findByTransactionId("TXN-123");
  }

  @Test
  void getPaymentMessagesByStatus_ShouldReturnMessages_WhenStatusExists() {
    // Given
    List<RtcPaymentMessage> messages = List.of(rtcPaymentMessage);
    when(rtcPaymentMessageRepository.findByStatus("PENDING")).thenReturn(messages);

    // When
    List<RtcPaymentMessage> result =
        rtcPaymentProcessingService.getPaymentMessagesByStatus("PENDING");

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(rtcPaymentMessageRepository).findByStatus("PENDING");
  }

  @Test
  void getPaymentMessagesByAdapterAndStatus_ShouldReturnMessages_WhenAdapterAndStatusExists() {
    // Given
    List<RtcPaymentMessage> messages = List.of(rtcPaymentMessage);
    when(rtcPaymentMessageRepository.findByAdapterIdAndStatus(adapterId.toString(), "PENDING"))
        .thenReturn(messages);

    // When
    List<RtcPaymentMessage> result =
        rtcPaymentProcessingService.getPaymentMessagesByAdapterAndStatus(adapterId, "PENDING");

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(rtcPaymentMessageRepository).findByAdapterIdAndStatus(adapterId.toString(), "PENDING");
  }

  @Test
  void getTransactionLogsByAdapter_ShouldReturnLogs_WhenAdapterExists() {
    // Given
    List<RtcTransactionLog> logs = List.of();
    when(rtcTransactionLogRepository.findByAdapterId(adapterId.toString())).thenReturn(logs);

    // When
    List<RtcTransactionLog> result =
        rtcPaymentProcessingService.getTransactionLogsByAdapter(adapterId);

    // Then
    assertNotNull(result);
    verify(rtcTransactionLogRepository).findByAdapterId(adapterId.toString());
  }

  @Test
  void getTransactionLogsByTransactionId_ShouldReturnLogs_WhenTransactionExists() {
    // Given
    List<RtcTransactionLog> logs = List.of();
    when(rtcTransactionLogRepository.findByTransactionId("TXN-123")).thenReturn(logs);

    // When
    List<RtcTransactionLog> result =
        rtcPaymentProcessingService.getTransactionLogsByTransactionId("TXN-123");

    // Then
    assertNotNull(result);
    verify(rtcTransactionLogRepository).findByTransactionId("TXN-123");
  }

  @Test
  void getPaymentMessageCount_ShouldReturnCount_WhenCalled() {
    // Given
    when(rtcPaymentMessageRepository.count()).thenReturn(5L);

    // When
    long result = rtcPaymentProcessingService.getPaymentMessageCount();

    // Then
    assertEquals(5L, result);
    verify(rtcPaymentMessageRepository).count();
  }

  @Test
  void getPaymentMessageCountByStatus_ShouldReturnCount_WhenStatusExists() {
    // Given
    when(rtcPaymentMessageRepository.countByStatus("PENDING")).thenReturn(3L);

    // When
    long result = rtcPaymentProcessingService.getPaymentMessageCountByStatus("PENDING");

    // Then
    assertEquals(3L, result);
    verify(rtcPaymentMessageRepository).countByStatus("PENDING");
  }

  @Test
  void getPaymentMessageCountByAdapter_ShouldReturnCount_WhenAdapterExists() {
    // Given
    when(rtcPaymentMessageRepository.countByAdapterId(adapterId.toString())).thenReturn(2L);

    // When
    long result = rtcPaymentProcessingService.getPaymentMessageCountByAdapter(adapterId);

    // Then
    assertEquals(2L, result);
    verify(rtcPaymentMessageRepository).countByAdapterId(adapterId.toString());
  }
}
