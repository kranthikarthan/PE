package com.payments.samosadapter.config;

import com.payments.samosadapter.repository.SamosAdapterRepository;
import com.payments.domain.clearing.AdapterOperationalStatus;
// import org.springframework.boot.actuator.health.Health;
// import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for SAMOS adapter
 */
@Component
public class SamosAdapterHealthIndicator /* implements HealthIndicator */ {

  private final SamosAdapterRepository samosAdapterRepository;

  public SamosAdapterHealthIndicator(SamosAdapterRepository samosAdapterRepository) {
    this.samosAdapterRepository = samosAdapterRepository;
  }

  // @Override
  // public Health health() {
  //   try {
  //     long totalAdapters = samosAdapterRepository.count();
  //     long activeAdapters = samosAdapterRepository.countByStatus(AdapterOperationalStatus.ACTIVE);
  //     long inactiveAdapters = samosAdapterRepository.countByStatus(AdapterOperationalStatus.INACTIVE);

  //     if (totalAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No SAMOS adapters configured")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .build();
  //     }

  //     if (activeAdapters == 0) {
  //       return Health.down()
  //           .withDetail("status", "No active SAMOS adapters")
  //           .withDetail("totalAdapters", totalAdapters)
  //           .withDetail("activeAdapters", activeAdapters)
  //           .withDetail("inactiveAdapters", inactiveAdapters)
  //           .build();
  //     }

  //     return Health.up()
  //         .withDetail("status", "SAMOS adapters operational")
  //         .withDetail("totalAdapters", totalAdapters)
  //         .withDetail("activeAdapters", activeAdapters)
  //         .withDetail("inactiveAdapters", inactiveAdapters)
  //         .build();

  //   } catch (Exception e) {
  //     return Health.down()
  //         .withDetail("status", "SAMOS adapter health check failed")
  //         .withDetail("error", e.getMessage())
  //         .build();
  //   }
  // }
}
