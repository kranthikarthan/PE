package com.payments.webbff.service;

import com.payments.webbff.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

/**
 * Service for Payment subscriptions in the Web BFF.
 *
 * <p>This service handles real-time subscriptions for payment-related events,
 * providing live updates for payment status changes and new payments.
 *
 * @since PE-414
 */
@Service
@Slf4j
public class PaymentSubscriptionService {

    @Autowired
    private ReactiveRedisTemplate<String, PaymentDto> redisTemplate;

    /**
     * Subscribes to payment status changes for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return publisher of payment status changes
     */
    public Flux<PaymentDto> paymentStatusChanged(UUID tenantId) {
        log.debug("Setting up payment status change subscription for tenant: {}", tenantId);
        
        String channel = "payment:status:changed:" + tenantId;
        
        return redisTemplate
                .listenToChannel(channel)
                .map(message -> {
                    PaymentDto payment = message.getMessage();
                    log.debug("Received payment status change for payment: {}", payment.getId());
                    return payment;
                })
                .doOnSubscribe(subscription -> 
                    log.debug("Subscribed to payment status changes for tenant: {}", tenantId))
                .doOnCancel(() -> 
                    log.debug("Unsubscribed from payment status changes for tenant: {}", tenantId))
                .doOnError(error -> 
                    log.error("Error in payment status change subscription for tenant: {}", tenantId, error));
    }

    /**
     * Subscribes to new payment creation for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return publisher of new payments
     */
    public Flux<PaymentDto> paymentCreated(UUID tenantId) {
        log.debug("Setting up payment creation subscription for tenant: {}", tenantId);
        
        String channel = "payment:created:" + tenantId;
        
        return redisTemplate
                .listenToChannel(channel)
                .map(message -> {
                    PaymentDto payment = message.getMessage();
                    log.debug("Received new payment creation for payment: {}", payment.getId());
                    return payment;
                })
                .doOnSubscribe(subscription -> 
                    log.debug("Subscribed to payment creation for tenant: {}", tenantId))
                .doOnCancel(() -> 
                    log.debug("Unsubscribed from payment creation for tenant: {}", tenantId))
                .doOnError(error -> 
                    log.error("Error in payment creation subscription for tenant: {}", tenantId, error));
    }
}
