package com.payments.domain.shared;

import lombok.Value;

@Value(staticConstructor = "of")
public class NotificationId {
    String value;

    public NotificationId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("NotificationId value cannot be null or empty");
        }
        this.value = value;
    }
}
