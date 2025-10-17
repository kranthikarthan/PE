package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingRouteId;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clearing Route Entity for BankservAfrica Adapter
 */
@Entity
@Table(name = "bankservafrica_clearing_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClearingRoute {

  @EmbeddedId private ClearingRouteId id;

  @Embedded private ClearingAdapterId clearingAdapterId;

  private String routeName;
  private String source;
  private String destination;
  private Integer priority;

  @Enumerated(EnumType.STRING)
  private RouteStatus status;

  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;

  public static ClearingRoute create(
      ClearingRouteId id,
      ClearingAdapterId clearingAdapterId,
      String routeName,
      String source,
      String destination,
      Integer priority,
      String createdBy) {
    ClearingRoute route = new ClearingRoute();
    route.id = id;
    route.clearingAdapterId = clearingAdapterId;
    route.routeName = routeName;
    route.source = source;
    route.destination = destination;
    route.priority = priority;
    route.status = RouteStatus.ACTIVE;
    route.createdAt = Instant.now();
    route.updatedAt = Instant.now();
    route.createdBy = createdBy;
    route.updatedBy = createdBy;
    return route;
  }
}

enum RouteStatus {
  ACTIVE,
  INACTIVE,
  MAINTENANCE
}
