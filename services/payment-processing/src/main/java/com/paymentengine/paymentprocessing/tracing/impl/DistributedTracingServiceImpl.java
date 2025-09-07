package com.paymentengine.paymentprocessing.tracing.impl;

import com.paymentengine.paymentprocessing.tracing.DistributedTracingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistributedTracingServiceImpl implements DistributedTracingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedTracingServiceImpl.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Map<String, TraceContext> traces = new ConcurrentHashMap<>();
    private final Map<String, SpanContext> spans = new ConcurrentHashMap<>();
    private final ThreadLocal<String> currentTraceId = new ThreadLocal<>();
    private final ThreadLocal<String> currentSpanId = new ThreadLocal<>();
    private double samplingRate = 1.0;
    
    @Override
    public String startTrace(String operationName) {
        return startTrace(operationName, null);
    }
    
    @Override
    public String startTrace(String operationName, String parentTraceId) {
        String traceId = UUID.randomUUID().toString();
        
        TraceContext trace = new TraceContext();
        trace.setTraceId(traceId);
        trace.setOperationName(operationName);
        trace.setParentTraceId(parentTraceId);
        trace.setStartTime(LocalDateTime.now());
        trace.setStatus("ACTIVE");
        
        traces.put(traceId, trace);
        currentTraceId.set(traceId);
        
        logger.debug("Started trace: {} - {}", traceId, operationName);
        return traceId;
    }
    
    @Override
    public void endTrace(String traceId) {
        endTrace(traceId, true);
    }
    
    @Override
    public void endTrace(String traceId, boolean success) {
        TraceContext trace = traces.get(traceId);
        if (trace != null) {
            trace.setEndTime(LocalDateTime.now());
            trace.setStatus(success ? "SUCCESS" : "ERROR");
            trace.setSuccess(success);
            
            // Export trace
            exportTrace(traceId);
            
            logger.debug("Ended trace: {} - {}", traceId, trace.getOperationName());
        }
    }
    
    @Override
    public String startSpan(String traceId, String spanName) {
        return startSpan(traceId, spanName, null);
    }
    
    @Override
    public String startSpan(String traceId, String spanName, String parentSpanId) {
        String spanId = UUID.randomUUID().toString();
        
        SpanContext span = new SpanContext();
        span.setSpanId(spanId);
        span.setTraceId(traceId);
        span.setSpanName(spanName);
        span.setParentSpanId(parentSpanId);
        span.setStartTime(LocalDateTime.now());
        span.setStatus("ACTIVE");
        
        spans.put(spanId, span);
        currentSpanId.set(spanId);
        
        // Add span to trace
        TraceContext trace = traces.get(traceId);
        if (trace != null) {
            trace.addSpan(spanId);
        }
        
        logger.debug("Started span: {} - {}", spanId, spanName);
        return spanId;
    }
    
    @Override
    public void endSpan(String spanId) {
        endSpan(spanId, true);
    }
    
    @Override
    public void endSpan(String spanId, boolean success) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.setEndTime(LocalDateTime.now());
            span.setStatus(success ? "SUCCESS" : "ERROR");
            span.setSuccess(success);
            
            logger.debug("Ended span: {} - {}", spanId, span.getSpanName());
        }
    }
    
    @Override
    public void addSpanAttribute(String spanId, String key, String value) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.addAttribute(key, value);
        }
    }
    
    @Override
    public void addSpanAttribute(String spanId, String key, Number value) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.addAttribute(key, value);
        }
    }
    
    @Override
    public void addSpanAttribute(String spanId, String key, Boolean value) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.addAttribute(key, value);
        }
    }
    
    @Override
    public void addSpanAttributes(String spanId, Map<String, Object> attributes) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.addAttributes(attributes);
        }
    }
    
    @Override
    public void addSpanEvent(String spanId, String eventName) {
        addSpanEvent(spanId, eventName, new HashMap<>());
    }
    
    @Override
    public void addSpanEvent(String spanId, String eventName, Map<String, Object> attributes) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            SpanEvent event = new SpanEvent();
            event.setEventName(eventName);
            event.setTimestamp(LocalDateTime.now());
            event.setAttributes(attributes);
            span.addEvent(event);
        }
    }
    
    @Override
    public void setSpanStatus(String spanId, String status) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.setStatus(status);
        }
    }
    
    @Override
    public void setSpanError(String spanId, Throwable error) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            span.setError(error);
            span.setStatus("ERROR");
        }
    }
    
    @Override
    public String getCurrentTraceId() {
        return currentTraceId.get();
    }
    
    @Override
    public String getCurrentSpanId() {
        return currentSpanId.get();
    }
    
    @Override
    public Map<String, String> getTraceContext() {
        Map<String, String> context = new HashMap<>();
        context.put("traceId", getCurrentTraceId());
        context.put("spanId", getCurrentSpanId());
        return context;
    }
    
    @Override
    public void setTraceContext(Map<String, String> context) {
        currentTraceId.set(context.get("traceId"));
        currentSpanId.set(context.get("spanId"));
    }
    
    @Override
    public void injectTraceContext(Map<String, String> headers) {
        String traceId = getCurrentTraceId();
        String spanId = getCurrentSpanId();
        
        if (traceId != null) {
            headers.put("X-Trace-Id", traceId);
        }
        if (spanId != null) {
            headers.put("X-Span-Id", spanId);
        }
    }
    
    @Override
    public Map<String, String> extractTraceContext(Map<String, String> headers) {
        Map<String, String> context = new HashMap<>();
        context.put("traceId", headers.get("X-Trace-Id"));
        context.put("spanId", headers.get("X-Span-Id"));
        return context;
    }
    
    @Override
    public boolean shouldSample(String operationName) {
        return Math.random() < samplingRate;
    }
    
    @Override
    public void setSamplingRate(double samplingRate) {
        this.samplingRate = Math.max(0.0, Math.min(1.0, samplingRate));
    }
    
    @Override
    public void exportTrace(String traceId) {
        TraceContext trace = traces.get(traceId);
        if (trace != null) {
            Map<String, Object> traceData = new HashMap<>();
            traceData.put("traceId", trace.getTraceId());
            traceData.put("operationName", trace.getOperationName());
            traceData.put("parentTraceId", trace.getParentTraceId());
            traceData.put("startTime", trace.getStartTime());
            traceData.put("endTime", trace.getEndTime());
            traceData.put("status", trace.getStatus());
            traceData.put("success", trace.isSuccess());
            traceData.put("spans", trace.getSpans());
            
            kafkaTemplate.send("trace-events", traceData);
        }
    }
    
    @Override
    public void exportAllTraces() {
        for (String traceId : traces.keySet()) {
            exportTrace(traceId);
        }
    }
    
    @Override
    public Map<String, Object> getTrace(String traceId) {
        TraceContext trace = traces.get(traceId);
        if (trace != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("traceId", trace.getTraceId());
            result.put("operationName", trace.getOperationName());
            result.put("parentTraceId", trace.getParentTraceId());
            result.put("startTime", trace.getStartTime());
            result.put("endTime", trace.getEndTime());
            result.put("status", trace.getStatus());
            result.put("success", trace.isSuccess());
            result.put("spans", trace.getSpans());
            return result;
        }
        return null;
    }
    
    @Override
    public Map<String, Object> getSpan(String spanId) {
        SpanContext span = spans.get(spanId);
        if (span != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("spanId", span.getSpanId());
            result.put("traceId", span.getTraceId());
            result.put("spanName", span.getSpanName());
            result.put("parentSpanId", span.getParentSpanId());
            result.put("startTime", span.getStartTime());
            result.put("endTime", span.getEndTime());
            result.put("status", span.getStatus());
            result.put("success", span.isSuccess());
            result.put("attributes", span.getAttributes());
            result.put("events", span.getEvents());
            result.put("error", span.getError());
            return result;
        }
        return null;
    }
    
    @Override
    public Map<String, Object> getTraceAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalTraces", traces.size());
        analytics.put("activeTraces", traces.values().stream().mapToInt(t -> "ACTIVE".equals(t.getStatus()) ? 1 : 0).sum());
        analytics.put("successfulTraces", traces.values().stream().mapToInt(t -> t.isSuccess() ? 1 : 0).sum());
        analytics.put("failedTraces", traces.values().stream().mapToInt(t -> !t.isSuccess() ? 1 : 0).sum());
        return analytics;
    }
    
    @Override
    public Map<String, Object> getSpanAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalSpans", spans.size());
        analytics.put("activeSpans", spans.values().stream().mapToInt(s -> "ACTIVE".equals(s.getStatus()) ? 1 : 0).sum());
        analytics.put("successfulSpans", spans.values().stream().mapToInt(s -> s.isSuccess() ? 1 : 0).sum());
        analytics.put("failedSpans", spans.values().stream().mapToInt(s -> !s.isSuccess() ? 1 : 0).sum());
        return analytics;
    }
    
    // Inner classes for trace and span context
    private static class TraceContext {
        private String traceId;
        private String operationName;
        private String parentTraceId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private boolean success;
        private List<String> spans = new ArrayList<>();
        
        // Getters and setters
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getOperationName() { return operationName; }
        public void setOperationName(String operationName) { this.operationName = operationName; }
        public String getParentTraceId() { return parentTraceId; }
        public void setParentTraceId(String parentTraceId) { this.parentTraceId = parentTraceId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<String> getSpans() { return spans; }
        public void setSpans(List<String> spans) { this.spans = spans; }
        public void addSpan(String spanId) { this.spans.add(spanId); }
    }
    
    private static class SpanContext {
        private String spanId;
        private String traceId;
        private String spanName;
        private String parentSpanId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private boolean success;
        private Map<String, Object> attributes = new HashMap<>();
        private List<SpanEvent> events = new ArrayList<>();
        private Throwable error;
        
        // Getters and setters
        public String getSpanId() { return spanId; }
        public void setSpanId(String spanId) { this.spanId = spanId; }
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getSpanName() { return spanName; }
        public void setSpanName(String spanName) { this.spanName = spanName; }
        public String getParentSpanId() { return parentSpanId; }
        public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
        public List<SpanEvent> getEvents() { return events; }
        public void setEvents(List<SpanEvent> events) { this.events = events; }
        public Throwable getError() { return error; }
        public void setError(Throwable error) { this.error = error; }
        
        public void addAttribute(String key, Object value) { this.attributes.put(key, value); }
        public void addAttributes(Map<String, Object> attributes) { this.attributes.putAll(attributes); }
        public void addEvent(SpanEvent event) { this.events.add(event); }
    }
    
    private static class SpanEvent {
        private String eventName;
        private LocalDateTime timestamp;
        private Map<String, Object> attributes = new HashMap<>();
        
        // Getters and setters
        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    }
}