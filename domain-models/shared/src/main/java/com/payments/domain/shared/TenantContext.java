package com.payments.domain.shared;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import jakarta.persistence.Embeddable;

/**
 * TenantContext - Value Object
 */
@Embeddable
@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class TenantContext {
    String tenantId;
    String tenantName;
    String businessUnitId;
    String businessUnitName;
    
    public static TenantContext of(
        String tenantId, 
        String tenantName,
        String businessUnitId,
        String businessUnitName
    ) {
        return new TenantContext(tenantId, tenantName, businessUnitId, businessUnitName);
    }
}
