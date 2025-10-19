package com.payments.domain.shared;

import lombok.Value;

@Value(staticConstructor = "of")
public class PreferenceId {
  String value;

  public PreferenceId(String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("PreferenceId value cannot be null or empty");
    }
    this.value = value;
  }
}
