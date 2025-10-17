package com.payments.payshapadapter.repository;

import com.payments.payshapadapter.domain.PayShapPaymentMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for PayShap Payment Message entities */
@Repository
public interface PayShapPaymentMessageRepository
    extends JpaRepository<PayShapPaymentMessage, String> {

  /** Find messages by adapter ID */
  List<PayShapPaymentMessage> findByAdapterId(String adapterId);

  /** Find message by transaction ID */
  PayShapPaymentMessage findByTransactionId(String transactionId);

  /** Find messages by status */
  List<PayShapPaymentMessage> findByStatus(String status);

  /** Find messages by adapter ID and status */
  List<PayShapPaymentMessage> findByAdapterIdAndStatus(String adapterId, String status);

  /** Find messages by proxy type and value */
  @Query(
      "SELECT p FROM PayShapPaymentMessage p WHERE p.proxyType = :proxyType AND p.proxyValue = :proxyValue")
  List<PayShapPaymentMessage> findByProxyTypeAndValue(
      @Param("proxyType") String proxyType, @Param("proxyValue") String proxyValue);

  /** Count messages by status */
  long countByStatus(String status);

  /** Count messages by adapter ID */
  long countByAdapterId(String adapterId);

  /** Find messages by amount range */
  @Query("SELECT p FROM PayShapPaymentMessage p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
  List<PayShapPaymentMessage> findByAmountRange(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);
}
