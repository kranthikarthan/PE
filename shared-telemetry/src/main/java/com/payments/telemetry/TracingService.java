package com.payments.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for managing distributed tracing */
@Service
@RequiredArgsConstructor
@Slf4j
public class TracingService {

  private final Tracer tracer;
  private final OpenTelemetry openTelemetry;

  /** Create a new span */
  public Span createSpan(String spanName) {
    return tracer.spanBuilder(spanName).setSpanKind(SpanKind.INTERNAL).startSpan();
  }

  /** Create a new span with parent context */
  public Span createSpan(String spanName, Context parentContext) {
    return tracer
        .spanBuilder(spanName)
        .setParent(parentContext)
        .setSpanKind(SpanKind.INTERNAL)
        .startSpan();
  }

  /** Create a new span with attributes */
  public Span createSpan(String spanName, Map<String, String> attributes) {
    SpanBuilder spanBuilder = tracer.spanBuilder(spanName).setSpanKind(SpanKind.INTERNAL);

    attributes.forEach(spanBuilder::setAttribute);

    return spanBuilder.startSpan();
  }

  /** Create a new span for external service calls */
  public Span createExternalSpan(String spanName, String serviceName, String operation) {
    return tracer
        .spanBuilder(spanName)
        .setSpanKind(SpanKind.CLIENT)
        .setAttribute("service.name", serviceName)
        .setAttribute("service.operation", operation)
        .startSpan();
  }

  /** Create a new span for incoming requests */
  public Span createIncomingSpan(String spanName, String httpMethod, String httpUrl) {
    return tracer
        .spanBuilder(spanName)
        .setSpanKind(SpanKind.SERVER)
        .setAttribute("http.method", httpMethod)
        .setAttribute("http.url", httpUrl)
        .startSpan();
  }

  /** Execute code within a span */
  public <T> T executeInSpan(String spanName, Supplier<T> operation) {
    Span span = createSpan(spanName);
    try (Scope scope = span.makeCurrent()) {
      return operation.get();
    } catch (Exception e) {
      span.recordException(e);
      span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
      throw e;
    } finally {
      span.end();
    }
  }

  /** Execute code within a span with attributes */
  public <T> T executeInSpan(
      String spanName, Map<String, String> attributes, Supplier<T> operation) {
    Span span = createSpan(spanName, attributes);
    try (Scope scope = span.makeCurrent()) {
      return operation.get();
    } catch (Exception e) {
      span.recordException(e);
      span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
      throw e;
    } finally {
      span.end();
    }
  }

  /** Execute code within a span for external service calls */
  public <T> T executeExternalSpan(
      String spanName, String serviceName, String operation, Supplier<T> operationCode) {
    Span span = createExternalSpan(spanName, serviceName, operation);
    try (Scope scope = span.makeCurrent()) {
      return operationCode.get();
    } catch (Exception e) {
      span.recordException(e);
      span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
      throw e;
    } finally {
      span.end();
    }
  }

  /** Add attributes to current span */
  public void addAttributes(Map<String, String> attributes) {
    Span currentSpan = Span.current();
    if (currentSpan != null && currentSpan.isRecording()) {
      attributes.forEach(currentSpan::setAttribute);
    }
  }

  /** Add attribute to current span */
  public void addAttribute(String key, String value) {
    Span currentSpan = Span.current();
    if (currentSpan != null && currentSpan.isRecording()) {
      currentSpan.setAttribute(key, value);
    }
  }

  /** Add event to current span */
  public void addEvent(String eventName) {
    Span currentSpan = Span.current();
    if (currentSpan != null && currentSpan.isRecording()) {
      currentSpan.addEvent(eventName);
    }
  }

  /** Add event with attributes to current span */
  public void addEvent(String eventName, Map<String, String> attributes) {
    Span currentSpan = Span.current();
    if (currentSpan != null && currentSpan.isRecording()) {
      AttributesBuilder builder = Attributes.builder();
      attributes.forEach(builder::put);
      currentSpan.addEvent(eventName, builder.build());
    }
  }

  /** Record exception in current span */
  public void recordException(Throwable throwable) {
    Span currentSpan = Span.current();
    if (currentSpan != null && currentSpan.isRecording()) {
      currentSpan.recordException(throwable);
      currentSpan.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, throwable.getMessage());
    }
  }

  /** Set status of current span */
  public void setStatus(io.opentelemetry.api.trace.StatusCode statusCode, String description) {
    Span currentSpan = Span.current();
    if (currentSpan != null && currentSpan.isRecording()) {
      currentSpan.setStatus(statusCode, description);
    }
  }

  /** Get current span */
  public Span getCurrentSpan() {
    return Span.current();
  }

  /** Get current context */
  public Context getCurrentContext() {
    return Context.current();
  }

  /** Create a new context with span */
  public Context createContext(Span span) {
    return Context.current().with(span);
  }

  /** Run code with specific context */
  public <T> T runWithContext(Context context, Supplier<T> operation) {
    try (Scope scope = context.makeCurrent()) {
      return operation.get();
    }
  }
}
