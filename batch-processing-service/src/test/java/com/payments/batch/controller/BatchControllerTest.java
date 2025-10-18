package com.payments.batch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BatchController.class)
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "spring.cloud.config.fail-fast=false",
      "spring.cloud.config.enabled=false",
      "spring.cloud.bootstrap.enabled=false",
      "spring.cloud.config.import-check.enabled=false",
      "spring.cloud.consul.config.enabled=false",
      "spring.cloud.discovery.enabled=false"
    })
class BatchControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JobLauncher jobLauncher;
  @MockBean private Job importJob;

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void shouldSubmitBatch_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(post("/api/v1/batch/submit").with(csrf()))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("BATCH_SUBMITTED");
  }
}
