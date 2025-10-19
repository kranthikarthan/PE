package com.payments.webbff.resolver;

import com.payments.webbff.dto.CreatePaymentInput;
import com.payments.webbff.dto.PaymentDto;
import com.payments.webbff.dto.UpdatePaymentInput;
import com.payments.webbff.service.PaymentService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * GraphQL Mutation Resolver for Payment operations.
 *
 * <p>This resolver handles all payment-related mutations in the GraphQL API,
 * providing create, update, and cancel operations for payments.
 *
 * @since PE-414
 */
@Component
public class PaymentMutationResolver implements GraphQLMutationResolver {

    @Autowired
    private PaymentService paymentService;

    /**
     * Creates a new payment.
     *
     * @param input the payment creation input
     * @return the created payment
     */
    public PaymentDto createPayment(CreatePaymentInput input) {
        return paymentService.createPayment(input);
    }

    /**
     * Updates an existing payment.
     *
     * @param id the payment ID
     * @param input the payment update input
     * @return the updated payment
     */
    public PaymentDto updatePayment(UUID id, UpdatePaymentInput input) {
        return paymentService.updatePayment(id, input);
    }

    /**
     * Cancels a payment.
     *
     * @param id the payment ID
     * @return the cancelled payment
     */
    public PaymentDto cancelPayment(UUID id) {
        return paymentService.cancelPayment(id);
    }
}
