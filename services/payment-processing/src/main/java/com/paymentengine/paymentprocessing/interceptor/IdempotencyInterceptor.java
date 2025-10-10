package com.paymentengine.paymentprocessing.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentengine.paymentprocessing.entity.IdempotencyKey;
import com.paymentengine.paymentprocessing.repository.IdempotencyKeyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Idempotency Interceptor
 * Ensures idempotent processing of requests using X-Idempotency-Key header
 * 
 * MAANG Best Practice: Prevents duplicate processing in distributed systems
 */
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyInterceptor.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private static final String TENANT_ID_HEADER = "X-Tenant-ID";

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${payment-processing.idempotency.ttl-hours:24}")
    private int ttlHours;

    @Value("${payment-processing.idempotency.enabled:true}")
    private boolean enabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        if (!enabled) {
            return true;
        }

        // Only apply to POST, PUT, PATCH methods
        String method = request.getMethod();
        if (!method.equals("POST") && !method.equals("PUT") && !method.equals("PATCH")) {
            return true;
        }

        // Skip if idempotency key not provided
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return true;
        }

        String tenantId = request.getHeader(TENANT_ID_HEADER);
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "default";
        }

        MDC.put("idempotencyKey", idempotencyKey);

        // Check if this request was already processed
        Optional<IdempotencyKey> existingKey = 
                idempotencyKeyRepository.findByIdempotencyKeyAndTenantId(idempotencyKey, tenantId);

        if (existingKey.isPresent()) {
            IdempotencyKey key = existingKey.get();
            
            // Check if expired
            if (key.getExpiresAt().isBefore(LocalDateTime.now())) {
                logger.debug("Idempotency key expired: {}", idempotencyKey);
                idempotencyKeyRepository.delete(key);
                return true;
            }

            // Return cached response
            logger.info("Duplicate request detected with idempotency key: {}", idempotencyKey);
            
            response.setStatus(key.getResponseStatus() != null ? key.getResponseStatus() : 200);
            response.setContentType("application/json");
            
            if (key.getResponseBody() != null) {
                String responseBody = objectMapper.writeValueAsString(key.getResponseBody());
                response.getWriter().write(responseBody);
            }
            
            // Add header to indicate this is a cached response
            response.setHeader("X-Idempotency-Replay", "true");
            response.setHeader("X-Original-Request-Time", key.getProcessedAt().toString());
            
            return false; // Stop further processing
        }

        // Store idempotency key in request attribute for postHandle
        request.setAttribute("idempotencyKey", idempotencyKey);
        request.setAttribute("tenantId", tenantId);

        return true;
    }

    /**
     * Generate request hash for duplicate detection
     */
    private String generateRequestHash(String method, String uri, String body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = method + ":" + uri + ":" + body;
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error generating request hash", e);
            return null;
        }
    }

    /**
     * Store processed request for future idempotency checks
     * Note: This should be called in a service layer after successful processing
     */
    public void storeProcessedRequest(String idempotencyKey, String tenantId, 
                                     String endpoint, String method,
                                     Map<String, Object> requestBody,
                                     int responseStatus, 
                                     Map<String, Object> responseBody) {
        try {
            IdempotencyKey key = new IdempotencyKey();
            key.setIdempotencyKey(idempotencyKey);
            key.setTenantId(tenantId);
            key.setEndpoint(endpoint);
            key.setHttpMethod(method);
            key.setRequestBody(requestBody);
            key.setResponseStatus(responseStatus);
            key.setResponseBody(responseBody);
            key.setProcessedAt(LocalDateTime.now());
            key.setExpiresAt(LocalDateTime.now().plusHours(ttlHours));
            
            // Generate hash for duplicate detection
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            String hash = generateRequestHash(method, endpoint, requestBodyStr);
            key.setRequestHash(hash);

            idempotencyKeyRepository.save(key);
            
            logger.debug("Stored idempotency key: {} for tenant: {}", idempotencyKey, tenantId);
        } catch (Exception e) {
            logger.error("Error storing idempotency key: {}", idempotencyKey, e);
        }
    }
}
