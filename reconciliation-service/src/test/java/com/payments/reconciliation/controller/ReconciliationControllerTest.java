package com.payments.reconciliation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.payments.reconciliation.event.ReconciliationEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ReconciliationController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    })
@TestPropertySource(
    properties = {
      "spring.config.import=optional:configserver:,optional:consul:",
      "spring.cloud.vault.enabled=false"
    })
class ReconciliationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ReconciliationEventPublisher reconciliationEventPublisher;

  @Test
  @WithMockUser
  void shouldRunReconciliation_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(post("/api/v1/reconciliation/run").param("system", "RTC").with(csrf()))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("RECONCILIATION-RUNNING");
  }

  @Test
  @WithMockUser
  void shouldReturnBadRequest_WhenSystemParamTooShort() throws Exception {
    mockMvc
        .perform(post("/api/v1/reconciliation/run").param("system", "R").with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void shouldListExceptions_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(get("/api/v1/reconciliation/exceptions").with(csrf()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("[]");
  }
}
