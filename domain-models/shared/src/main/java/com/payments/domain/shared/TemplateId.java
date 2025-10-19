package com.payments.domain.shared;

import lombok.Value;

@Value(staticConstructor = "of")
public class TemplateId {
  String value;

  public TemplateId(String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("TemplateId value cannot be null or empty");
    }
    this.value = value;
  }
}
