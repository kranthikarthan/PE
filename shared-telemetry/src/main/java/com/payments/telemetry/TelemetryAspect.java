package com.payments.telemetry;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** Aspect for automatic telemetry instrumentation */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetryAspect {

  private final TracingService tracingService;
  private final MetricsService metricsService;
  private final LoggingService loggingService;

  /** Instrument service methods with tracing and metrics */
  @Around("@annotation(com.payments.telemetry.Traced)")
  public Object traceServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String spanName = className + "." + methodName;

    return tracingService.executeInSpan(
        spanName,
        () -> {
          try {
            // Add method attributes
            tracingService.addAttribute("method.name", methodName);
            tracingService.addAttribute("class.name", className);

            // Record method execution timing
            return metricsService.recordTiming(
                "service.method.duration",
                Map.of("class", className, "method", methodName),
                () -> {
                  try {
                    return joinPoint.proceed();
                  } catch (Throwable throwable) {
                    tracingService.recordException(throwable);
                    throw new RuntimeException(throwable);
                  }
                });
          } catch (Exception e) {
            metricsService.recordError("service.method", e.getMessage(), className);
            throw e;
          }
        });
  }

  /** Instrument external service calls */
  @Around("@annotation(com.payments.telemetry.ExternalService)")
  public Object traceExternalService(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String spanName = "external." + className + "." + methodName;

    return tracingService.executeExternalSpan(
        spanName,
        className,
        methodName,
        () -> {
          try {
            // Add service attributes
            tracingService.addAttribute("service.name", className);
            tracingService.addAttribute("operation", methodName);

            // Record external service call timing
            return metricsService.recordExternalServiceCall(
                className,
                methodName,
                () -> {
                  try {
                    Object result = joinPoint.proceed();
                    loggingService.logExternalServiceCallSuccess(className, methodName, 0);
                    return result;
                  } catch (Throwable throwable) {
                    loggingService.logExternalServiceCallFailure(
                        className, methodName, throwable.getMessage());
                    throw new RuntimeException(throwable);
                  }
                });
          } catch (Exception e) {
            metricsService.recordError("external.service", e.getMessage(), className);
            throw e;
          }
        });
  }

  /** Instrument database operations */
  @Around("@annotation(com.payments.telemetry.DatabaseOperation)")
  public Object traceDatabaseOperation(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String spanName = "database." + className + "." + methodName;

    return tracingService.executeInSpan(
        spanName,
        () -> {
          try {
            // Add database attributes
            tracingService.addAttribute("operation", methodName);
            tracingService.addAttribute("component", "database");

            // Record database operation timing
            return metricsService.recordDatabaseOperation(
                methodName,
                className,
                () -> {
                  try {
                    Object result = joinPoint.proceed();
                    loggingService.logDatabaseOperation(methodName, className, "unknown");
                    return result;
                  } catch (Throwable throwable) {
                    tracingService.recordException(throwable);
                    throw new RuntimeException(throwable);
                  }
                });
          } catch (Exception e) {
            metricsService.recordError("database.operation", e.getMessage(), className);
            throw e;
          }
        });
  }

  /** Instrument cache operations */
  @Around("@annotation(com.payments.telemetry.CacheOperation)")
  public Object traceCacheOperation(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String spanName = "cache." + className + "." + methodName;

    return tracingService.executeInSpan(
        spanName,
        () -> {
          try {
            // Add cache attributes
            tracingService.addAttribute("operation", methodName);
            tracingService.addAttribute("component", "cache");

            // Record cache operation timing
            return metricsService.recordCacheOperation(
                methodName,
                className,
                () -> {
                  try {
                    Object result = joinPoint.proceed();
                    loggingService.logCacheOperation(methodName, className, "unknown");
                    return result;
                  } catch (Throwable throwable) {
                    tracingService.recordException(throwable);
                    throw new RuntimeException(throwable);
                  }
                });
          } catch (Exception e) {
            metricsService.recordError("cache.operation", e.getMessage(), className);
            throw e;
          }
        });
  }

  /** Instrument Kafka operations */
  @Around("@annotation(com.payments.telemetry.KafkaOperation)")
  public Object traceKafkaOperation(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String spanName = "kafka." + className + "." + methodName;

    return tracingService.executeInSpan(
        spanName,
        () -> {
          try {
            // Add Kafka attributes
            tracingService.addAttribute("operation", methodName);
            tracingService.addAttribute("component", "kafka");

            // Record Kafka operation timing
            return metricsService.recordKafkaOperation(
                methodName,
                className,
                () -> {
                  try {
                    Object result = joinPoint.proceed();
                    loggingService.logKafkaOperation(methodName, className, "unknown");
                    return result;
                  } catch (Throwable throwable) {
                    tracingService.recordException(throwable);
                    throw new RuntimeException(throwable);
                  }
                });
          } catch (Exception e) {
            metricsService.recordError("kafka.operation", e.getMessage(), className);
            throw e;
          }
        });
  }
}
