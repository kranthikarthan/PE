package com.payments.settlement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.payments.settlement.event.SettlementEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SettlementController.class)
@TestPropertySource(
    properties = {
      "spring.cloud.config.import-check.enabled=false",
      "spring.config.import=optional:configserver:,optional:consul:"
    })
class SettlementControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SettlementEventPublisher settlementEventPublisher;

  @Test
  @WithMockUser
  void shouldCreateBatch_WhenValidRequest() throws Exception {
    var response =
        mockMvc
            .perform(post("/api/v1/settlement/batches").with(csrf()))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("BATCH-CREATED");
  }

  @Test
  @WithMockUser
  void shouldReturnPositions_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(get("/api/v1/settlement/positions"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("OK");
  }
}
