package com.payments.swiftadapter.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * SWIFT Network Client
 *
 * <p>Client for SWIFT Alliance Gateway API integration for international payment messaging.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Send MT and MX format messages
 *   <li>Retrieve incoming messages
 *   <li>Query message status
 *   <li>mTLS authentication
 *   <li>Circuit breaker and retry patterns
 *   <li>Comprehensive error handling
 * </ul>
 *
 * <p>Supported Message Types:
 *
 * <ul>
 *   <li>MT103 - Single Customer Credit Transfer
 *   <li>MT202 - General Financial Institution Transfer
 *   <li>pacs.008 - Customer Credit Transfer (ISO 20022)
 *   <li>pacs.002 - Payment Status Report
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SwiftNetworkClient {

  private final RestTemplate swiftNetworkRestTemplate;
  private final SwiftNetworkClientConfig config;

  /**
   * Send SWIFT message
   *
   * @param messageType Message type (MT103, pacs.008, etc.)
   * @param messageContent Message content (MT or MX format)
   * @param sender Sender BIC code
   * @param receiver Receiver BIC code
   * @param reference Unique message reference
   * @return Message submission response
   */
  @CircuitBreaker(name = "swift-network", fallbackMethod = "sendMessageFallback")
  @Retry(name = "swift-network")
  @TimeLimiter(name = "swift-network")
  public SwiftMessageResponse sendMessage(
      String messageType, String messageContent, String sender, String receiver, String reference) {

    log.info(
        "Sending SWIFT message: {} from {} to {} (ref: {})",
        messageType,
        sender,
        receiver,
        reference);

    try {
      String url = config.getBaseUrl() + "/messages/send";

      Map<String, Object> request = new HashMap<>();
      request.put("messageType", messageType);
      request.put("messageContent", messageContent);
      request.put("senderBic", sender);
      request.put("receiverBic", receiver);
      request.put("messageReference", reference);
      request.put("submissionTime", Instant.now().toString());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-SWIFT-Reference", reference);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

      ResponseEntity<SwiftMessageResponse> response =
          swiftNetworkRestTemplate.exchange(
              url, HttpMethod.POST, entity, SwiftMessageResponse.class);

      SwiftMessageResponse result = response.getBody();

      if (result != null && result.isSuccess()) {
        log.info(
            "SWIFT message sent successfully: {} (SWIFT ID: {})",
            reference,
            result.getSwiftMessageId());
      } else {
        log.error(
            "SWIFT message submission failed: {} - {}",
            result != null ? result.getErrorCode() : "UNKNOWN",
            result != null ? result.getErrorMessage() : "No response");
      }

      return result;

    } catch (HttpClientErrorException e) {
      log.error("SWIFT network client error (4xx): {} - {}", e.getStatusCode(), e.getMessage());
      return SwiftMessageResponse.builder()
          .success(false)
          .errorCode("CLIENT_ERROR")
          .errorMessage("SWIFT client error: " + e.getMessage())
          .build();

    } catch (HttpServerErrorException e) {
      log.error("SWIFT network server error (5xx): {} - {}", e.getStatusCode(), e.getMessage());
      return SwiftMessageResponse.builder()
          .success(false)
          .errorCode("SERVER_ERROR")
          .errorMessage("SWIFT server error: " + e.getMessage())
          .build();

    } catch (Exception e) {
      log.error("SWIFT message send failed: {}", e.getMessage(), e);
      throw new SwiftNetworkException("Failed to send SWIFT message", e);
    }
  }

  /**
   * Retrieve incoming SWIFT messages
   *
   * @param receiverBic Receiver BIC code
   * @param messageType Optional message type filter
   * @param maxMessages Maximum number of messages to retrieve
   * @return List of incoming messages
   */
  @CircuitBreaker(name = "swift-network", fallbackMethod = "retrieveMessagesFallback")
  @Retry(name = "swift-network")
  public List<SwiftIncomingMessage> retrieveMessages(
      String receiverBic, String messageType, int maxMessages) {

    log.info(
        "Retrieving SWIFT messages for {} (type: {}, max: {})",
        receiverBic,
        messageType,
        maxMessages);

    try {
      String url =
          String.format(
              "%s/messages/retrieve?receiverBic=%s&messageType=%s&maxMessages=%d",
              config.getBaseUrl(),
              receiverBic,
              messageType != null ? messageType : "ALL",
              maxMessages);

      ResponseEntity<List<SwiftIncomingMessage>> response =
          swiftNetworkRestTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

      List<SwiftIncomingMessage> messages = response.getBody();
      log.info("Retrieved {} SWIFT messages", messages != null ? messages.size() : 0);

      return messages;

    } catch (Exception e) {
      log.error("Failed to retrieve SWIFT messages: {}", e.getMessage(), e);
      throw new SwiftNetworkException("Failed to retrieve SWIFT messages", e);
    }
  }

  /**
   * Get message status
   *
   * @param messageReference Message reference
   * @return Message status
   */
  @CircuitBreaker(name = "swift-network")
  public SwiftMessageStatus getMessageStatus(String messageReference) {

    log.debug("Getting SWIFT message status: {}", messageReference);

    try {
      String url = String.format("%s/messages/%s/status", config.getBaseUrl(), messageReference);

      ResponseEntity<SwiftMessageStatus> response =
          swiftNetworkRestTemplate.getForEntity(url, SwiftMessageStatus.class);

      return response.getBody();

    } catch (Exception e) {
      log.error("Failed to get SWIFT message status: {}", e.getMessage(), e);
      throw new SwiftNetworkException("Failed to get message status", e);
    }
  }

  /**
   * Test SWIFT network connectivity
   *
   * @return true if connection successful
   */
  public boolean testConnection() {
    log.info("Testing SWIFT network connectivity");

    try {
      String url = config.getBaseUrl() + "/health";

      ResponseEntity<Map<String, Object>> response =
          swiftNetworkRestTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

      boolean healthy =
          response.getStatusCode() == HttpStatus.OK
              && response.getBody() != null
              && "UP".equals(response.getBody().get("status"));

      log.info("SWIFT network connectivity test: {}", healthy ? "SUCCESS" : "FAILED");
      return healthy;

    } catch (Exception e) {
      log.error("SWIFT network connectivity test failed: {}", e.getMessage());
      return false;
    }
  }

  // Fallback methods for resilience

  private SwiftMessageResponse sendMessageFallback(
      String messageType,
      String messageContent,
      String sender,
      String receiver,
      String reference,
      Throwable t) {

    log.error("Circuit breaker: SWIFT message send failed for {}: {}", reference, t.getMessage());

    return SwiftMessageResponse.builder()
        .success(false)
        .messageReference(reference)
        .errorCode("CIRCUIT_BREAKER_OPEN")
        .errorMessage("SWIFT network unavailable: " + t.getMessage())
        .submissionTime(Instant.now())
        .build();
  }

  private List<SwiftIncomingMessage> retrieveMessagesFallback(
      String receiverBic, String messageType, int maxMessages, Throwable t) {

    log.error("Circuit breaker: SWIFT message retrieval failed: {}", t.getMessage());
    return List.of();
  }

  /** Custom exception for SWIFT network operations */
  public static class SwiftNetworkException extends RuntimeException {
    public SwiftNetworkException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
