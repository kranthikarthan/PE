package com.payments.operationsmanagement.dto;

import com.payments.operationsmanagement.entity.PodInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pod Information DTO
 * 
 * Data Transfer Object for Kubernetes pod information.
 * Used in API responses for pod monitoring and management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodInfoDto {

    private String name;
    private String namespace;
    private PodInfoEntity.PodStatus status;
    private Double cpuUsagePercentage;
    private Long memoryUsageMb;
    private Integer restartCount;
    private Long ageSeconds;
    private String nodeName;
    private String ipAddress;
}
