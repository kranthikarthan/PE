package com.payments.batch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobExecution;

import java.util.List;

/**
 * Response DTO for batch job history.
 *
 * <p>This DTO encapsulates the historical information for batch job executions,
 * including pagination details and execution summaries.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobHistoryResponse {
  
  /** The list of job executions */
  private List<JobExecution> jobExecutions;
  
  /** The total count of job executions */
  private Long totalCount;
  
  /** The limit for pagination */
  private Integer limit;
  
  /** The offset for pagination */
  private Integer offset;
  
  /** The current page number */
  private Integer currentPage;
  
  /** The total number of pages */
  private Integer totalPages;
  
  /** Whether there are more results */
  private Boolean hasMore;
  
  /** The response message */
  private String message;
  
  /** The error message (if any) */
  private String errorMessage;
  
  /** Additional metadata */
  private java.util.Map<String, Object> metadata;
}
