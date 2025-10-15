package com.payments.accountadapter.client;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

/**
 * Account Service Retryer
 * 
 * Custom retryer for account service Feign client:
 * - Retry configuration
 * - Retry logic
 * - Error handling
 * - Logging
 */
@Slf4j
public class AccountServiceRetryer implements Retryer {

    private final int maxAttempts;
    private final long period;
    private final long maxPeriod;
    private int attempt = 1;

    public AccountServiceRetryer() {
        this(3, 1000L, 5000L);
    }

    public AccountServiceRetryer(int maxAttempts, long period, long maxPeriod) {
        this.maxAttempts = maxAttempts;
        this.period = period;
        this.maxPeriod = maxPeriod;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            log.error("Max retry attempts ({}) reached for account service call", maxAttempts);
            throw e;
        }

        long sleepTime = Math.min(period * attempt, maxPeriod);
        log.warn("Retrying account service call (attempt {}/{}) after {}ms delay", 
                attempt, maxAttempts, sleepTime);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return new AccountServiceRetryer(maxAttempts, period, maxPeriod);
    }
}
