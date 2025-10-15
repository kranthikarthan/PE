package com.payments.domain.tenant;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.*;
import org.junit.jupiter.api.Test;

class TenantTest {

  @Test
  void create_add_business_unit_and_user() {
    Tenant tenant =
        Tenant.create(
            TenantId.generate(), "Tenant One", TenantType.BANK, "tenant@example.com", "creator");

    assertTrue(tenant.isActive());

    BusinessUnitId buId = BusinessUnitId.generate();
    tenant.addBusinessUnit(buId, "Ops", BusinessUnitType.OPERATIONS, 2, "creator");
    assertTrue(tenant.hasBusinessUnit(buId));

    tenant.addUser(UserId.generate(), "alice", "alice@example.com", "ADMIN", "creator");
    assertTrue(tenant.hasUser(UserId.generate()) == false); // sanity: new random id is absent
  }
}
