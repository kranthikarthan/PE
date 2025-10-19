package com.payments.webbff.type;

/**
 * Enumeration for Payment Type.
 *
 * <p>This enum represents the different types of payments supported by the Payment Engine system.
 *
 * @since PE-414
 */
public enum PaymentType {
    RTGS,
    ACH,
    RTC,
    INSTANT_P2P,
    INTERNATIONAL,
    CARD
}
