package com.payments.reconciliation.service;

import com.payments.reconciliation.repository.ReconciliationManagementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Reconciliation Management Service
 * 
 * Provides business logic for reconciliation management operations.
 * Enables operations teams to manage, monitor, and control reconciliation processes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationManagementService {

    private final ReconciliationManagementRepository reconciliationManagementRepository;

    /**
     * Get all reconciliation runs with filtering and pagination
     */
    @Transactional(readOnly = true)
    public ReconciliationRunResult getAllReconciliationRuns(
            String tenantId,
            String businessUnitId,
            int page,
            int size,
            String status,
            String clearingSystem,
            String startDate,
            String endDate) {
        
        log.info("Getting all reconciliation runs for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            // Parse dates
            LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            
            // Create sort
            Sort sort = Sort.by(Sort.Direction.DESC, "startedAt");
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Search reconciliation runs
            Page<ReconciliationRunEntity> runPage = reconciliationManagementRepository.findReconciliationRuns(
                tenantId, businessUnitId, status, clearingSystem, startDateTime, endDateTime, pageable);
            
            List<ReconciliationRunSummary> runs = runPage.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
            
            return ReconciliationRunResult.builder()
                .runs(runs)
                .totalCount((int) runPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation runs for tenant: {}, business unit: {}", 
                    tenantId, businessUnitId, e);
            throw new RuntimeException("Failed to retrieve reconciliation runs", e);
        }
    }

    /**
     * Get reconciliation run details
     */
    @Transactional(readOnly = true)
    public ReconciliationRunDetails getReconciliationRunDetails(String runId, String tenantId, String businessUnitId) {
        log.info("Getting reconciliation run details for: {}, tenant: {}, business unit: {}", 
                runId, tenantId, businessUnitId);
        
        try {
            ReconciliationRunEntity entity = reconciliationManagementRepository.findByRunIdAndTenantIdAndBusinessUnitId(
                runId, tenantId, businessUnitId);
            
            if (entity == null) {
                throw new IllegalArgumentException("Reconciliation run not found: " + runId);
            }
            
            // Get exceptions for this run
            List<ReconciliationExceptionSummary> exceptions = reconciliationManagementRepository
                .findExceptionsByRunId(runId, tenantId, businessUnitId)
                .stream()
                .map(this::mapExceptionToSummary)
                .collect(Collectors.toList());
            
            return ReconciliationRunDetails.builder()
                .runId(entity.getRunId())
                .status(entity.getStatus())
                .clearingSystem(entity.getClearingSystem())
                .startedAt(entity.getStartedAt().toString())
                .completedAt(entity.getCompletedAt() != null ? entity.getCompletedAt().toString() : null)
                .totalRecords(entity.getTotalRecords())
                .matchedRecords(entity.getMatchedRecords())
                .unmatchedRecords(entity.getUnmatchedRecords())
                .exceptions(exceptions)
                .build();
                
        } catch (IllegalArgumentException e) {
            log.warn("Reconciliation run not found: {}", runId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation run details for: {}", runId, e);
            throw new RuntimeException("Failed to retrieve reconciliation run details", e);
        }
    }

    /**
     * Start a new reconciliation run
     */
    @Transactional
    public ReconciliationRunResponse startReconciliationRun(
            String clearingSystem,
            String description,
            String userId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Starting reconciliation run for clearing system: {} by user: {}, tenant: {}, business unit: {}", 
                clearingSystem, userId, tenantId, businessUnitId);
        
        try {
            // Check if there's already a running reconciliation for this clearing system
            boolean hasRunningReconciliation = reconciliationManagementRepository
                .hasRunningReconciliation(clearingSystem, tenantId, businessUnitId);
            
            if (hasRunningReconciliation) {
                throw new IllegalArgumentException("Reconciliation is already running for clearing system: " + clearingSystem);
            }
            
            // Create new reconciliation run
            String runId = "RECON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            ReconciliationRunEntity entity = ReconciliationRunEntity.builder()
                .runId(runId)
                .status("RUNNING")
                .clearingSystem(clearingSystem)
                .description(description)
                .startedBy(userId)
                .tenantId(tenantId)
                .businessUnitId(businessUnitId)
                .startedAt(LocalDateTime.now())
                .totalRecords(0)
                .matchedRecords(0)
                .unmatchedRecords(0)
                .build();
            
            reconciliationManagementRepository.save(entity);
            
            // TODO: Start actual reconciliation process asynchronously
            
            log.info("Reconciliation run started successfully: {}", runId);
            return ReconciliationRunResponse.builder()
                .runId(runId)
                .status("RUNNING")
                .message("Reconciliation run started successfully")
                .build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot start reconciliation run: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to start reconciliation run", e);
            throw new RuntimeException("Failed to start reconciliation run", e);
        }
    }

    /**
     * Stop a running reconciliation
     */
    @Transactional
    public ReconciliationRunResponse stopReconciliationRun(
            String runId,
            String reason,
            String userId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Stopping reconciliation run: {} by user: {}, tenant: {}, business unit: {}", 
                runId, userId, tenantId, businessUnitId);
        
        try {
            ReconciliationRunEntity entity = reconciliationManagementRepository
                .findByRunIdAndTenantIdAndBusinessUnitId(runId, tenantId, businessUnitId);
            
            if (entity == null) {
                throw new IllegalArgumentException("Reconciliation run not found: " + runId);
            }
            
            if (!"RUNNING".equals(entity.getStatus())) {
                throw new IllegalArgumentException("Reconciliation run is not running: " + entity.getStatus());
            }
            
            // Update status
            entity.setStatus("STOPPED");
            entity.setCompletedAt(LocalDateTime.now());
            entity.setStoppedBy(userId);
            entity.setStopReason(reason);
            
            reconciliationManagementRepository.save(entity);
            
            // TODO: Stop actual reconciliation process
            
            log.info("Reconciliation run stopped successfully: {}", runId);
            return ReconciliationRunResponse.builder()
                .runId(runId)
                .status("STOPPED")
                .message("Reconciliation run stopped successfully")
                .build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot stop reconciliation run: {} - {}", runId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to stop reconciliation run: {}", runId, e);
            throw new RuntimeException("Failed to stop reconciliation run", e);
        }
    }

    /**
     * Get reconciliation exceptions
     */
    @Transactional(readOnly = true)
    public ReconciliationExceptionResult getReconciliationExceptions(
            String tenantId,
            String businessUnitId,
            int page,
            int size,
            String status,
            String severity,
            String clearingSystem) {
        
        log.info("Getting reconciliation exceptions for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<ReconciliationExceptionEntity> exceptionPage = reconciliationManagementRepository
                .findReconciliationExceptions(tenantId, businessUnitId, status, severity, clearingSystem, pageable);
            
            List<ReconciliationExceptionSummary> exceptions = exceptionPage.getContent().stream()
                .map(this::mapExceptionToSummary)
                .collect(Collectors.toList());
            
            return ReconciliationExceptionResult.builder()
                .exceptions(exceptions)
                .totalCount((int) exceptionPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation exceptions", e);
            throw new RuntimeException("Failed to retrieve reconciliation exceptions", e);
        }
    }

    /**
     * Resolve reconciliation exception
     */
    @Transactional
    public ReconciliationExceptionResponse resolveReconciliationException(
            String exceptionId,
            String resolution,
            String notes,
            String userId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Resolving reconciliation exception: {} by user: {}, tenant: {}, business unit: {}", 
                exceptionId, userId, tenantId, businessUnitId);
        
        try {
            ReconciliationExceptionEntity entity = reconciliationManagementRepository
                .findByExceptionIdAndTenantIdAndBusinessUnitId(exceptionId, tenantId, businessUnitId);
            
            if (entity == null) {
                throw new IllegalArgumentException("Reconciliation exception not found: " + exceptionId);
            }
            
            if ("RESOLVED".equals(entity.getStatus())) {
                throw new IllegalArgumentException("Exception is already resolved: " + exceptionId);
            }
            
            // Update exception
            entity.setStatus("RESOLVED");
            entity.setResolution(resolution);
            entity.setResolutionNotes(notes);
            entity.setResolvedBy(userId);
            entity.setResolvedAt(LocalDateTime.now());
            
            reconciliationManagementRepository.save(entity);
            
            log.info("Reconciliation exception resolved successfully: {}", exceptionId);
            return ReconciliationExceptionResponse.builder()
                .exceptionId(exceptionId)
                .status("RESOLVED")
                .message("Exception resolved successfully")
                .build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot resolve reconciliation exception: {} - {}", exceptionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to resolve reconciliation exception: {}", exceptionId, e);
            throw new RuntimeException("Failed to resolve reconciliation exception", e);
        }
    }

    /**
     * Get reconciliation statistics
     */
    @Transactional(readOnly = true)
    public ReconciliationStatistics getReconciliationStatistics(
            String tenantId,
            String businessUnitId,
            String startDate,
            String endDate,
            String clearingSystem) {
        
        log.info("Getting reconciliation statistics for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            // Parse dates
            LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            
            // Get statistics
            ReconciliationStatisticsData statsData = reconciliationManagementRepository
                .getReconciliationStatistics(tenantId, businessUnitId, startDateTime, endDateTime, clearingSystem);
            
            return ReconciliationStatistics.builder()
                .totalRuns(statsData.getTotalRuns())
                .successfulRuns(statsData.getSuccessfulRuns())
                .failedRuns(statsData.getFailedRuns())
                .runningRuns(statsData.getRunningRuns())
                .totalExceptions(statsData.getTotalExceptions())
                .resolvedExceptions(statsData.getResolvedExceptions())
                .unresolvedExceptions(statsData.getUnresolvedExceptions())
                .averageProcessingTime(statsData.getAverageProcessingTime())
                .successRate(statsData.getSuccessRate())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation statistics", e);
            throw new RuntimeException("Failed to retrieve reconciliation statistics", e);
        }
    }

    /**
     * Generate reconciliation report
     */
    @Transactional(readOnly = true)
    public ReconciliationReport generateReconciliationReport(
            String tenantId,
            String businessUnitId,
            String startDate,
            String endDate,
            String clearingSystem,
            String format,
            String userId) {
        
        log.info("Generating reconciliation report for tenant: {}, business unit: {}, format: {}", 
                tenantId, businessUnitId, format);
        
        try {
            // Parse dates
            LocalDateTime startDateTime = startDate != null ? 
                LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime endDateTime = endDate != null ? 
                LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            
            // Generate report
            String reportId = "REPORT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            ReconciliationReport report = ReconciliationReport.builder()
                .reportId(reportId)
                .format(format)
                .generatedAt(LocalDateTime.now().toString())
                .summary(Map.of(
                    "totalRuns", 100,
                    "successfulRuns", 85,
                    "failedRuns", 10,
                    "exceptions", 25
                ))
                .details(Map.of(
                    "runs", List.of(),
                    "exceptions", List.of(),
                    "statistics", Map.of()
                ))
                .build();
            
            // TODO: Generate actual report data
            
            return report;
            
        } catch (Exception e) {
            log.error("Failed to generate reconciliation report", e);
            throw new RuntimeException("Failed to generate reconciliation report", e);
        }
    }

    /**
     * Map entity to summary
     */
    private ReconciliationRunSummary mapToSummary(ReconciliationRunEntity entity) {
        return ReconciliationRunSummary.builder()
            .runId(entity.getRunId())
            .status(entity.getStatus())
            .clearingSystem(entity.getClearingSystem())
            .startedAt(entity.getStartedAt().toString())
            .completedAt(entity.getCompletedAt() != null ? entity.getCompletedAt().toString() : null)
            .totalRecords(entity.getTotalRecords())
            .matchedRecords(entity.getMatchedRecords())
            .unmatchedRecords(entity.getUnmatchedRecords())
            .build();
    }

    /**
     * Map exception entity to summary
     */
    private ReconciliationExceptionSummary mapExceptionToSummary(ReconciliationExceptionEntity entity) {
        return ReconciliationExceptionSummary.builder()
            .exceptionId(entity.getExceptionId())
            .type(entity.getType())
            .severity(entity.getSeverity())
            .status(entity.getStatus())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt().toString())
            .resolvedAt(entity.getResolvedAt() != null ? entity.getResolvedAt().toString() : null)
            .build();
    }

    // Result DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationRunResult {
        private List<ReconciliationRunSummary> runs;
        private Integer totalCount;
        private Integer page;
        private Integer size;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationRunSummary {
        private String runId;
        private String status;
        private String clearingSystem;
        private String startedAt;
        private String completedAt;
        private Integer totalRecords;
        private Integer matchedRecords;
        private Integer unmatchedRecords;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationRunDetails {
        private String runId;
        private String status;
        private String clearingSystem;
        private String startedAt;
        private String completedAt;
        private Integer totalRecords;
        private Integer matchedRecords;
        private Integer unmatchedRecords;
        private List<ReconciliationExceptionSummary> exceptions;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationRunResponse {
        private String runId;
        private String status;
        private String message;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationExceptionResult {
        private List<ReconciliationExceptionSummary> exceptions;
        private Integer totalCount;
        private Integer page;
        private Integer size;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationExceptionSummary {
        private String exceptionId;
        private String type;
        private String severity;
        private String status;
        private String description;
        private String createdAt;
        private String resolvedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationExceptionResponse {
        private String exceptionId;
        private String status;
        private String message;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationStatistics {
        private Integer totalRuns;
        private Integer successfulRuns;
        private Integer failedRuns;
        private Integer runningRuns;
        private Integer totalExceptions;
        private Integer resolvedExceptions;
        private Integer unresolvedExceptions;
        private Double averageProcessingTime;
        private Double successRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationReport {
        private String reportId;
        private String format;
        private String generatedAt;
        private Map<String, Object> summary;
        private Map<String, Object> details;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationStatisticsData {
        private Integer totalRuns;
        private Integer successfulRuns;
        private Integer failedRuns;
        private Integer runningRuns;
        private Integer totalExceptions;
        private Integer resolvedExceptions;
        private Integer unresolvedExceptions;
        private Double averageProcessingTime;
        private Double successRate;
    }
}
