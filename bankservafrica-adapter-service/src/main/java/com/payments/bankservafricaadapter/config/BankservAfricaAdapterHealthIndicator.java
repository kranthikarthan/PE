package com.payments.bankservafricaadapter.config;

import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
// import org.springframework.boot.actuator.health.Health;
// import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Health indicator for BankservAfrica adapter */
@Component
public class BankservAfricaAdapterHealthIndicator /* implements HealthIndicator */ {

  private final BankservAfricaAdapterRepository adapterRepository;

  public BankservAfricaAdapterHealthIndicator(BankservAfricaAdapterRepository adapterRepository) {
    this.adapterRepository = adapterRepository;
  }

  // TODO: Fix HealthIndicator implementation - temporarily commented out due to dependency issues
  // @Override
  // public Health health() {
  //   try {
  //     long totalAdapters = adapterRepository.count();
  //     long activeAdapters = adapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);
  //     long inactiveAdapters = adapterRepository.countByStatus(AdapterOperationalStatus.INACTIVE);

  //     if (totalAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No BankservAfrica adapters configured")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .build();
  //     }

  //     if (activeAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No active BankservAfrica adapters")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .withDetail("activeAdapters", activeAdapters)
  //           .withDetail("inactiveAdapters", inactiveAdapters)
  //           .build();
  //     }

  //     return Health.up()
  //         .withDetail("status", "BankservAfrica adapters operational")
  //         .withDetail("totalAdapters", totalAdapters)
  //         .withDetail("activeAdapters", activeAdapters)
  //         .withDetail("inactiveAdapters", inactiveAdapters)
  //         .build();

  //   } catch (Exception e) {
  //     return Health.down()
  //         .withDetail("status", "BankservAfrica adapter health check failed")
  //         .withDetail("error", e.getMessage())
  //         .build();
  //   }
  // }
}
