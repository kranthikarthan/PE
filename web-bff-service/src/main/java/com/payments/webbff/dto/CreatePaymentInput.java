package com.payments.webbff.dto;

import com.payments.webbff.type.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Input DTO for creating a new payment.
 *
 * <p>This DTO contains all the required information to create a new payment
 * in the Payment Engine system.
 *
 * @since PE-414
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentInput {
    
    private String transactionReference;
    private BigDecimal amount;
    private String currency;
    private String debtorName;
    private String debtorAccount;
    private String creditorName;
    private String creditorAccount;
    private String description;
    private PaymentType paymentType;
    private UUID tenantId;
    private UUID businessUnitId;
}
