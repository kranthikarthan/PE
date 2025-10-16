package com.payments.paymentinitiation.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Payment Business Rules Service
 *
 * <p>Manages configurable business rules for payment processing In a real implementation, this
 * would integrate with a rules engine
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentBusinessRulesService {

  // In-memory cache for business rules (in production, this would be from a database)
  private final Map<String, BusinessRules> tenantRules = new ConcurrentHashMap<>();

  /**
   * Get business rules for tenant
   *
   * @param tenantId Tenant ID
   * @return Business rules for tenant
   */
  public BusinessRules getBusinessRulesForTenant(String tenantId) {
    return tenantRules.computeIfAbsent(tenantId, this::createDefaultRules);
  }

  /**
   * Update business rules for tenant
   *
   * @param tenantId Tenant ID
   * @param rules New business rules
   */
  public void updateBusinessRulesForTenant(String tenantId, BusinessRules rules) {
    tenantRules.put(tenantId, rules);
    log.info("Updated business rules for tenant: {}", tenantId);
  }

  /** Create default business rules */
  private BusinessRules createDefaultRules(String tenantId) {
    return BusinessRules.builder()
        .tenantId(tenantId)
        .dailyLimit(BigDecimal.valueOf(1000000.00)) // 1M ZAR
        .velocityLimit(100) // 100 payments per hour
        .complianceThreshold(BigDecimal.valueOf(50000.00)) // 50K ZAR
        .maxAmount(BigDecimal.valueOf(100000.00)) // 100K ZAR per payment
        .minAmount(BigDecimal.valueOf(0.01)) // 1 cent minimum
        .allowedPaymentTypes(java.util.Set.of("EFT", "IMMEDIATE_PAYMENT"))
        .restrictedAccounts(java.util.Set.of())
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /** Business rules data class */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class BusinessRules {
    private String tenantId;
    private BigDecimal dailyLimit;
    private Integer velocityLimit;
    private BigDecimal complianceThreshold;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private java.util.Set<String> allowedPaymentTypes;
    private java.util.Set<String> restrictedAccounts;
    private Instant createdAt;
    private Instant updatedAt;
  }
}
