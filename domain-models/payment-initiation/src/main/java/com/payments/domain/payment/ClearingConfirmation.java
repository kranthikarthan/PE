package com.payments.domain.payment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import jakarta.persistence.Embeddable;

@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class ClearingConfirmation {
    String confirmationNumber;

    private ClearingConfirmation(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.isBlank()) {
            throw new IllegalArgumentException("Confirmation number cannot be null or blank");
        }
        this.confirmationNumber = confirmationNumber;
    }

    public static ClearingConfirmation of(String confirmationNumber) {
        return new ClearingConfirmation(confirmationNumber);
    }
}


