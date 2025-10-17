package com.payments.payshapadapter.config;

import com.payments.payshapadapter.repository.PayShapAdapterRepository;
// import org.springframework.boot.actuator.health.Health;
// import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Health indicator for PayShap adapter */
@Component
public class PayShapAdapterHealthIndicator /* implements HealthIndicator */ {

  private final PayShapAdapterRepository payShapAdapterRepository;

  public PayShapAdapterHealthIndicator(PayShapAdapterRepository payShapAdapterRepository) {
    this.payShapAdapterRepository = payShapAdapterRepository;
  }

  // TODO: Fix HealthIndicator implementation - temporarily commented out due to dependency issues
  // @Override
  // public Health health() {
  //   try {
  //     long totalAdapters = payShapAdapterRepository.count();
  //     long activeAdapters =
  // payShapAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);
  //     long inactiveAdapters =
  // payShapAdapterRepository.countByStatus(AdapterOperationalStatus.INACTIVE);

  //     if (totalAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No PayShap adapters configured")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .build();
  //     }

  //     if (activeAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No active PayShap adapters")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .withDetail("activeAdapters", activeAdapters)
  //           .withDetail("inactiveAdapters", inactiveAdapters)
  //           .build();
  //     }

  //     return Health.up()
  //         .withDetail("status", "PayShap adapters operational")
  //         .withDetail("totalAdapters", totalAdapters)
  //         .withDetail("activeAdapters", activeAdapters)
  //         .withDetail("inactiveAdapters", inactiveAdapters)
  //         .build();

  //   } catch (Exception e) {
  //     return Health.down()
  //         .withDetail("status", "PayShap adapter health check failed")
  //         .withDetail("error", e.getMessage())
  //         .build();
  //   }
  // }
}
