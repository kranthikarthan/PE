package com.payments.bankservafricaadapter.client;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for BankservAfricaPgpService
 *
 * <p>Tests PGP encryption and decryption for BankservAfrica including:
 *
 * <ul>
 *   <li>Content encryption/decryption
 *   <li>File encryption/decryption
 *   <li>Key management
 *   <li>Configuration validation
 *   <li>Error handling
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class BankservAfricaPgpServiceTest {

  @TempDir Path tempDir;

  private BankservAfricaPgpService pgpService;

  @BeforeAll
  static void setUpClass() {
    // Ensure BouncyCastle is registered
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @BeforeEach
  void setUp() {
    pgpService = new BankservAfricaPgpService();

    // Set test configuration
    String publicKeyPath = tempDir.resolve("public.asc").toString();
    String privateKeyPath = tempDir.resolve("private.asc").toString();

    ReflectionTestUtils.setField(pgpService, "publicKeyPath", publicKeyPath);
    ReflectionTestUtils.setField(pgpService, "privateKeyPath", privateKeyPath);
    ReflectionTestUtils.setField(pgpService, "privateKeyPassword", "test-password");
    ReflectionTestUtils.setField(pgpService, "armored", true);
    ReflectionTestUtils.setField(pgpService, "verifySignatures", true);
  }

  @Test
  void shouldHandleEncryptionWithoutKeys() {
    // Given
    byte[] plaintext = "Test ACH batch content".getBytes(StandardCharsets.UTF_8);

    // When/Then - Keys don't exist, should fail gracefully
    assertThatThrownBy(() -> pgpService.encrypt(plaintext))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to encrypt");
  }

  @Test
  void shouldHandleDecryptionWithoutKeys() {
    // Given
    byte[] encryptedData =
        "-----BEGIN PGP MESSAGE-----\ntest\n-----END PGP MESSAGE-----".getBytes();

    // When/Then - Keys don't exist, should fail gracefully
    assertThatThrownBy(() -> pgpService.decrypt(encryptedData))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to decrypt");
  }

  @Test
  void shouldHandleFileEncryptionError() throws IOException {
    // Given
    Path inputFile = tempDir.resolve("plain.txt");
    Path outputFile = tempDir.resolve("encrypted.pgp");
    Files.writeString(inputFile, "Test content");

    // When/Then - Keys don't exist
    assertThatThrownBy(() -> pgpService.encryptFile(inputFile.toString(), outputFile.toString()))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to encrypt file");
  }

  @Test
  void shouldHandleFileDecryptionError() throws IOException {
    // Given
    Path encryptedFile = tempDir.resolve("encrypted.pgp");
    Path outputFile = tempDir.resolve("decrypted.txt");
    Files.writeString(encryptedFile, "Invalid PGP content");

    // When/Then
    assertThatThrownBy(
            () -> pgpService.decryptFile(encryptedFile.toString(), outputFile.toString()))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to decrypt file");
  }

  @Test
  void shouldValidateConfiguration() {
    // Then
    assertThat(ReflectionTestUtils.getField(pgpService, "publicKeyPath")).isNotNull();
    assertThat(ReflectionTestUtils.getField(pgpService, "privateKeyPath")).isNotNull();
    assertThat(ReflectionTestUtils.getField(pgpService, "privateKeyPassword"))
        .isEqualTo("test-password");
    assertThat(ReflectionTestUtils.getField(pgpService, "armored")).isEqualTo(true);
    assertThat(ReflectionTestUtils.getField(pgpService, "verifySignatures")).isEqualTo(true);
  }

  @Test
  void shouldFailConfigurationTestWithoutKeys() {
    // When
    boolean result = pgpService.testConfiguration();

    // Then
    assertThat(result).isFalse(); // Keys don't exist
  }

  @Test
  void shouldProvideAppropriateExceptionMessages() {
    // Given
    Exception cause = new IOException("Key file not found");
    BankservAfricaPgpService.BankservAfricaPgpException exception =
        new BankservAfricaPgpService.BankservAfricaPgpException("PGP operation failed", cause);

    // Then
    assertThat(exception.getMessage()).isEqualTo("PGP operation failed");
    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.getCause().getMessage()).isEqualTo("Key file not found");
  }

  @Test
  void shouldHandleNonExistentInputFile() {
    // Given
    String nonExistentFile = tempDir.resolve("nonexistent.txt").toString();
    String outputFile = tempDir.resolve("output.pgp").toString();

    // When/Then
    assertThatThrownBy(() -> pgpService.encryptFile(nonExistentFile, outputFile))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to encrypt file");
  }

  @Test
  void shouldHandleInvalidEncryptedData() {
    // Given
    byte[] invalidData = "This is not valid PGP encrypted data".getBytes();

    // When/Then
    assertThatThrownBy(() -> pgpService.decrypt(invalidData))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to decrypt");
  }

  @Test
  void shouldHandleEmptyContent() {
    // Given
    byte[] emptyContent = new byte[0];

    // When/Then - Empty content should still fail gracefully (no keys)
    assertThatThrownBy(() -> pgpService.encrypt(emptyContent))
        .isInstanceOf(BankservAfricaPgpService.BankservAfricaPgpException.class)
        .hasMessageContaining("Failed to encrypt");
  }

  /**
   * Note: Full encryption/decryption round-trip tests with real keys should be in integration tests
   * These unit tests focus on error handling and configuration validation
   */
  @Test
  void shouldVerifyBouncyCastleProviderIsRegistered() {
    // Then
    assertThat(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)).isNotNull();
  }
}
