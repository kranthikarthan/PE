package com.payments.domain.shared;

import java.util.Optional;

/**
 * Lightweight functional result type for domain operations.
 */
public class Result<T> {
    private final T value;
    private final String error;

    private Result(T value, String error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> ok(T value) { return new Result<>(value, null); }
    public static <T> Result<T> fail(String error) { return new Result<>(null, error); }

    public boolean isSuccess() { return error == null; }
    public Optional<T> get() { return Optional.ofNullable(value); }
    public Optional<String> getError() { return Optional.ofNullable(error); }
}


