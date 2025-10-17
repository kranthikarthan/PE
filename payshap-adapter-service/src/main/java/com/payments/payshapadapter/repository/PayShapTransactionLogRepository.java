package com.payments.payshapadapter.repository;

import com.payments.payshapadapter.domain.PayShapTransactionLog;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for PayShap Transaction Log entities */
@Repository
public interface PayShapTransactionLogRepository
    extends JpaRepository<PayShapTransactionLog, String> {

  /** Find logs by adapter ID */
  List<PayShapTransactionLog> findByAdapterId(String adapterId);

  /** Find logs by transaction ID */
  List<PayShapTransactionLog> findByTransactionId(String transactionId);

  /** Find logs by log level */
  List<PayShapTransactionLog> findByLogLevel(String logLevel);

  /** Find logs by adapter ID and log level */
  List<PayShapTransactionLog> findByAdapterIdAndLogLevel(String adapterId, String logLevel);

  /** Find logs by date range */
  @Query("SELECT l FROM PayShapTransactionLog l WHERE l.occurredAt BETWEEN :startDate AND :endDate")
  List<PayShapTransactionLog> findByDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /** Find logs by adapter ID and date range */
  @Query(
      "SELECT l FROM PayShapTransactionLog l WHERE l.adapterId = :adapterId AND l.occurredAt BETWEEN :startDate AND :endDate")
  List<PayShapTransactionLog> findByAdapterIdAndDateRange(
      @Param("adapterId") String adapterId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);
}
