package com.payments.batch.error;

/**
 * Enumeration of error categories for batch processing operations.
 *
 * <p>This enum defines the categories used to group related errors in batch
 * processing operations, enabling better error handling, monitoring, and
 * troubleshooting based on the source and nature of errors.
 *
 * @since PE-404
 */
public enum ErrorCategory {
  
  /**
   * System-level errors related to infrastructure and resources.
   * 
   * <p>These errors indicate problems with the underlying system infrastructure,
   * such as database connections, file system access, network connectivity,
   * or resource availability.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>Database connection failures
   *   <li>File system access denied
   *   <li>Network timeout errors
   *   <li>Memory or disk space issues
   * </ul>
   */
  SYSTEM("System", "Infrastructure and resource-related errors"),
  
  /**
   * Data validation errors related to input data quality and format.
   * 
   * <p>These errors indicate problems with the input data being processed,
   * such as invalid formats, missing required fields, or data that doesn't
   * meet business rules or constraints.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>Invalid file format
   *   <li>Missing required fields
   *   <li>Data type mismatches
   *   <li>Business rule violations
   * </ul>
   */
  VALIDATION("Validation", "Data quality and format validation errors"),
  
  /**
   * Business logic errors related to processing rules and workflows.
   * 
   * <p>These errors indicate problems with the business logic or processing
   * rules, such as calculation errors, workflow violations, or business
   * rule enforcement failures.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>Calculation errors
   *   <li>Workflow state violations
   *   <li>Business rule enforcement failures
   *   <li>Processing logic errors
   * </ul>
   */
  BUSINESS("Business", "Business logic and processing rule errors"),
  
  /**
   * Security and authorization errors related to access control.
   * 
   * <p>These errors indicate problems with security, authentication, or
   * authorization, such as access denied, authentication failures, or
   * security policy violations.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>Authentication failures
   *   <li>Access denied errors
   *   <li>Security policy violations
   *   <li>Permission denied errors
   * </ul>
   */
  SECURITY("Security", "Security and authorization related errors"),
  
  /**
   * Integration errors related to external system communication.
   * 
   * <p>These errors indicate problems with external system integration,
   * such as API failures, service unavailability, or communication
   * protocol issues.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>External API failures
   *   <li>Service unavailability
   *   <li>Communication protocol errors
   *   <li>Integration timeout errors
   * </ul>
   */
  INTEGRATION("Integration", "External system integration errors"),
  
  /**
   * Configuration errors related to system configuration and settings.
   * 
   * <p>These errors indicate problems with system configuration, such as
   * invalid settings, missing configuration, or configuration conflicts.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>Invalid configuration values
   *   <li>Missing required configuration
   *   <li>Configuration conflicts
   *   <li>Environment setup issues
   * </ul>
   */
  CONFIGURATION("Configuration", "System configuration and settings errors"),
  
  /**
   * Performance errors related to system performance and resource usage.
   * 
   * <p>These errors indicate problems with system performance, such as
   * timeouts, resource exhaustion, or performance degradation.
   * 
   * <p><b>Examples:</b>
   * <ul>
   *   <li>Operation timeouts
   *   <li>Resource exhaustion
   *   <li>Performance degradation
   *   <li>Memory or CPU issues
   * </ul>
   */
  PERFORMANCE("Performance", "System performance and resource usage errors"),
  
  /**
   * Unknown or unclassified errors.
   * 
   * <p>These errors don't fit into any specific category or are not yet
   * classified. They require investigation to determine the appropriate
   * category and handling strategy.
   */
  UNKNOWN("Unknown", "Unclassified or unknown error types");
  
  private final String displayName;
  private final String description;
  
  ErrorCategory(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }
  
  /**
   * Gets the display name of the category.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }
  
  /**
   * Gets the description of the category.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Gets the category from its display name.
   *
   * @param displayName the display name
   * @return the category, or null if not found
   */
  public static ErrorCategory fromDisplayName(String displayName) {
    for (ErrorCategory category : values()) {
      if (category.displayName.equalsIgnoreCase(displayName)) {
        return category;
      }
    }
    return null;
  }
  
  /**
   * Checks if this category is related to infrastructure issues.
   *
   * @return true if infrastructure-related, false otherwise
   */
  public boolean isInfrastructureRelated() {
    return this == SYSTEM || this == PERFORMANCE || this == CONFIGURATION;
  }
  
  /**
   * Checks if this category is related to data issues.
   *
   * @return true if data-related, false otherwise
   */
  public boolean isDataRelated() {
    return this == VALIDATION || this == BUSINESS;
  }
  
  /**
   * Checks if this category is related to external dependencies.
   *
   * @return true if external dependency-related, false otherwise
   */
  public boolean isExternalDependencyRelated() {
    return this == INTEGRATION || this == SECURITY;
  }
  
  /**
   * Gets the recommended handling strategy for this category.
   *
   * @return recommended handling strategy
   */
  public String getRecommendedHandling() {
    switch (this) {
      case SYSTEM:
        return "Check system resources and connectivity";
      case VALIDATION:
        return "Review and correct input data";
      case BUSINESS:
        return "Review business rules and processing logic";
      case SECURITY:
        return "Check authentication and authorization settings";
      case INTEGRATION:
        return "Verify external system availability and configuration";
      case CONFIGURATION:
        return "Review and update system configuration";
      case PERFORMANCE:
        return "Monitor system resources and optimize performance";
      case UNKNOWN:
        return "Investigate and classify the error";
      default:
        return "Review error details and context";
    }
  }
  
  @Override
  public String toString() {
    return displayName;
  }
}
