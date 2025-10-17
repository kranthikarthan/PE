package com.payments.rtcadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * RTC Adapter Service Application
 *
 * <p>Real-Time Clearing adapter for instant low-value payments: - ISO 20022 messaging
 * (pacs.008/pacs.002) - Amount limit: R5,000 per transaction - 24/7/365 availability - Real-time
 * settlement (seconds) - REST API protocol
 */
@SpringBootApplication
@EnableFeignClients
public class RtcAdapterApplication {

  public static void main(String[] args) {
    SpringApplication.run(RtcAdapterApplication.class, args);
  }
}
