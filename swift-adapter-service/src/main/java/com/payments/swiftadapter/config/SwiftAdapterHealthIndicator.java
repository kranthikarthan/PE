package com.payments.swiftadapter.config;

import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import com.payments.domain.clearing.AdapterOperationalStatus;
// import org.springframework.boot.actuator.health.Health;
// import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for SWIFT adapter
 */
@Component
public class SwiftAdapterHealthIndicator /* implements HealthIndicator */ {

  private final SwiftAdapterRepository swiftAdapterRepository;

  public SwiftAdapterHealthIndicator(SwiftAdapterRepository swiftAdapterRepository) {
    this.swiftAdapterRepository = swiftAdapterRepository;
  }

  // TODO: Fix HealthIndicator implementation - temporarily commented out due to dependency issues
  // @Override
  // public Health health() {
  //   try {
  //     long totalAdapters = swiftAdapterRepository.count();
  //     long activeAdapters = swiftAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);
  //     long inactiveAdapters = swiftAdapterRepository.countByStatus(AdapterOperationalStatus.INACTIVE);

  //     if (totalAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No SWIFT adapters configured")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .build();
  //     }

  //     if (activeAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No active SWIFT adapters")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .withDetail("activeAdapters", activeAdapters)
  //           .withDetail("inactiveAdapters", inactiveAdapters)
  //           .build();
  //     }

  //     return Health.up()
  //         .withDetail("status", "SWIFT adapters operational")
  //         .withDetail("totalAdapters", totalAdapters)
  //         .withDetail("activeAdapters", activeAdapters)
  //         .withDetail("inactiveAdapters", inactiveAdapters)
  //         .build();

  //   } catch (Exception e) {
  //     return Health.down()
  //         .withDetail("status", "SWIFT adapter health check failed")
  //         .withDetail("error", e.getMessage())
  //         .build();
  //   }
  // }
}