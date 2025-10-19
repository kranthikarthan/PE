package com.payments.samosadapter.domain;

import static org.assertj.core.api.Assertions.*;

import com.payments.domain.shared.TenantContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for SamosSettlementAccount entity */
@DisplayName("SAMOS Settlement Account Entity Tests")
class SamosSettlementAccountTest {

  private static final String TENANT_ID = "tenant-123";
  private static final String ACCOUNT_NUMBER = "123456789";
  private static final String BANK_CODE = "ABSA01";
  private static final String BANK_NAME = "ABSA Bank";
  private static final BigDecimal DAILY_LIMIT = new BigDecimal("10000000.00");
  private static final BigDecimal COLLATERAL_REQ = new BigDecimal("1000000.00");
  private static final String CREATED_BY = "admin";

  @Test
  @DisplayName("Should create settlement account with valid data")
  void shouldCreateSettlementAccountWithValidData() {
    // When
    SamosSettlementAccount account =
        SamosSettlementAccount.create(
            TenantContext.of(TENANT_ID),
            ACCOUNT_NUMBER,
            BANK_CODE,
            BANK_NAME,
            DAILY_LIMIT,
            COLLATERAL_REQ,
            CREATED_BY);

    // Then
    assertThat(account).isNotNull();
    assertThat(account.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
    assertThat(account.getBankCode()).isEqualTo(BANK_CODE);
    assertThat(account.getBankName()).isEqualTo(BANK_NAME);
    assertThat(account.getDailyDebitLimit()).isEqualTo(DAILY_LIMIT);
    assertThat(account.getCollateralRequirement()).isEqualTo(COLLATERAL_REQ);
    assertThat(account.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(account.getReservedBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(account.isActive()).isTrue();
    assertThat(account.getCurrency()).isEqualTo("ZAR");
  }

  @Test
  @DisplayName("Should reject invalid account number")
  void shouldRejectInvalidAccountNumber() {
    // Then
    assertThatThrownBy(
            () ->
                SamosSettlementAccount.create(
                    TenantContext.of(TENANT_ID),
                    "12345", // Invalid: too short
                    BANK_CODE,
                    BANK_NAME,
                    DAILY_LIMIT,
                    COLLATERAL_REQ,
                    CREATED_BY))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid SARB account number");
  }

  @Test
  @DisplayName("Should credit account successfully")
  void shouldCreditAccountSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    BigDecimal creditAmount = new BigDecimal("5000.00");

    // When
    account.credit(creditAmount, "admin");

    // Then
    assertThat(account.getCurrentBalance()).isEqualByComparingTo(creditAmount);
    assertThat(account.getLastBalanceUpdate()).isNotNull();
  }

  @Test
  @DisplayName("Should debit account successfully")
  void shouldDebitAccountSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("10000.00"), "admin");
    BigDecimal debitAmount = new BigDecimal("3000.00");

    // When
    account.debit(debitAmount, "admin");

    // Then
    assertThat(account.getCurrentBalance())
        .isEqualByComparingTo(new BigDecimal("7000.00"));
  }

  @Test
  @DisplayName("Should reject debit with insufficient balance")
  void shouldRejectDebitWithInsufficientBalance() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("1000.00"), "admin");

    // When/Then
    assertThatThrownBy(() -> account.debit(new BigDecimal("2000.00"), "admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Insufficient balance");
  }

  @Test
  @DisplayName("Should reserve funds successfully")
  void shouldReserveFundsSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("10000.00"), "admin");
    BigDecimal reserveAmount = new BigDecimal("3000.00");

    // When
    account.reserve(reserveAmount, "admin");

    // Then
    assertThat(account.getReservedBalance()).isEqualByComparingTo(reserveAmount);
    assertThat(account.getAvailableBalance())
        .isEqualByComparingTo(new BigDecimal("7000.00"));
  }

  @Test
  @DisplayName("Should reject reservation exceeding available balance")
  void shouldRejectReservationExceedingAvailableBalance() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("5000.00"), "admin");

    // When/Then
    assertThatThrownBy(() -> account.reserve(new BigDecimal("6000.00"), "admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Insufficient balance to reserve");
  }

  @Test
  @DisplayName("Should release reservation successfully")
  void shouldReleaseReservationSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("10000.00"), "admin");
    account.reserve(new BigDecimal("3000.00"), "admin");

    // When
    account.releaseReservation(new BigDecimal("1000.00"), "admin");

    // Then
    assertThat(account.getReservedBalance())
        .isEqualByComparingTo(new BigDecimal("2000.00"));
    assertThat(account.getAvailableBalance())
        .isEqualByComparingTo(new BigDecimal("8000.00"));
  }

  @Test
  @DisplayName("Should settle reserved funds successfully")
  void shouldSettleReservedFundsSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("10000.00"), "admin");
    account.reserve(new BigDecimal("3000.00"), "admin");

    // When
    account.settleReservedFunds(new BigDecimal("3000.00"), "admin");

    // Then
    assertThat(account.getCurrentBalance())
        .isEqualByComparingTo(new BigDecimal("7000.00"));
    assertThat(account.getReservedBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(account.getSettlementsToday()).isEqualTo(1);
    assertThat(account.getAmountSettledToday())
        .isEqualByComparingTo(new BigDecimal("3000.00"));
  }

  @Test
  @DisplayName("Should pledge collateral successfully")
  void shouldPledgeCollateralSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    BigDecimal pledgeAmount = new BigDecimal("500000.00");

    // When
    account.pledgeCollateral(pledgeAmount, "admin");

    // Then
    assertThat(account.getCollateralPledged()).isEqualByComparingTo(pledgeAmount);
  }

  @Test
  @DisplayName("Should release collateral successfully")
  void shouldReleaseCollateralSuccessfully() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.pledgeCollateral(new BigDecimal("2000000.00"), "admin");

    // When
    account.releaseCollateral(new BigDecimal("500000.00"), "admin");

    // Then
    assertThat(account.getCollateralPledged())
        .isEqualByComparingTo(new BigDecimal("1500000.00"));
  }

  @Test
  @DisplayName("Should reject collateral release below requirement")
  void shouldRejectCollateralReleaseBelowRequirement() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.pledgeCollateral(new BigDecimal("1500000.00"), "admin");

    // When/Then
    assertThatThrownBy(() -> account.releaseCollateral(new BigDecimal("600000.00"), "admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot release collateral below requirement");
  }

  @Test
  @DisplayName("Should check adequate collateral correctly")
  void shouldCheckAdequateCollateralCorrectly() {
    // Given
    SamosSettlementAccount account = createTestAccount();

    // When/Then - Initially no collateral
    assertThat(account.hasAdequateCollateral()).isFalse();

    // When - Pledge exact requirement
    account.pledgeCollateral(COLLATERAL_REQ, "admin");

    // Then
    assertThat(account.hasAdequateCollateral()).isTrue();

    // When - Pledge more than requirement
    account.pledgeCollateral(new BigDecimal("500000.00"), "admin");

    // Then
    assertThat(account.hasAdequateCollateral()).isTrue();
  }

  @Test
  @DisplayName("Should check daily limit correctly")
  void shouldCheckDailyLimitCorrectly() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("20000000.00"), "admin");
    account.reserve(new BigDecimal("5000000.00"), "admin");
    account.settleReservedFunds(new BigDecimal("5000000.00"), "admin");

    // When/Then - Should be within limit
    assertThat(account.isWithinDailyLimit(new BigDecimal("4000000.00"))).isTrue();

    // When/Then - Should exceed limit
    assertThat(account.isWithinDailyLimit(new BigDecimal("6000000.00"))).isFalse();
  }

  @Test
  @DisplayName("Should reset daily counters")
  void shouldResetDailyCounters() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("10000.00"), "admin");
    account.reserve(new BigDecimal("5000.00"), "admin");
    account.settleReservedFunds(new BigDecimal("5000.00"), "admin");

    // When
    account.resetDailyCounters();

    // Then
    assertThat(account.getSettlementsToday()).isZero();
    assertThat(account.getAmountSettledToday()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("Should activate suspended account")
  void shouldActivateSuspendedAccount() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.suspend("admin");

    // When
    account.activate("admin");

    // Then
    assertThat(account.isActive()).isTrue();
    assertThat(account.isSuspended()).isFalse();
  }

  @Test
  @DisplayName("Should suspend active account")
  void shouldSuspendActiveAccount() {
    // Given
    SamosSettlementAccount account = createTestAccount();

    // When
    account.suspend("admin");

    // Then
    assertThat(account.isActive()).isFalse();
    assertThat(account.isSuspended()).isTrue();
  }

  @Test
  @DisplayName("Should close account with zero balance")
  void shouldCloseAccountWithZeroBalance() {
    // Given
    SamosSettlementAccount account = createTestAccount();

    // When
    account.close("admin");

    // Then
    assertThat(account.isClosed()).isTrue();
    assertThat(account.isActive()).isFalse();
  }

  @Test
  @DisplayName("Should reject closing account with non-zero balance")
  void shouldRejectClosingAccountWithNonZeroBalance() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("1000.00"), "admin");

    // When/Then
    assertThatThrownBy(() -> account.close("admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot close account with non-zero balance");
  }

  @Test
  @DisplayName("Should reject closing account with reserved funds")
  void shouldRejectClosingAccountWithReservedFunds() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.credit(new BigDecimal("5000.00"), "admin");
    account.reserve(new BigDecimal("5000.00"), "admin");

    // When/Then
    assertThatThrownBy(() -> account.close("admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot close account with reserved funds");
  }

  @Test
  @DisplayName("Should reject operations on inactive account")
  void shouldRejectOperationsOnInactiveAccount() {
    // Given
    SamosSettlementAccount account = createTestAccount();
    account.suspend("admin");

    // When/Then
    assertThatThrownBy(() -> account.credit(new BigDecimal("100.00"), "admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot credit inactive account");

    assertThatThrownBy(() -> account.debit(new BigDecimal("100.00"), "admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot debit inactive account");

    assertThatThrownBy(() -> account.reserve(new BigDecimal("100.00"), "admin"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot reserve funds on inactive account");
  }

  private SamosSettlementAccount createTestAccount() {
    return SamosSettlementAccount.create(
        TenantContext.of(TENANT_ID),
        ACCOUNT_NUMBER,
        BANK_CODE,
        BANK_NAME,
        DAILY_LIMIT,
        COLLATERAL_REQ,
        CREATED_BY);
  }
}

