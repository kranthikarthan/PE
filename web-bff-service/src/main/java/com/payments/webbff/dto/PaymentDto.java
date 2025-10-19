package com.payments.webbff.dto;

import com.payments.webbff.type.ClearingSystem;
import com.payments.webbff.type.PaymentStatus;
import com.payments.webbff.type.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Payment information.
 *
 * <p>This DTO represents a payment in the GraphQL API, containing all
 * relevant payment information including status, amounts, and participant details.
 *
 * @since PE-414
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    
    private UUID id;
    private String transactionReference;
    private BigDecimal amount;
    private String currency;
    private String debtorName;
    private String debtorAccount;
    private String creditorName;
    private String creditorAccount;
    private String description;
    private PaymentStatus status;
    private PaymentType paymentType;
    private ClearingSystem clearingSystem;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UUID tenantId;
    private UUID businessUnitId;
}
