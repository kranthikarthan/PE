package com.paymentengine.middleware.security;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

/**
 * Service for message encryption and digital signatures
 */
@Service
public class MessageEncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${app.security.encryption.key:aes-256-key-for-iso20022-messages}")
    private String encryptionKey;

    @Value("${app.security.signature.private-key:}")
    private String privateKeyBase64;

    @Value("${app.security.signature.public-key:}")
    private String publicKeyBase64;

    /**
     * Encrypt a message using AES-GCM
     */
    public String encryptMessage(String message) throws Exception {
        if (message == null || message.isEmpty()) {
            return message;
        }

        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        byte[] encryptedData = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and encrypted data
        byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
        
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    /**
     * Decrypt a message using AES-GCM
     */
    public String decryptMessage(String encryptedMessage) throws Exception {
        if (encryptedMessage == null || encryptedMessage.isEmpty()) {
            return encryptedMessage;
        }

        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedMessage);
        
        // Extract IV and encrypted data
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
        
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Sign a message using RSA
     */
    public String signMessage(String message) throws Exception {
        if (privateKeyBase64 == null || privateKeyBase64.isEmpty()) {
            throw new IllegalStateException("Private key not configured for message signing");
        }

        PrivateKey privateKey = getPrivateKey();
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Verify a message signature using RSA
     */
    public boolean verifySignature(String message, String signature) throws Exception {
        if (publicKeyBase64 == null || publicKeyBase64.isEmpty()) {
            throw new IllegalStateException("Public key not configured for signature verification");
        }

        PublicKey publicKey = getPublicKey();
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(publicKey);
        sig.update(message.getBytes(StandardCharsets.UTF_8));
        
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return sig.verify(signatureBytes);
    }

    /**
     * Encrypt and sign a message
     */
    public Map<String, String> encryptAndSignMessage(String message) throws Exception {
        String encryptedMessage = encryptMessage(message);
        String signature = signMessage(encryptedMessage);
        
        Map<String, String> result = new HashMap<>();
        result.put("encryptedMessage", encryptedMessage);
        result.put("signature", signature);
        result.put("algorithm", "AES-GCM+RSA");
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return result;
    }

    /**
     * Verify signature and decrypt a message
     */
    public String verifyAndDecryptMessage(String encryptedMessage, String signature) throws Exception {
        if (!verifySignature(encryptedMessage, signature)) {
            throw new SecurityException("Message signature verification failed");
        }
        
        return decryptMessage(encryptedMessage);
    }

    private PrivateKey getPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }

    private PublicKey getPublicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Generate RSA key pair for testing
     */
    public static Map<String, String> generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        Map<String, String> keys = new HashMap<>();
        keys.put("privateKey", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        keys.put("publicKey", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        
        return keys;
    }
}