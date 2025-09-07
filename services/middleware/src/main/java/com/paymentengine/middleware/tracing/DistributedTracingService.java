package com.paymentengine.middleware.tracing;

import java.util.Map;
import java.util.UUID;

public interface DistributedTracingService {
    
    // Trace Management
    String startTrace(String operationName);
    
    String startTrace(String operationName, String parentTraceId);
    
    void endTrace(String traceId);
    
    void endTrace(String traceId, boolean success);
    
    // Span Management
    String startSpan(String traceId, String spanName);
    
    String startSpan(String traceId, String spanName, String parentSpanId);
    
    void endSpan(String spanId);
    
    void endSpan(String spanId, boolean success);
    
    // Span Attributes
    void addSpanAttribute(String spanId, String key, String value);
    
    void addSpanAttribute(String spanId, String key, Number value);
    
    void addSpanAttribute(String spanId, String key, Boolean value);
    
    void addSpanAttributes(String spanId, Map<String, Object> attributes);
    
    // Span Events
    void addSpanEvent(String spanId, String eventName);
    
    void addSpanEvent(String spanId, String eventName, Map<String, Object> attributes);
    
    // Span Status
    void setSpanStatus(String spanId, String status);
    
    void setSpanError(String spanId, Throwable error);
    
    // Trace Context
    String getCurrentTraceId();
    
    String getCurrentSpanId();
    
    Map<String, String> getTraceContext();
    
    void setTraceContext(Map<String, String> context);
    
    // Trace Propagation
    void injectTraceContext(Map<String, String> headers);
    
    Map<String, String> extractTraceContext(Map<String, String> headers);
    
    // Trace Sampling
    boolean shouldSample(String operationName);
    
    void setSamplingRate(double samplingRate);
    
    // Trace Export
    void exportTrace(String traceId);
    
    void exportAllTraces();
    
    // Trace Query
    Map<String, Object> getTrace(String traceId);
    
    Map<String, Object> getSpan(String spanId);
    
    // Trace Analytics
    Map<String, Object> getTraceAnalytics();
    
    Map<String, Object> getSpanAnalytics();
}