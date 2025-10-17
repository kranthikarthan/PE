package com.payments.rtcadapter.repository;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcTransactionLog;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for RTC Transaction Log entities */
@Repository
public interface RtcTransactionLogRepository
    extends JpaRepository<RtcTransactionLog, ClearingMessageId> {

  /** Find logs by adapter ID */
  @Query("SELECT rtl FROM RtcTransactionLog rtl WHERE rtl.rtcAdapterId = :adapterId")
  List<RtcTransactionLog> findByAdapterId(@Param("adapterId") String adapterId);

  /** Find logs by transaction ID */
  List<RtcTransactionLog> findByTransactionId(String transactionId);

  /** Find logs by operation */
  List<RtcTransactionLog> findByOperation(String operation);

  /** Find logs by status */
  List<RtcTransactionLog> findByStatus(String status);

  /** Find logs by occurred time range */
  List<RtcTransactionLog> findByOccurredAtBetween(Instant startTime, Instant endTime);

  /** Find logs by adapter and status */
  @Query(
      "SELECT rtl FROM RtcTransactionLog rtl WHERE rtl.rtcAdapterId = :adapterId AND rtl.status = :status")
  List<RtcTransactionLog> findByAdapterIdAndStatus(
      @Param("adapterId") String adapterId, @Param("status") String status);

  /** Count logs by status */
  long countByStatus(String status);

  /** Count logs by adapter */
  @Query("SELECT COUNT(rtl) FROM RtcTransactionLog rtl WHERE rtl.rtcAdapterId = :adapterId")
  long countByAdapterId(@Param("adapterId") String adapterId);
}
