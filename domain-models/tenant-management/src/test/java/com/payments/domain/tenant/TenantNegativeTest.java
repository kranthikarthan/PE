package com.payments.domain.tenant;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.*;
import org.junit.jupiter.api.Test;

class TenantNegativeTest {

  @Test
  void create_fails_when_name_blank() {
    assertThrows(
        InvalidTenantException.class,
        () ->
            Tenant.create(
                TenantId.generate(), " ", TenantType.BANK, "tenant@example.com", "creator"));
  }

  @Test
  void create_fails_when_email_blank() {
    assertThrows(
        InvalidTenantException.class,
        () -> Tenant.create(TenantId.generate(), "Tenant", TenantType.BANK, "", "creator"));
  }

  @Test
  void addBusinessUnit_fails_when_name_blank() {
    Tenant tenant =
        Tenant.create(
            TenantId.generate(), "Tenant", TenantType.BANK, "tenant@example.com", "creator");
    assertThrows(
        InvalidTenantException.class,
        () ->
            tenant.addBusinessUnit(
                BusinessUnitId.generate(), "", BusinessUnitType.OPERATIONS, 2, "creator"));
  }

  @Test
  void suspend_fails_when_already_suspended() {
    Tenant tenant =
        Tenant.create(
            TenantId.generate(), "Tenant", TenantType.BANK, "tenant@example.com", "creator");
    tenant.suspend("reason", "admin");
    assertThrows(InvalidTenantException.class, () -> tenant.suspend("again", "admin"));
  }

  @Test
  void activate_fails_when_already_active() {
    Tenant tenant =
        Tenant.create(
            TenantId.generate(), "Tenant", TenantType.BANK, "tenant@example.com", "creator");
    assertThrows(InvalidTenantException.class, () -> tenant.activate("admin"));
  }
}
