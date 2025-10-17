package com.payments.rtcadapter.repository;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcSettlementRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for RTC Settlement Record entities */
@Repository
public interface RtcSettlementRecordRepository
    extends JpaRepository<RtcSettlementRecord, ClearingMessageId> {

  /** Find records by adapter ID */
  @Query("SELECT rsr FROM RtcSettlementRecord rsr WHERE rsr.rtcAdapterId = :adapterId")
  List<RtcSettlementRecord> findByAdapterId(@Param("adapterId") String adapterId);

  /** Find records by settlement date */
  List<RtcSettlementRecord> findBySettlementDate(LocalDate settlementDate);

  /** Find records by settlement status */
  List<RtcSettlementRecord> findBySettlementStatus(String settlementStatus);

  /** Find record by settlement reference */
  Optional<RtcSettlementRecord> findBySettlementReference(String settlementReference);

  /** Find records by adapter and date */
  @Query(
      "SELECT rsr FROM RtcSettlementRecord rsr WHERE rsr.rtcAdapterId = :adapterId AND rsr.settlementDate = :settlementDate")
  List<RtcSettlementRecord> findByAdapterIdAndSettlementDate(
      @Param("adapterId") String adapterId, @Param("settlementDate") LocalDate settlementDate);

  /** Find records by adapter and status */
  @Query(
      "SELECT rsr FROM RtcSettlementRecord rsr WHERE rsr.rtcAdapterId = :adapterId AND rsr.settlementStatus = :settlementStatus")
  List<RtcSettlementRecord> findByAdapterIdAndSettlementStatus(
      @Param("adapterId") String adapterId, @Param("settlementStatus") String settlementStatus);

  /** Count records by status */
  long countBySettlementStatus(String settlementStatus);

  /** Count records by adapter */
  @Query("SELECT COUNT(rsr) FROM RtcSettlementRecord rsr WHERE rsr.rtcAdapterId = :adapterId")
  long countByAdapterId(@Param("adapterId") String adapterId);
}
