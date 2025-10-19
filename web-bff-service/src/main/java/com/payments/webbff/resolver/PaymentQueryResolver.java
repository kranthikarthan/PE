package com.payments.webbff.resolver;

import com.payments.webbff.dto.PaymentDto;
import com.payments.webbff.service.PaymentService;
import com.payments.webbff.type.PaymentStatus;
import com.payments.webbff.type.PaymentType;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL Query Resolver for Payment operations.
 *
 * <p>This resolver handles all payment-related queries in the GraphQL API,
 * providing access to payment data with filtering, pagination, and multi-tenant support.
 *
 * @since PE-414
 */
@Component
public class PaymentQueryResolver implements GraphQLQueryResolver {

    @Autowired
    private PaymentService paymentService;

    /**
     * Retrieves a list of payments with optional filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param status optional payment status filter
     * @param paymentType optional payment type filter
     * @param limit maximum number of results (default: 50)
     * @param offset offset for pagination (default: 0)
     * @return list of payments
     */
    public List<PaymentDto> payments(
            UUID tenantId,
            UUID businessUnitId,
            PaymentStatus status,
            PaymentType paymentType,
            Integer limit,
            Integer offset) {
        
        return paymentService.getPayments(
                tenantId,
                businessUnitId,
                status,
                paymentType,
                limit != null ? limit : 50,
                offset != null ? offset : 0
        );
    }

    /**
     * Retrieves a specific payment by ID.
     *
     * @param id the payment ID
     * @return the payment or null if not found
     */
    public PaymentDto payment(UUID id) {
        return paymentService.getPayment(id);
    }
}
