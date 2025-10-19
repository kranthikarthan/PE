package com.payments.webbff.dto;

import com.payments.webbff.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input DTO for updating an existing payment.
 *
 * <p>This DTO contains the fields that can be updated for an existing payment
 * in the Payment Engine system.
 *
 * @since PE-414
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentInput {
    
    private PaymentStatus status;
    private String description;
}
