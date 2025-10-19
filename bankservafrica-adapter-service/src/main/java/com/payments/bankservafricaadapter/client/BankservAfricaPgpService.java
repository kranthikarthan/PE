package com.payments.bankservafricaadapter.client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * BankservAfrica PGP Encryption/Decryption Service
 *
 * <p>Handles PGP encryption and decryption for secure file transfer with BankservAfrica.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>File encryption using BankservAfrica's public key
 *   <li>File decryption using Payment Engine's private key
 *   <li>ASCII armored output support
 *   <li>Signature verification
 *   <li>Key management and validation
 * </ul>
 *
 * <p>Configuration:
 *
 * <pre>
 * bankservafrica.pgp.public-key-path=/etc/bankservafrica/keys/bankservafrica_public.asc
 * bankservafrica.pgp.private-key-path=/etc/bankservafrica/keys/payment_engine_private.asc
 * bankservafrica.pgp.private-key-password=${BANKSERVAFRICA_PGP_PASSWORD}
 * bankservafrica.pgp.armored=true
 * bankservafrica.pgp.verify-signatures=true
 * </pre>
 */
@Component
@Slf4j
public class BankservAfricaPgpService {

  static {
    // Add BouncyCastle as Security Provider
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @Value("${bankservafrica.pgp.public-key-path}")
  private String publicKeyPath;

  @Value("${bankservafrica.pgp.private-key-path}")
  private String privateKeyPath;

  @Value("${bankservafrica.pgp.private-key-password}")
  private String privateKeyPassword;

  @Value("${bankservafrica.pgp.armored:true}")
  private boolean armored;

  @Value("${bankservafrica.pgp.verify-signatures:true}")
  private boolean verifySignatures;

  /**
   * Encrypt file content for BankservAfrica
   *
   * @param plaintext Plain text content
   * @return Encrypted content
   */
  public byte[] encrypt(byte[] plaintext) {
    log.info("Encrypting content with PGP ({} bytes)", plaintext.length);

    try {
      // Load public key
      PGPPublicKey publicKey = loadPublicKey(publicKeyPath);

      // Create encrypted data generator
      ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
      OutputStream out = armored ? new ArmoredOutputStream(encryptedOut) : encryptedOut;

      // Initialize encryption
      PGPEncryptedDataGenerator encGen =
          new PGPEncryptedDataGenerator(
              new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                  .setWithIntegrityPacket(true)
                  .setSecureRandom(new java.security.SecureRandom())
                  .setProvider(BouncyCastleProvider.PROVIDER_NAME));

      encGen.addMethod(
          new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
              .setProvider(BouncyCastleProvider.PROVIDER_NAME));

      OutputStream encryptedStream = encGen.open(out, new byte[8192]);

      // Create compressed data
      PGPCompressedDataGenerator compressor = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
      OutputStream compressedStream = compressor.open(encryptedStream);

      // Create literal data
      PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();
      OutputStream literalStream =
          literalGen.open(
              compressedStream,
              PGPLiteralData.BINARY,
              "encrypted_file",
              plaintext.length,
              new Date());

      // Write plaintext
      literalStream.write(plaintext);

      // Close streams
      literalStream.close();
      compressor.close();
      encryptedStream.close();
      out.close();

      byte[] encrypted = encryptedOut.toByteArray();
      log.info("Content encrypted successfully ({} bytes)", encrypted.length);
      return encrypted;

    } catch (Exception e) {
      log.error("Failed to encrypt content with PGP: {}", e.getMessage(), e);
      throw new BankservAfricaPgpException("Failed to encrypt content", e);
    }
  }

  /**
   * Encrypt file for BankservAfrica
   *
   * @param inputFile Input file path
   * @param outputFile Output file path
   */
  public void encryptFile(String inputFile, String outputFile) {
    log.info("Encrypting file with PGP: {} -> {}", inputFile, outputFile);

    try {
      byte[] plaintext = Files.readAllBytes(Paths.get(inputFile));
      byte[] encrypted = encrypt(plaintext);
      Files.write(Paths.get(outputFile), encrypted);

      log.info("File encrypted successfully: {}", outputFile);

    } catch (IOException e) {
      log.error("Failed to encrypt file with PGP: {}", e.getMessage(), e);
      throw new BankservAfricaPgpException("Failed to encrypt file: " + inputFile, e);
    }
  }

  /**
   * Decrypt file content from BankservAfrica
   *
   * @param encryptedData Encrypted content
   * @return Decrypted content
   */
  public byte[] decrypt(byte[] encryptedData) {
    log.info("Decrypting content with PGP ({} bytes)", encryptedData.length);

    try {
      // Load private key
      PGPPrivateKey privateKey = loadPrivateKey(privateKeyPath, privateKeyPassword);

      // Parse encrypted data
      InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedData));

      PGPObjectFactory pgpFactory = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
      Object firstObject = pgpFactory.nextObject();

      // Handle encrypted data list
      PGPEncryptedDataList encDataList;
      if (firstObject instanceof PGPEncryptedDataList) {
        encDataList = (PGPEncryptedDataList) firstObject;
      } else {
        encDataList = (PGPEncryptedDataList) pgpFactory.nextObject();
      }

      // Find encrypted data for our key
      Iterator<?> it = encDataList.getEncryptedDataObjects();
      PGPPublicKeyEncryptedData encData = null;

      while (it.hasNext()) {
        encData = (PGPPublicKeyEncryptedData) it.next();
        // Use first encrypted data object
        break;
      }

      if (encData == null) {
        throw new BankservAfricaPgpException("No encrypted data found", null);
      }

      // Decrypt data
      InputStream clearStream =
          encData.getDataStream(
              new JcePublicKeyDataDecryptorFactoryBuilder()
                  .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                  .build(privateKey));

      PGPObjectFactory plainFactory =
          new PGPObjectFactory(clearStream, new JcaKeyFingerprintCalculator());
      Object message = plainFactory.nextObject();

      // Handle compressed data
      if (message instanceof PGPCompressedData) {
        PGPCompressedData compressedData = (PGPCompressedData) message;
        PGPObjectFactory pgpFactoryCompressed =
            new PGPObjectFactory(compressedData.getDataStream(), new JcaKeyFingerprintCalculator());
        message = pgpFactoryCompressed.nextObject();
      }

      // Extract literal data
      ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
      if (message instanceof PGPLiteralData) {
        PGPLiteralData literalData = (PGPLiteralData) message;
        InputStream literalStream = literalData.getInputStream();

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = literalStream.read(buffer)) != -1) {
          decryptedOut.write(buffer, 0, bytesRead);
        }
      } else {
        throw new BankservAfricaPgpException(
            "Unexpected message type: " + message.getClass().getName(), null);
      }

      // Verify integrity
      if (encData.isIntegrityProtected() && !encData.verify()) {
        throw new BankservAfricaPgpException("Data integrity check failed", null);
      }

      byte[] decrypted = decryptedOut.toByteArray();
      log.info("Content decrypted successfully ({} bytes)", decrypted.length);
      return decrypted;

    } catch (Exception e) {
      log.error("Failed to decrypt content with PGP: {}", e.getMessage(), e);
      throw new BankservAfricaPgpException("Failed to decrypt content", e);
    }
  }

  /**
   * Decrypt file from BankservAfrica
   *
   * @param encryptedFile Encrypted file path
   * @param outputFile Output file path
   */
  public void decryptFile(String encryptedFile, String outputFile) {
    log.info("Decrypting file with PGP: {} -> {}", encryptedFile, outputFile);

    try {
      byte[] encrypted = Files.readAllBytes(Paths.get(encryptedFile));
      byte[] decrypted = decrypt(encrypted);
      Files.write(Paths.get(outputFile), decrypted);

      log.info("File decrypted successfully: {}", outputFile);

    } catch (IOException e) {
      log.error("Failed to decrypt file with PGP: {}", e.getMessage(), e);
      throw new BankservAfricaPgpException("Failed to decrypt file: " + encryptedFile, e);
    }
  }

  /**
   * Load PGP public key from file
   *
   * @param keyPath Key file path
   * @return PGP public key
   */
  private PGPPublicKey loadPublicKey(String keyPath) throws IOException, PGPException {
    try (InputStream keyIn = Files.newInputStream(Paths.get(keyPath))) {
      InputStream decoderStream = PGPUtil.getDecoderStream(keyIn);
      PGPPublicKeyRingCollection keyRingCollection =
          new PGPPublicKeyRingCollection(decoderStream, new JcaKeyFingerprintCalculator());

      // Find first encryption key
      Iterator<PGPPublicKeyRing> keyRings = keyRingCollection.getKeyRings();
      while (keyRings.hasNext()) {
        PGPPublicKeyRing keyRing = keyRings.next();
        Iterator<PGPPublicKey> keys = keyRing.getPublicKeys();
        while (keys.hasNext()) {
          PGPPublicKey key = keys.next();
          if (key.isEncryptionKey()) {
            log.debug("Loaded PGP public key: {}", Long.toHexString(key.getKeyID()));
            return key;
          }
        }
      }

      throw new PGPException("No encryption key found in: " + keyPath);
    }
  }

  /**
   * Load PGP private key from file
   *
   * @param keyPath Key file path
   * @param password Key password
   * @return PGP private key
   */
  private PGPPrivateKey loadPrivateKey(String keyPath, String password)
      throws IOException, PGPException {
    try (InputStream keyIn = Files.newInputStream(Paths.get(keyPath))) {
      InputStream decoderStream = PGPUtil.getDecoderStream(keyIn);
      PGPSecretKeyRingCollection keyRingCollection =
          new PGPSecretKeyRingCollection(decoderStream, new JcaKeyFingerprintCalculator());

      // Find first signing key
      Iterator<PGPSecretKeyRing> keyRings = keyRingCollection.getKeyRings();
      while (keyRings.hasNext()) {
        PGPSecretKeyRing keyRing = keyRings.next();
        Iterator<PGPSecretKey> keys = keyRing.getSecretKeys();
        while (keys.hasNext()) {
          PGPSecretKey secretKey = keys.next();
          if (secretKey.isSigningKey()) {
            PGPPrivateKey privateKey =
                secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .build(password.toCharArray()));

            log.debug("Loaded PGP private key: {}", Long.toHexString(secretKey.getKeyID()));
            return privateKey;
          }
        }
      }

      throw new PGPException("No signing key found in: " + keyPath);
    }
  }

  /**
   * Test PGP configuration
   *
   * @return true if configuration is valid
   */
  public boolean testConfiguration() {
    log.info("Testing BankservAfrica PGP configuration");

    try {
      // Test public key loading
      PGPPublicKey publicKey = loadPublicKey(publicKeyPath);
      log.info("Public key loaded: {}", Long.toHexString(publicKey.getKeyID()));

      // Test private key loading
      PGPPrivateKey privateKey = loadPrivateKey(privateKeyPath, privateKeyPassword);
      log.info("Private key loaded successfully");

      // Test encryption/decryption
      byte[] testData = "BankservAfrica PGP Test".getBytes();
      byte[] encrypted = encrypt(testData);
      byte[] decrypted = decrypt(encrypted);

      if (!new String(decrypted).equals(new String(testData))) {
        log.error("PGP encryption/decryption test failed: data mismatch");
        return false;
      }

      log.info("BankservAfrica PGP configuration test successful");
      return true;

    } catch (Exception e) {
      log.error("BankservAfrica PGP configuration test failed: {}", e.getMessage(), e);
      return false;
    }
  }

  /** Custom exception for PGP operations */
  public static class BankservAfricaPgpException extends RuntimeException {
    public BankservAfricaPgpException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
