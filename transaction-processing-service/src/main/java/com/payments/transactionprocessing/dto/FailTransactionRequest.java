package com.payments.transactionprocessing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailTransactionRequest {
    @NotBlank(message = "Failure reason is required")
    private String reason;
}






