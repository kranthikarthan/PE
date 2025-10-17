package com.payments.rtcadapter.repository;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcPaymentMessage;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for RTC Payment Message entities */
@Repository
public interface RtcPaymentMessageRepository
    extends JpaRepository<RtcPaymentMessage, ClearingMessageId> {

  /** Find messages by adapter ID */
  @Query("SELECT rpm FROM RtcPaymentMessage rpm WHERE rpm.rtcAdapterId = :adapterId")
  List<RtcPaymentMessage> findByAdapterId(@Param("adapterId") String adapterId);

  /** Find messages by transaction ID */
  RtcPaymentMessage findByTransactionId(String transactionId);

  /** Find messages by status */
  List<RtcPaymentMessage> findByStatus(String status);

  /** Find messages by direction */
  List<RtcPaymentMessage> findByDirection(String direction);

  /** Find messages by message type */
  List<RtcPaymentMessage> findByMessageType(String messageType);

  /** Find messages created after timestamp */
  List<RtcPaymentMessage> findByCreatedAtAfter(Instant timestamp);

  /** Find messages by status and adapter */
  @Query(
      "SELECT rpm FROM RtcPaymentMessage rpm WHERE rpm.rtcAdapterId = :adapterId AND rpm.status = :status")
  List<RtcPaymentMessage> findByAdapterIdAndStatus(
      @Param("adapterId") String adapterId, @Param("status") String status);

  /** Count messages by status */
  long countByStatus(String status);

  /** Count messages by adapter */
  @Query("SELECT COUNT(rpm) FROM RtcPaymentMessage rpm WHERE rpm.rtcAdapterId = :adapterId")
  long countByAdapterId(@Param("adapterId") String adapterId);
}
