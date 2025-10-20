package com.payments.operationsmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Feature Flag DTO
 * 
 * Data Transfer Object for feature flag information.
 * Used in API responses for feature flag management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagDto {

    private String name;
    private String description;
    private Boolean enabled;
    private Integer rolloutPercentage;
    private String strategy;
    private String[] variants;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
