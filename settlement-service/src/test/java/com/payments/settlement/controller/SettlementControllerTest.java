package com.payments.settlement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.payments.settlement.event.SettlementEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SettlementEventPublisher settlementEventPublisher;

  @Test
  void shouldCreateBatch_WhenValidRequest() throws Exception {
    var response =
        mockMvc
            .perform(post("/api/v1/settlement/batches"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("BATCH-CREATED");
  }

  @Test
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
