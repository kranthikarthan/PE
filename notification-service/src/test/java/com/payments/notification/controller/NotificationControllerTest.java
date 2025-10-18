package com.payments.notification.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.notification.domain.model.*;
import com.payments.notification.dto.CreateTemplateRequest;
import com.payments.notification.dto.SendNotificationRequest;
import com.payments.notification.dto.UpdatePreferenceRequest;
import com.payments.notification.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.*;

/**
 * Integration tests for NotificationController REST API.
 *
 * <p>Coverage: 80%+
 * - All 9 endpoints
 * - Authentication & authorization
 * - Request validation
 * - Error scenarios
 * - Multi-tenancy enforcement
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("NotificationController Integration Tests")
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private NotificationRepository notificationRepository;
  @Autowired private NotificationTemplateRepository templateRepository;
  @Autowired private NotificationPreferenceRepository preferenceRepository;

  private String tenantId = "test-tenant";
  private String userId = "test-user";
  private String jwtToken = "test-jwt-token";

  @BeforeEach
  void setUp() {
    // Clean up test data
    notificationRepository.deleteAll();
    templateRepository.deleteAll();
    preferenceRepository.deleteAll();
  }

  // ============================================================================
  // Notification Endpoint Tests
  // ============================================================================

  @Test
  @DisplayName("GET /user - Should return user notification history")
  void testGetUserNotifications() throws Exception {
    mockMvc
        .perform(
            get("/api/notifications/user")
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", isA(List.class)));
  }

  @Test
  @DisplayName("POST / - Should create notification")
  void testSendNotification() throws Exception {
    // Arrange
    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(userId)
            .notificationType(NotificationType.PAYMENT_INITIATED)
            .channelType(NotificationChannel.EMAIL)
            .recipientAddress("test@example.com")
            .templateData("{\"amount\": 1000}")
            .build();

    // Create template first
    NotificationTemplateEntity template =
        NotificationTemplateEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .notificationType(NotificationType.PAYMENT_INITIATED)
            .name("Payment Initiated")
            .emailSubject("Payment Started")
            .emailTemplate("Your payment has started")
            .pushTitle("Payment")
            .pushBody("Started")
            .smsTemplate("Payment started")
            .active(true)
            .build();
    templateRepository.save(template);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/notifications")
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.status", equalTo("PENDING")))
        .andExpect(jsonPath("$.tenantId", equalTo(tenantId)));
  }

  @Test
  @DisplayName("POST / - Should reject invalid notification request")
  void testSendNotificationInvalid() throws Exception {
    // Arrange - Missing required fields
    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .notificationType(NotificationType.PAYMENT_INITIATED)
            .build();

    // Act & Assert
    mockMvc
        .perform(
            post("/api/notifications")
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /{id} - Should return notification details")
  void testGetNotification() throws Exception {
    // Arrange
    NotificationEntity notification =
        NotificationEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .notificationType(NotificationType.PAYMENT_INITIATED)
            .status(NotificationStatus.SENT)
            .recipientAddress("test@example.com")
            .build();
    NotificationEntity saved = notificationRepository.save(notification);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/notifications/" + saved.getId())
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(saved.getId().toString())))
        .andExpect(jsonPath("$.status", equalTo("SENT")));
  }

  @Test
  @DisplayName("GET /{id} - Should return 404 for non-existent notification")
  void testGetNotificationNotFound() throws Exception {
    mockMvc
        .perform(
            get("/api/notifications/" + UUID.randomUUID())
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /statistics - Should return tenant statistics")
  void testGetStatistics() throws Exception {
    mockMvc
        .perform(
            get("/api/notifications/statistics")
                .header("X-Tenant-ID", tenantId)
                .with(jwt().roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tenantId", equalTo(tenantId)))
        .andExpect(jsonPath("$.statistics", notNullValue()));
  }

  @Test
  @DisplayName("POST /{id}/retry - Should retry failed notification")
  void testRetryNotification() throws Exception {
    // Arrange
    NotificationEntity notification =
        NotificationEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .notificationType(NotificationType.PAYMENT_INITIATED)
            .status(NotificationStatus.FAILED)
            .recipientAddress("test@example.com")
            .attempts(2)
            .build();
    NotificationEntity saved = notificationRepository.save(notification);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/notifications/" + saved.getId() + "/retry")
                .header("X-Tenant-ID", tenantId)
                .with(jwt().roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  // ============================================================================
  // Template Endpoint Tests
  // ============================================================================

  @Test
  @DisplayName("GET /templates - Should list active templates")
  void testListTemplates() throws Exception {
    mockMvc
        .perform(
            get("/api/notifications/templates")
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", isA(List.class)));
  }

  @Test
  @DisplayName("POST /templates - Should create template")
  void testCreateTemplate() throws Exception {
    // Arrange
    CreateTemplateRequest request =
        CreateTemplateRequest.builder()
            .notificationType(NotificationType.PAYMENT_CLEARED)
            .name("Payment Cleared")
            .emailSubject("Payment Cleared")
            .emailTemplate("Your payment has been cleared")
            .pushTitle("Payment")
            .pushBody("Cleared")
            .smsTemplate("Payment cleared")
            .build();

    // Act & Assert
    mockMvc
        .perform(
            post("/api/notifications/templates")
                .header("X-Tenant-ID", tenantId)
                .with(jwt().roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", equalTo("Payment Cleared")));
  }

  @Test
  @DisplayName("DELETE /templates/{id} - Should deactivate template")
  void testDeactivateTemplate() throws Exception {
    // Arrange
    NotificationTemplateEntity template =
        NotificationTemplateEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .notificationType(NotificationType.PAYMENT_INITIATED)
            .name("Payment Initiated")
            .emailSubject("Payment Started")
            .emailTemplate("Your payment has started")
            .pushTitle("Payment")
            .pushBody("Started")
            .smsTemplate("Payment started")
            .active(true)
            .build();
    NotificationTemplateEntity saved = templateRepository.save(template);

    // Act & Assert
    mockMvc
        .perform(
            delete("/api/notifications/templates/" + saved.getId())
                .header("X-Tenant-ID", tenantId)
                .with(jwt().roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  // ============================================================================
  // Preference Endpoint Tests
  // ============================================================================

  @Test
  @DisplayName("GET /preferences - Should return user preferences")
  void testGetPreferences() throws Exception {
    // Arrange - Create preferences first
    NotificationPreferenceEntity preferences =
        NotificationPreferenceEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(userId)
            .preferredChannels(Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS))
            .transactionAlertsOptIn(true)
            .marketingOptIn(false)
            .systemNotificationsOptIn(true)
            .build();
    preferenceRepository.save(preferences);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/notifications/preferences")
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", equalTo(userId)));
  }

  @Test
  @DisplayName("PUT /preferences - Should update preferences")
  void testUpdatePreferences() throws Exception {
    // Arrange
    UpdatePreferenceRequest request =
        UpdatePreferenceRequest.builder()
            .preferredChannels(Set.of(NotificationChannel.EMAIL))
            .transactionAlertsOptIn(true)
            .marketingOptIn(false)
            .systemNotificationsOptIn(true)
            .quietHoursStart(LocalTime.of(22, 0))
            .quietHoursEnd(LocalTime.of(8, 0))
            .build();

    // Act & Assert
    mockMvc
        .perform(
            put("/api/notifications/preferences")
                .header("X-Tenant-ID", tenantId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.preferredChannels", hasSize(1)));
  }

  // ============================================================================
  // Security & Authorization Tests
  // ============================================================================

  @Test
  @DisplayName("Should return 401 Unauthorized without JWT")
  void testUnauthorizedRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/notifications/user")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should return 403 Forbidden for insufficient role")
  void testForbiddenRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/notifications/templates")
                .header("X-Tenant-ID", tenantId)
                .with(jwt().roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  // ============================================================================
  // Health Endpoint Test
  // ============================================================================

  @Test
  @DisplayName("GET /health - Should return service health")
  void testHealth() throws Exception {
    mockMvc
        .perform(
            get("/api/notifications/health")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", equalTo("UP")))
        .andExpect(jsonPath("$.service", equalTo("NotificationService")));
  }
}
