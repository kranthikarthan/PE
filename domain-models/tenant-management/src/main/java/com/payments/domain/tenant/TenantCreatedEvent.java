package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Tenant Created
 */
@Value
@AllArgsConstructor
public class TenantCreatedEvent implements DomainEvent {
    TenantId tenantId;
    String tenantName;
    TenantType tenantType;
    Instant createdAt;
    
    @Override
    public String getEventType() {
        return "TenantCreated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return createdAt;
    }
}
