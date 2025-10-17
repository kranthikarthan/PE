package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Clearing Message Log Entity for RTC Adapter */
@Entity
@Table(name = "rtc_clearing_message_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClearingMessageLog {

  @EmbeddedId private ClearingMessageId id;

  @Embedded private ClearingAdapterId clearingAdapterId;

  private String direction; // INBOUND / OUTBOUND
  private String messageType; // e.g. pacs.008, pain.001
  private String payloadHash;
  private Integer statusCode;
  private Instant createdAt;

  public static ClearingMessageLog create(
      ClearingMessageId id,
      ClearingAdapterId clearingAdapterId,
      String direction,
      String messageType,
      String payloadHash,
      Integer statusCode) {
    ClearingMessageLog log = new ClearingMessageLog();
    log.id = id;
    log.clearingAdapterId = clearingAdapterId;
    log.direction = direction;
    log.messageType = messageType;
    log.payloadHash = payloadHash;
    log.statusCode = statusCode;
    log.createdAt = Instant.now();
    return log;
  }
}
