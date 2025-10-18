package com.payments.batch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {

  private final JobLauncher jobLauncher;
  private final Job importJob;

  public BatchController(JobLauncher jobLauncher, Job importJob) {
    this.jobLauncher = jobLauncher;
    this.importJob = importJob;
  }

  @PostMapping("/submit")
  public ResponseEntity<String> submitJob() throws Exception {
    jobLauncher.run(
        importJob,
        new JobParametersBuilder().addLong("ts", System.currentTimeMillis()).toJobParameters());
    return ResponseEntity.accepted().body("BATCH_SUBMITTED");
  }
}
