package com.payments.bankservafricaadapter.repository;

import com.payments.bankservafricaadapter.domain.BankservAfricaEftMessage;
import com.payments.domain.shared.ClearingMessageId;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for BankservAfrica EFT Message entities */
@Repository
public interface BankservAfricaEftMessageRepository
    extends JpaRepository<BankservAfricaEftMessage, ClearingMessageId> {

  /** Find messages by batch ID */
  List<BankservAfricaEftMessage> findByBatchId(String batchId);

  /** Find messages by adapter ID */
  @Query("SELECT m FROM BankservAfricaEftMessage m WHERE m.bankservafricaAdapterId = :adapterId")
  List<BankservAfricaEftMessage> findByAdapterId(@Param("adapterId") String adapterId);

  /** Find messages by status */
  List<BankservAfricaEftMessage> findByStatus(String status);

  /** Find messages by batch ID and status */
  List<BankservAfricaEftMessage> findByBatchIdAndStatus(String batchId, String status);

  /** Find messages by adapter ID and status */
  @Query(
      "SELECT m FROM BankservAfricaEftMessage m WHERE m.bankservafricaAdapterId = :adapterId AND m.status = :status")
  List<BankservAfricaEftMessage> findByAdapterIdAndStatus(
      @Param("adapterId") String adapterId, @Param("status") String status);

  /** Find messages by direction */
  List<BankservAfricaEftMessage> findByDirection(String direction);

  /** Find messages by message type */
  List<BankservAfricaEftMessage> findByMessageType(String messageType);

  /** Find messages created after timestamp */
  List<BankservAfricaEftMessage> findByCreatedAtAfter(Instant timestamp);

  /** Find messages by batch ID and direction */
  List<BankservAfricaEftMessage> findByBatchIdAndDirection(String batchId, String direction);

  /** Count messages by batch ID */
  long countByBatchId(String batchId);

  /** Count messages by batch ID and status */
  long countByBatchIdAndStatus(String batchId, String status);
}
