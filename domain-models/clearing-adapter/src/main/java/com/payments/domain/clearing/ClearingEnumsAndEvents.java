package com.payments.domain.clearing;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

@Value
@AllArgsConstructor
class ClearingAdapterCreatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String adapterName;
  ClearingNetwork network;
  Instant createdAt;

  @Override
  public String getEventType() {
    return "ClearingAdapterCreated";
  }

  @Override
  public Instant getOccurredAt() {
    return createdAt;
  }
}

@Value
@AllArgsConstructor
class ClearingRouteAddedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  com.payments.domain.shared.ClearingRouteId routeId;
  String routeName;

  @Override
  public String getEventType() {
    return "ClearingRouteAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}

@Value
@AllArgsConstructor
class ClearingMessageLoggedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String direction;
  String messageType;
  Integer statusCode;

  @Override
  public String getEventType() {
    return "ClearingMessageLogged";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}

@Value
@AllArgsConstructor
class ClearingAdapterConfigurationUpdatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String endpoint;
  String apiVersion;

  @Override
  public String getEventType() {
    return "ClearingAdapterConfigurationUpdated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}

@Value
@AllArgsConstructor
class ClearingAdapterActivatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String activatedBy;

  @Override
  public String getEventType() {
    return "ClearingAdapterActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}

@Value
@AllArgsConstructor
class ClearingAdapterDeactivatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String reason;
  String deactivatedBy;

  @Override
  public String getEventType() {
    return "ClearingAdapterDeactivated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}

class InvalidClearingAdapterException extends RuntimeException {
  public InvalidClearingAdapterException(String message) {
    super(message);
  }
}
