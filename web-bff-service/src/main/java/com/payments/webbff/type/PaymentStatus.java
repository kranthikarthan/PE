package com.payments.webbff.type;

/**
 * Enumeration for Payment Status.
 *
 * <p>This enum represents the possible states of a payment in the Payment Engine system.
 *
 * @since PE-414
 */
public enum PaymentStatus {
    PENDING,
    VALIDATED,
    SUBMITTED_TO_CLEARING,
    CLEARED,
    COMPLETED,
    FAILED,
    CANCELLED
}
