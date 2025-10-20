package com.payments.e2e.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Payment Request Model for E2E Tests
 */
@Data
@Builder
public class PaymentRequest {
    private String paymentId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String paymentType;
    private String status;
    private String tenantId;
    private String clearingSystem;
    private Date createdAt;
    private Date updatedAt;
}
