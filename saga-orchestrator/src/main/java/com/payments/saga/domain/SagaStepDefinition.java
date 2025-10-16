package com.payments.saga.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Definition of a saga step that can be executed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepDefinition {
    private String stepName;
    private SagaStepType stepType;
    private String serviceName;
    private String endpoint;
    private String compensationEndpoint;
    private int maxRetries;
    private long timeoutSeconds;
    private Map<String, Object> defaultInputData;
    private boolean isOptional;
    private boolean isCompensationStep;
    private String description;

    public static SagaStepDefinition create(String stepName, SagaStepType stepType, 
                                          String serviceName, String endpoint, String compensationEndpoint) {
        return SagaStepDefinition.builder()
                .stepName(stepName)
                .stepType(stepType)
                .serviceName(serviceName)
                .endpoint(endpoint)
                .compensationEndpoint(compensationEndpoint)
                .maxRetries(3)
                .timeoutSeconds(30)
                .isOptional(false)
                .isCompensationStep(false)
                .build();
    }

    public static SagaStepDefinition createCompensation(String stepName, SagaStepType stepType,
                                                       String serviceName, String compensationEndpoint) {
        return SagaStepDefinition.builder()
                .stepName(stepName)
                .stepType(stepType)
                .serviceName(serviceName)
                .endpoint(null)
                .compensationEndpoint(compensationEndpoint)
                .maxRetries(3)
                .timeoutSeconds(30)
                .isOptional(false)
                .isCompensationStep(true)
                .build();
    }

    public boolean isCompensationStep() {
        return isCompensationStep;
    }

    public boolean isBusinessStep() {
        return !isCompensationStep;
    }

    public boolean hasCompensation() {
        return compensationEndpoint != null && !compensationEndpoint.trim().isEmpty();
    }
}






