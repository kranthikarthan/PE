package com.payments.domain.valueobjects;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Value object for clearing confirmation
 */
@Data
@Builder
@EqualsAndHashCode
public class ClearingConfirmation {
    
    private final String clearingSystemId;
    private final String confirmationId;
    private final String status;
    private final String message;
    private final Instant confirmedAt;
    
    public ClearingConfirmation(String clearingSystemId, String confirmationId, String status, String message, Instant confirmedAt) {
        if (clearingSystemId == null || clearingSystemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Clearing system ID cannot be null or empty");
        }
        if (confirmationId == null || confirmationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmation ID cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (confirmedAt == null) {
            throw new IllegalArgumentException("Confirmed at cannot be null");
        }
        this.clearingSystemId = clearingSystemId.trim();
        this.confirmationId = confirmationId.trim();
        this.status = status.trim();
        this.message = message != null ? message.trim() : null;
        this.confirmedAt = confirmedAt;
    }
}
