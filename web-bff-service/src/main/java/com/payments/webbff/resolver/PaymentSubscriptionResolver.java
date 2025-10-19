package com.payments.webbff.resolver;

import com.payments.webbff.dto.PaymentDto;
import com.payments.webbff.service.PaymentSubscriptionService;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * GraphQL Subscription Resolver for Payment operations.
 *
 * <p>This resolver handles all payment-related subscriptions in the GraphQL API,
 * providing real-time updates for payment status changes and new payments.
 *
 * @since PE-414
 */
@Component
public class PaymentSubscriptionResolver implements GraphQLSubscriptionResolver {

    @Autowired
    private PaymentSubscriptionService paymentSubscriptionService;

    /**
     * Subscribes to payment status changes for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return publisher of payment status changes
     */
    public Publisher<PaymentDto> paymentStatusChanged(UUID tenantId) {
        return paymentSubscriptionService.paymentStatusChanged(tenantId);
    }

    /**
     * Subscribes to new payment creation for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return publisher of new payments
     */
    public Publisher<PaymentDto> paymentCreated(UUID tenantId) {
        return paymentSubscriptionService.paymentCreated(tenantId);
    }
}
