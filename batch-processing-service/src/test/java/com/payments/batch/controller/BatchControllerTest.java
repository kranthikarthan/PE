package com.payments.batch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

  private MockMvc mockMvc;

  @Mock private JobLauncher jobLauncher;
  @Mock private Job importJob;

  @InjectMocks private BatchController batchController;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
  }

  @Test
  void shouldSubmitBatch_WhenRequested() throws Exception {
    var response =
        mockMvc
            .perform(post("/api/v1/batch/submit"))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).isEqualTo("BATCH_SUBMITTED");
  }
}
