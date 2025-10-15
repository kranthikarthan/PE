package com.payments.domain.shared;

public interface TenantAware {
  TenantContext getTenantContext();
}
