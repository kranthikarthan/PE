package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Tenant Suspended
 */
@Value
@AllArgsConstructor
public class TenantSuspendedEvent implements DomainEvent {
    TenantId tenantId;
    String reason;
    String suspendedBy;
    
    @Override
    public String getEventType() {
        return "TenantSuspended";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
