package com.payments.reconciliation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.payments.reconciliation.event.ReconciliationEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReconciliationController.class)
class ReconciliationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ReconciliationEventPublisher reconciliationEventPublisher;

  @Test
  void shouldRunReconciliation_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(post("/api/v1/reconciliation/run"))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("RECONCILIATION-RUNNING");
  }

  @Test
  void shouldListExceptions_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(get("/api/v1/reconciliation/exceptions"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("[]");
  }
}
