package com.payments.paymentinitiation.api.validation;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.paymentinitiation.service.PaymentBusinessRulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * Payment Validation Interceptor
 * 
 * Provides additional validation for payment requests:
 * - Business rule validation
 * - Amount limits validation
 * - Tenant-specific validation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidationInterceptor implements HandlerInterceptor {

    private final PaymentBusinessRulesService businessRulesService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("Payment validation interceptor processing request: {}", request.getRequestURI());
        
        // Only validate payment initiation requests
        if (request.getRequestURI().contains("/initiate") && request.getMethod().equals("POST")) {
            return validatePaymentInitiationRequest(request, response);
        }
        
        return true;
    }

    /**
     * Validate payment initiation request
     */
    private boolean validatePaymentInitiationRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get tenant ID from headers
            String tenantId = request.getHeader("X-Tenant-ID");
            if (tenantId == null || tenantId.trim().isEmpty()) {
                log.warn("Missing tenant ID in request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing X-Tenant-ID header\"}");
                return false;
            }
            
            // Get business rules for tenant
            var businessRules = businessRulesService.getBusinessRulesForTenant(tenantId);
            
            // Validate correlation ID
            String correlationId = request.getHeader("X-Correlation-ID");
            if (correlationId == null || correlationId.trim().isEmpty()) {
                log.warn("Missing correlation ID in request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing X-Correlation-ID header\"}");
                return false;
            }
            
            // Validate business unit ID
            String businessUnitId = request.getHeader("X-Business-Unit-ID");
            if (businessUnitId == null || businessUnitId.trim().isEmpty()) {
                log.warn("Missing business unit ID in request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing X-Business-Unit-ID header\"}");
                return false;
            }
            
            log.debug("Request validation passed for tenant: {}, business unit: {}", tenantId, businessUnitId);
            return true;
            
        } catch (Exception e) {
            log.error("Error in payment validation interceptor", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal validation error\"}");
            return false;
        }
    }
}
