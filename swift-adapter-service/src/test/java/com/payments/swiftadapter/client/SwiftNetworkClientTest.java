package com.payments.swiftadapter.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for SwiftNetworkClient
 *
 * <p>Tests SWIFT network operations including:
 *
 * <ul>
 *   <li>Message sending
 *   <li>Message retrieval
 *   <li>Status queries
 *   <li>Error handling
 *   <li>Resilience patterns
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class SwiftNetworkClientTest {

  @Mock private RestTemplate swiftNetworkRestTemplate;

  @Mock private SwiftNetworkClientConfig config;

  private SwiftNetworkClient swiftNetworkClient;

  private static final String BASE_URL = "https://swift-gateway.test.com/api/v1";
  private static final String SENDER_BIC = "DEUTDEFF";
  private static final String RECEIVER_BIC = "CHASUS33";
  private static final String MESSAGE_REFERENCE = "MSGREF123456";

  @BeforeEach
  void setUp() {
    when(config.getBaseUrl()).thenReturn(BASE_URL);
    swiftNetworkClient = new SwiftNetworkClient(swiftNetworkRestTemplate, config);
  }

  @Test
  void shouldSendMessageSuccessfully() {
    // Given
    String messageType = "MT103";
    String messageContent = "{1:F01DEUTDEFFAXXX...}";

    SwiftMessageResponse mockResponse =
        SwiftMessageResponse.builder()
            .success(true)
            .swiftMessageId("SWIFT123456")
            .messageReference(MESSAGE_REFERENCE)
            .messageType(messageType)
            .senderBic(SENDER_BIC)
            .receiverBic(RECEIVER_BIC)
            .status("SUBMITTED")
            .submissionTime(Instant.now())
            .build();

    when(swiftNetworkRestTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(SwiftMessageResponse.class)))
        .thenReturn(ResponseEntity.ok(mockResponse));

    // When
    SwiftMessageResponse response =
        swiftNetworkClient.sendMessage(
            messageType, messageContent, SENDER_BIC, RECEIVER_BIC, MESSAGE_REFERENCE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getSwiftMessageId()).isEqualTo("SWIFT123456");
    assertThat(response.getMessageReference()).isEqualTo(MESSAGE_REFERENCE);
    assertThat(response.getStatus()).isEqualTo("SUBMITTED");

    verify(swiftNetworkRestTemplate)
        .exchange(
            eq(BASE_URL + "/messages/send"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(SwiftMessageResponse.class));
  }

  @Test
  void shouldHandleMessageSendFailure() {
    // Given
    String messageType = "pacs.008";
    String messageContent = "<Document>...</Document>";

    SwiftMessageResponse mockResponse =
        SwiftMessageResponse.builder()
            .success(false)
            .messageReference(MESSAGE_REFERENCE)
            .errorCode("INVALID_BIC")
            .errorMessage("Receiver BIC not found")
            .build();

    when(swiftNetworkRestTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(SwiftMessageResponse.class)))
        .thenReturn(ResponseEntity.ok(mockResponse));

    // When
    SwiftMessageResponse response =
        swiftNetworkClient.sendMessage(
            messageType, messageContent, SENDER_BIC, RECEIVER_BIC, MESSAGE_REFERENCE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getErrorCode()).isEqualTo("INVALID_BIC");
  }

  @Test
  void shouldRetrieveMessages() {
    // Given
    String messageType = "MT103";
    int maxMessages = 10;

    List<SwiftIncomingMessage> mockMessages =
        List.of(
            SwiftIncomingMessage.builder()
                .swiftMessageId("SWIFT001")
                .messageType(messageType)
                .messageFormat("MT")
                .senderBic(SENDER_BIC)
                .receiverBic(RECEIVER_BIC)
                .messageContent("{1:F01...}")
                .messageReference("REF001")
                .receptionTime(Instant.now())
                .build(),
            SwiftIncomingMessage.builder()
                .swiftMessageId("SWIFT002")
                .messageType(messageType)
                .messageFormat("MT")
                .senderBic(SENDER_BIC)
                .receiverBic(RECEIVER_BIC)
                .messageContent("{1:F01...}")
                .messageReference("REF002")
                .receptionTime(Instant.now())
                .build());

    when(swiftNetworkRestTemplate.exchange(
            anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
        .thenReturn(ResponseEntity.ok(mockMessages));

    // When
    List<SwiftIncomingMessage> messages =
        swiftNetworkClient.retrieveMessages(RECEIVER_BIC, messageType, maxMessages);

    // Then
    assertThat(messages).isNotNull();
    assertThat(messages).hasSize(2);
    assertThat(messages.get(0).getSwiftMessageId()).isEqualTo("SWIFT001");
    assertThat(messages.get(1).getSwiftMessageId()).isEqualTo("SWIFT002");

    verify(swiftNetworkRestTemplate)
        .exchange(
            contains("/messages/retrieve"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class));
  }

  @Test
  void shouldGetMessageStatus() {
    // Given
    SwiftMessageStatus mockStatus =
        SwiftMessageStatus.builder()
            .swiftMessageId("SWIFT123456")
            .messageReference(MESSAGE_REFERENCE)
            .status("DELIVERED")
            .statusTimestamp(Instant.now())
            .deliveryTimestamp(Instant.now())
            .statusDescription("Message delivered successfully")
            .build();

    when(swiftNetworkRestTemplate.getForEntity(anyString(), eq(SwiftMessageStatus.class)))
        .thenReturn(ResponseEntity.ok(mockStatus));

    // When
    SwiftMessageStatus status = swiftNetworkClient.getMessageStatus(MESSAGE_REFERENCE);

    // Then
    assertThat(status).isNotNull();
    assertThat(status.getStatus()).isEqualTo("DELIVERED");
    assertThat(status.isDelivered()).isTrue();
    assertThat(status.isFailed()).isFalse();

    verify(swiftNetworkRestTemplate)
        .getForEntity(
            eq(BASE_URL + "/messages/" + MESSAGE_REFERENCE + "/status"),
            eq(SwiftMessageStatus.class));
  }

  @Test
  void shouldHandleFailedMessageStatus() {
    // Given
    SwiftMessageStatus mockStatus =
        SwiftMessageStatus.builder()
            .swiftMessageId("SWIFT123456")
            .messageReference(MESSAGE_REFERENCE)
            .status("FAILED")
            .statusTimestamp(Instant.now())
            .errorCode("TIMEOUT")
            .errorMessage("Message delivery timeout")
            .build();

    when(swiftNetworkRestTemplate.getForEntity(anyString(), eq(SwiftMessageStatus.class)))
        .thenReturn(ResponseEntity.ok(mockStatus));

    // When
    SwiftMessageStatus status = swiftNetworkClient.getMessageStatus(MESSAGE_REFERENCE);

    // Then
    assertThat(status).isNotNull();
    assertThat(status.getStatus()).isEqualTo("FAILED");
    assertThat(status.isDelivered()).isFalse();
    assertThat(status.isFailed()).isTrue();
    assertThat(status.getErrorCode()).isEqualTo("TIMEOUT");
  }

  @Test
  void shouldTestConnection() {
    // Given
    Map<String, Object> healthResponse = Map.of("status", "UP");

    when(swiftNetworkRestTemplate.exchange(
            anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
        .thenReturn(ResponseEntity.ok(healthResponse));

    // When
    boolean connected = swiftNetworkClient.testConnection();

    // Then
    assertThat(connected).isTrue();

    verify(swiftNetworkRestTemplate)
        .exchange(
            eq(BASE_URL + "/health"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class));
  }

  @Test
  void shouldHandleConnectionFailure() {
    // Given
    when(swiftNetworkRestTemplate.exchange(
            anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
        .thenThrow(new RuntimeException("Connection refused"));

    // When
    boolean connected = swiftNetworkClient.testConnection();

    // Then
    assertThat(connected).isFalse();
  }

  @Test
  void shouldHandleRetrieveMessagesError() {
    // Given
    when(swiftNetworkRestTemplate.exchange(
            anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
        .thenThrow(new RuntimeException("Network error"));

    // When/Then
    assertThatThrownBy(() -> swiftNetworkClient.retrieveMessages(RECEIVER_BIC, "MT103", 10))
        .isInstanceOf(SwiftNetworkClient.SwiftNetworkException.class)
        .hasMessageContaining("Failed to retrieve SWIFT messages");
  }

  @Test
  void shouldSetProperHeadersInSendMessage() {
    // Given
    String messageType = "MT103";
    String messageContent = "{1:F01...}";

    SwiftMessageResponse mockResponse = SwiftMessageResponse.builder().success(true).build();

    when(swiftNetworkRestTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(SwiftMessageResponse.class)))
        .thenReturn(ResponseEntity.ok(mockResponse));

    // When
    swiftNetworkClient.sendMessage(
        messageType, messageContent, SENDER_BIC, RECEIVER_BIC, MESSAGE_REFERENCE);

    // Then
    verify(swiftNetworkRestTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.POST),
            argThat(
                (HttpEntity<?> entity) -> {
                  HttpHeaders headers = entity.getHeaders();
                  return headers.getContentType() != null
                      && headers.getContentType().includes(MediaType.APPLICATION_JSON)
                      && MESSAGE_REFERENCE.equals(headers.getFirst("X-SWIFT-Reference"));
                }),
            eq(SwiftMessageResponse.class));
  }
}
