package com.payments.e2e.models;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Payment Response Model for E2E Tests
 */
@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private String status;
    private String message;
    private Date timestamp;
    private String errorCode;
    private String errorMessage;
}
