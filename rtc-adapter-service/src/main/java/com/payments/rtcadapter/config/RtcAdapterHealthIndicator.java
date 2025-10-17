package com.payments.rtcadapter.config;

import com.payments.rtcadapter.repository.RtcAdapterRepository;
// import org.springframework.boot.actuator.health.Health;
// import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Health indicator for RTC adapter */
@Component
public class RtcAdapterHealthIndicator /* implements HealthIndicator */ {

  private final RtcAdapterRepository rtcAdapterRepository;

  public RtcAdapterHealthIndicator(RtcAdapterRepository rtcAdapterRepository) {
    this.rtcAdapterRepository = rtcAdapterRepository;
  }

  // TODO: Fix HealthIndicator implementation - temporarily commented out due to dependency issues
  // @Override
  // public Health health() {
  //   try {
  //     long totalAdapters = rtcAdapterRepository.count();
  //     long activeAdapters = rtcAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);
  //     long inactiveAdapters =
  // rtcAdapterRepository.countByStatus(AdapterOperationalStatus.INACTIVE);

  //     if (totalAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No RTC adapters configured")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .build();
  //     }

  //     if (activeAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No active RTC adapters")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .withDetail("activeAdapters", activeAdapters)
  //           .withDetail("inactiveAdapters", inactiveAdapters)
  //           .build();
  //     }

  //     return Health.up()
  //         .withDetail("status", "RTC adapters operational")
  //         .withDetail("totalAdapters", totalAdapters)
  //         .withDetail("activeAdapters", activeAdapters)
  //         .withDetail("inactiveAdapters", inactiveAdapters)
  //         .build();

  //   } catch (Exception e) {
  //     return Health.down()
  //         .withDetail("status", "RTC adapter health check failed")
  //         .withDetail("error", e.getMessage())
  //         .build();
  //   }
  // }
}
