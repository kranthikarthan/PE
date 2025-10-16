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
public class MarkClearedRequest {
    @NotBlank(message = "Clearing system is required")
    private String clearingSystem;
    
    @NotBlank(message = "Clearing reference is required")
    private String clearingReference;
}






