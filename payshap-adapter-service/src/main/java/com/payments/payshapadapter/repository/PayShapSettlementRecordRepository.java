package com.payments.payshapadapter.repository;

import com.payments.payshapadapter.domain.PayShapSettlementRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for PayShap Settlement Record entities */
@Repository
public interface PayShapSettlementRecordRepository
    extends JpaRepository<PayShapSettlementRecord, String> {

  /** Find settlement records by adapter ID */
  List<PayShapSettlementRecord> findByAdapterId(String adapterId);

  /** Find settlement record by transaction ID */
  PayShapSettlementRecord findByTransactionId(String transactionId);

  /** Find settlement records by status */
  List<PayShapSettlementRecord> findByStatus(String status);

  /** Find settlement records by settlement type */
  List<PayShapSettlementRecord> findBySettlementType(String settlementType);

  /** Find settlement records by settlement date */
  List<PayShapSettlementRecord> findBySettlementDate(LocalDate settlementDate);

  /** Find settlement records by adapter ID and status */
  List<PayShapSettlementRecord> findByAdapterIdAndStatus(String adapterId, String status);

  /** Find settlement records by date range */
  @Query(
      "SELECT s FROM PayShapSettlementRecord s WHERE s.settlementDate BETWEEN :startDate AND :endDate")
  List<PayShapSettlementRecord> findByDateRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
