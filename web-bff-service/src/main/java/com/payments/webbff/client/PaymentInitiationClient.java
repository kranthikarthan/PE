package com.payments.webbff.client;

import com.payments.webbff.dto.CreatePaymentInput;
import com.payments.webbff.dto.PaymentDto;
import com.payments.webbff.dto.UpdatePaymentInput;
import com.payments.webbff.type.PaymentStatus;
import com.payments.webbff.type.PaymentType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * OpenFeign client for Payment Initiation Service.
 *
 * <p>This client provides integration with the Payment Initiation Service
 * for all payment-related operations in the Web BFF.
 *
 * @since PE-414
 */
@FeignClient(
    name = "payment-initiation-service",
    url = "${services.payment-initiation.url:http://localhost:8081}",
    configuration = PaymentInitiationClientConfig.class
)
public interface PaymentInitiationClient {

    /**
     * Retrieves a list of payments with optional filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param status optional payment status filter
     * @param paymentType optional payment type filter
     * @param limit maximum number of results
     * @param offset offset for pagination
     * @return list of payments
     */
    @GetMapping("/api/v1/payments")
    List<PaymentDto> getPayments(
            @RequestParam("tenantId") UUID tenantId,
            @RequestParam(value = "businessUnitId", required = false) UUID businessUnitId,
            @RequestParam(value = "status", required = false) PaymentStatus status,
            @RequestParam(value = "paymentType", required = false) PaymentType paymentType,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset
    );

    /**
     * Retrieves a specific payment by ID.
     *
     * @param id the payment ID
     * @return the payment
     */
    @GetMapping("/api/v1/payments/{id}")
    PaymentDto getPayment(@PathVariable("id") UUID id);

    /**
     * Creates a new payment.
     *
     * @param input the payment creation input
     * @return the created payment
     */
    @PostMapping("/api/v1/payments")
    PaymentDto createPayment(@RequestBody CreatePaymentInput input);

    /**
     * Updates an existing payment.
     *
     * @param id the payment ID
     * @param input the payment update input
     * @return the updated payment
     */
    @PutMapping("/api/v1/payments/{id}")
    PaymentDto updatePayment(@PathVariable("id") UUID id, @RequestBody UpdatePaymentInput input);

    /**
     * Cancels a payment.
     *
     * @param id the payment ID
     * @return the cancelled payment
     */
    @PostMapping("/api/v1/payments/{id}/cancel")
    PaymentDto cancelPayment(@PathVariable("id") UUID id);
}
