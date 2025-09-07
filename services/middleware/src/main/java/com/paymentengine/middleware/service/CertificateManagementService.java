package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.certificate.*;
import com.paymentengine.middleware.entity.CertificateInfo;
import com.paymentengine.middleware.entity.TrustedCertificate;
import com.paymentengine.middleware.exception.CertificateException;
import com.paymentengine.middleware.repository.CertificateInfoRepository;
import com.paymentengine.middleware.repository.TrustedCertificateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Certificate Management Service
 * 
 * Provides automated certificate and key generation, .pfx consumption,
 * validation, and management for the Payment Engine.
 */
@Service
@Transactional
public class CertificateManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateManagementService.class);
    
    @Autowired
    private CertificateInfoRepository certificateInfoRepository;
    
    @Autowired
    private TrustedCertificateRepository trustedCertificateRepository;
    
    @Value("${certificate.storage.path:/app/certificates}")
    private String certificateStoragePath;
    
    @Value("${certificate.default.validity.days:365}")
    private int defaultValidityDays;
    
    @Value("${certificate.key.size:2048}")
    private int keySize;
    
    @Value("${certificate.signature.algorithm:SHA256withRSA}")
    private String signatureAlgorithm;
    
    /**
     * Generate a new certificate and private key pair
     */
    public CertificateGenerationResult generateCertificate(CertificateGenerationRequest request) {
        logger.info("Generating certificate for subject: {}", request.getSubjectDN());
        
        try {
            // Generate key pair
            KeyPair keyPair = generateKeyPair();
            
            // Create certificate
            X509Certificate certificate = createSelfSignedCertificate(
                keyPair, 
                request.getSubjectDN(), 
                request.getValidityDays() != null ? request.getValidityDays() : defaultValidityDays,
                request.getKeyUsage(),
                request.getExtendedKeyUsage()
            );
            
            // Save certificate info to database
            CertificateInfo certInfo = saveCertificateInfo(certificate, keyPair, request);
            
            // Save certificate and key to filesystem
            saveCertificateToFileSystem(certInfo, certificate, keyPair);
            
            logger.info("Certificate generated successfully with ID: {}", certInfo.getId());
            
            return CertificateGenerationResult.builder()
                .certificateId(certInfo.getId())
                .subjectDN(certificate.getSubjectDN().getName())
                .issuerDN(certificate.getIssuerDN().getName())
                .validFrom(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .validTo(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .serialNumber(certificate.getSerialNumber().toString())
                .publicKeyAlgorithm(certificate.getPublicKey().getAlgorithm())
                .keySize(keySize)
                .signatureAlgorithm(certificate.getSigAlgName())
                .certificatePem(convertToPem(certificate))
                .privateKeyPem(convertToPem(keyPair.getPrivate()))
                .build();
                
        } catch (Exception e) {
            logger.error("Error generating certificate: {}", e.getMessage(), e);
            throw new CertificateException("Failed to generate certificate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Import and consume a .pfx certificate file
     */
    public PfxImportResult importPfxCertificate(MultipartFile pfxFile, PfxImportRequest request) {
        logger.info("Importing PFX certificate: {}", pfxFile.getOriginalFilename());
        
        try {
            // Load PFX file
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(pfxFile.getInputStream(), request.getPassword().toCharArray());
            
            // Extract certificates and keys
            PfxImportResult result = extractFromPfx(keyStore, request);
            
            // Save to database
            savePfxCertificateInfo(result, request);
            
            // Save to filesystem
            savePfxToFileSystem(result, pfxFile, request);
            
            logger.info("PFX certificate imported successfully");
            return result;
            
        } catch (Exception e) {
            logger.error("Error importing PFX certificate: {}", e.getMessage(), e);
            throw new CertificateException("Failed to import PFX certificate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate a certificate against trusted CA certificates
     */
    public CertificateValidationResult validateCertificate(String certificateId) {
        logger.info("Validating certificate: {}", certificateId);
        
        try {
            CertificateInfo certInfo = certificateInfoRepository.findById(UUID.fromString(certificateId))
                .orElseThrow(() -> new CertificateException("Certificate not found: " + certificateId));
            
            // Load certificate
            X509Certificate certificate = loadCertificateFromFile(certInfo);
            
            // Build trust store from trusted certificates
            KeyStore trustStore = buildTrustStore();
            
            // Validate certificate
            CertificateValidationResult result = validateCertificateChain(certificate, trustStore);
            
            // Update validation status in database
            certInfo.setLastValidated(LocalDateTime.now());
            certInfo.setValidationStatus(result.getStatus());
            certInfo.setValidationMessage(result.getMessage());
            certificateInfoRepository.save(certInfo);
            
            logger.info("Certificate validation completed: {}", result.getStatus());
            return result;
            
        } catch (Exception e) {
            logger.error("Error validating certificate: {}", e.getMessage(), e);
            throw new CertificateException("Failed to validate certificate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all certificates with optional filtering
     */
    @Transactional(readOnly = true)
    public List<CertificateInfo> getAllCertificates(CertificateFilter filter) {
        if (filter == null) {
            return certificateInfoRepository.findAll();
        }
        
        return certificateInfoRepository.findByFilter(
            filter.getTenantId(),
            filter.getCertificateType(),
            filter.getStatus(),
            filter.getValidFrom(),
            filter.getValidTo()
        );
    }
    
    /**
     * Get certificate by ID
     */
    @Transactional(readOnly = true)
    public CertificateInfo getCertificateById(String certificateId) {
        return certificateInfoRepository.findById(UUID.fromString(certificateId))
            .orElseThrow(() -> new CertificateException("Certificate not found: " + certificateId));
    }
    
    /**
     * Delete certificate
     */
    public void deleteCertificate(String certificateId) {
        logger.info("Deleting certificate: {}", certificateId);
        
        try {
            CertificateInfo certInfo = certificateInfoRepository.findById(UUID.fromString(certificateId))
                .orElseThrow(() -> new CertificateException("Certificate not found: " + certificateId));
            
            // Delete from filesystem
            deleteCertificateFromFileSystem(certInfo);
            
            // Delete from database
            certificateInfoRepository.delete(certInfo);
            
            logger.info("Certificate deleted successfully: {}", certificateId);
            
        } catch (Exception e) {
            logger.error("Error deleting certificate: {}", e.getMessage(), e);
            throw new CertificateException("Failed to delete certificate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Rotate certificate (generate new one and mark old as expired)
     */
    public CertificateGenerationResult rotateCertificate(String certificateId, CertificateGenerationRequest request) {
        logger.info("Rotating certificate: {}", certificateId);
        
        try {
            // Get existing certificate
            CertificateInfo existingCert = certificateInfoRepository.findById(UUID.fromString(certificateId))
                .orElseThrow(() -> new CertificateException("Certificate not found: " + certificateId));
            
            // Generate new certificate
            CertificateGenerationResult newCert = generateCertificate(request);
            
            // Mark old certificate as rotated
            existingCert.setStatus("ROTATED");
            existingCert.setRotatedTo(newCert.getCertificateId());
            existingCert.setRotatedAt(LocalDateTime.now());
            certificateInfoRepository.save(existingCert);
            
            logger.info("Certificate rotated successfully: {} -> {}", certificateId, newCert.getCertificateId());
            return newCert;
            
        } catch (Exception e) {
            logger.error("Error rotating certificate: {}", e.getMessage(), e);
            throw new CertificateException("Failed to rotate certificate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get certificates expiring soon
     */
    @Transactional(readOnly = true)
    public List<CertificateInfo> getExpiringCertificates(int daysAhead) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(daysAhead);
        return certificateInfoRepository.findExpiringCertificates(expiryDate);
    }
    
    /**
     * Generate key pair
     */
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        return keyGen.generateKeyPair();
    }
    
    /**
     * Create self-signed certificate
     */
    private X509Certificate createSelfSignedCertificate(
            KeyPair keyPair, 
            String subjectDN, 
            int validityDays,
            List<String> keyUsage,
            List<String> extendedKeyUsage) throws Exception {
        
        // Create certificate builder
        X509CertificatBuilder builder = X509CertificatBuilder.builder()
            .setSubjectDN(new X500Principal(subjectDN))
            .setIssuerDN(new X500Principal(subjectDN)) // Self-signed
            .setPublicKey(keyPair.getPublic())
            .setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
            .setNotBefore(new Date())
            .setNotAfter(new Date(System.currentTimeMillis() + validityDays * 24L * 60L * 60L * 1000L));
        
        // Add key usage extensions
        if (keyUsage != null && !keyUsage.isEmpty()) {
            builder.addExtension(Extension.keyUsage, false, 
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        }
        
        // Add extended key usage
        if (extendedKeyUsage != null && !extendedKeyUsage.isEmpty()) {
            List<ASN1ObjectIdentifier> oids = extendedKeyUsage.stream()
                .map(ExtendedKeyUsage::getOID)
                .collect(Collectors.toList());
            builder.addExtension(Extension.extendedKeyUsage, false, 
                new ExtendedKeyUsage(oids.toArray(new ASN1ObjectIdentifier[0])));
        }
        
        // Sign certificate
        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm)
            .build(keyPair.getPrivate());
        
        return new JcaX509CertificateConverter()
            .getCertificate(builder.build(signer));
    }
    
    /**
     * Extract certificates and keys from PFX
     */
    private PfxImportResult extractFromPfx(KeyStore keyStore, PfxImportRequest request) throws Exception {
        PfxImportResult result = new PfxImportResult();
        
        // Get aliases
        Enumeration<String> aliases = keyStore.aliases();
        List<String> aliasList = Collections.list(aliases);
        
        if (aliasList.isEmpty()) {
            throw new CertificateException("No certificates found in PFX file");
        }
        
        // Extract first certificate (usually the main one)
        String alias = aliasList.get(0);
        Certificate cert = keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, request.getPassword().toCharArray());
        
        if (cert instanceof X509Certificate) {
            X509Certificate x509Cert = (X509Certificate) cert;
            
            result.setSubjectDN(x509Cert.getSubjectDN().getName());
            result.setIssuerDN(x509Cert.getIssuerDN().getName());
            result.setValidFrom(x509Cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            result.setValidTo(x509Cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            result.setSerialNumber(x509Cert.getSerialNumber().toString());
            result.setPublicKeyAlgorithm(x509Cert.getPublicKey().getAlgorithm());
            result.setSignatureAlgorithm(x509Cert.getSigAlgName());
            result.setCertificatePem(convertToPem(x509Cert));
            result.setPrivateKeyPem(convertToPem(privateKey));
            result.setAlias(alias);
        }
        
        // Extract certificate chain
        Certificate[] chain = keyStore.getCertificateChain(alias);
        if (chain != null) {
            List<String> chainPems = Arrays.stream(chain)
                .map(this::convertToPem)
                .collect(Collectors.toList());
            result.setCertificateChain(chainPems);
        }
        
        return result;
    }
    
    /**
     * Validate certificate chain
     */
    private CertificateValidationResult validateCertificateChain(X509Certificate certificate, KeyStore trustStore) {
        try {
            // Create certificate path builder
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            PKIXBuilderParameters params = new PKIXBuilderParameters(trustStore, new X509CertSelector() {
                @Override
                public boolean match(Certificate cert) {
                    return cert.equals(certificate);
                }
            });
            
            // Build certificate path
            CertPath certPath = builder.build(params).getCertPath();
            
            // Validate certificate path
            CertPathValidator validator = CertPathValidator.getInstance("PKIX");
            PKIXParameters pkixParams = new PKIXParameters(trustStore);
            pkixParams.setRevocationEnabled(false); // Disable for now
            
            PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validator.validate(certPath, pkixParams);
            
            return CertificateValidationResult.builder()
                .status("VALID")
                .message("Certificate is valid")
                .validFrom(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .validTo(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .issuerDN(certificate.getIssuerDN().getName())
                .subjectDN(certificate.getSubjectDN().getName())
                .build();
                
        } catch (Exception e) {
            return CertificateValidationResult.builder()
                .status("INVALID")
                .message("Certificate validation failed: " + e.getMessage())
                .validFrom(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .validTo(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .issuerDN(certificate.getIssuerDN().getName())
                .subjectDN(certificate.getSubjectDN().getName())
                .build();
        }
    }
    
    /**
     * Build trust store from trusted certificates
     */
    private KeyStore buildTrustStore() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        
        List<TrustedCertificate> trustedCerts = trustedCertificateRepository.findAll();
        for (TrustedCertificate trustedCert : trustedCerts) {
            X509Certificate cert = loadTrustedCertificate(trustedCert);
            trustStore.setCertificateEntry(trustedCert.getAlias(), cert);
        }
        
        return trustStore;
    }
    
    /**
     * Convert certificate/key to PEM format
     */
    private String convertToPem(Object obj) {
        try {
            StringWriter writer = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
            pemWriter.writeObject(obj);
            pemWriter.close();
            return writer.toString();
        } catch (Exception e) {
            throw new CertificateException("Failed to convert to PEM format", e);
        }
    }
    
    /**
     * Save certificate info to database
     */
    private CertificateInfo saveCertificateInfo(X509Certificate certificate, KeyPair keyPair, CertificateGenerationRequest request) {
        CertificateInfo certInfo = new CertificateInfo();
        certInfo.setId(UUID.randomUUID());
        certInfo.setSubjectDN(certificate.getSubjectDN().getName());
        certInfo.setIssuerDN(certificate.getIssuerDN().getName());
        certInfo.setSerialNumber(certificate.getSerialNumber().toString());
        certInfo.setValidFrom(certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        certInfo.setValidTo(certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        certInfo.setPublicKeyAlgorithm(certificate.getPublicKey().getAlgorithm());
        certInfo.setKeySize(keySize);
        certInfo.setSignatureAlgorithm(certificate.getSigAlgName());
        certInfo.setCertificateType(request.getCertificateType());
        certInfo.setTenantId(request.getTenantId());
        certInfo.setStatus("ACTIVE");
        certInfo.setCreatedAt(LocalDateTime.now());
        certInfo.setUpdatedAt(LocalDateTime.now());
        
        return certificateInfoRepository.save(certInfo);
    }
    
    /**
     * Save certificate to filesystem
     */
    private void saveCertificateToFileSystem(CertificateInfo certInfo, X509Certificate certificate, KeyPair keyPair) throws Exception {
        // Create directory if not exists
        File certDir = new File(certificateStoragePath, certInfo.getId().toString());
        certDir.mkdirs();
        
        // Save certificate
        try (FileOutputStream certOut = new FileOutputStream(new File(certDir, "certificate.pem"))) {
            certOut.write(convertToPem(certificate).getBytes());
        }
        
        // Save private key
        try (FileOutputStream keyOut = new FileOutputStream(new File(certDir, "private-key.pem"))) {
            keyOut.write(convertToPem(keyPair.getPrivate()).getBytes());
        }
        
        // Save public key
        try (FileOutputStream pubKeyOut = new FileOutputStream(new File(certDir, "public-key.pem"))) {
            pubKeyOut.write(convertToPem(keyPair.getPublic()).getBytes());
        }
    }
    
    /**
     * Save PFX certificate info to database
     */
    private void savePfxCertificateInfo(PfxImportResult result, PfxImportRequest request) {
        CertificateInfo certInfo = new CertificateInfo();
        certInfo.setId(UUID.randomUUID());
        certInfo.setSubjectDN(result.getSubjectDN());
        certInfo.setIssuerDN(result.getIssuerDN());
        certInfo.setSerialNumber(result.getSerialNumber());
        certInfo.setValidFrom(result.getValidFrom());
        certInfo.setValidTo(result.getValidTo());
        certInfo.setPublicKeyAlgorithm(result.getPublicKeyAlgorithm());
        certInfo.setSignatureAlgorithm(result.getSignatureAlgorithm());
        certInfo.setCertificateType("PFX_IMPORTED");
        certInfo.setTenantId(request.getTenantId());
        certInfo.setStatus("ACTIVE");
        certInfo.setCreatedAt(LocalDateTime.now());
        certInfo.setUpdatedAt(LocalDateTime.now());
        certInfo.setAlias(result.getAlias());
        
        certificateInfoRepository.save(certInfo);
    }
    
    /**
     * Save PFX to filesystem
     */
    private void savePfxToFileSystem(PfxImportResult result, MultipartFile pfxFile, PfxImportRequest request) throws Exception {
        // Create directory if not exists
        File certDir = new File(certificateStoragePath, result.getCertificateId().toString());
        certDir.mkdirs();
        
        // Save original PFX file
        try (FileOutputStream pfxOut = new FileOutputStream(new File(certDir, "certificate.pfx"))) {
            pfxOut.write(pfxFile.getBytes());
        }
        
        // Save certificate as PEM
        try (FileOutputStream certOut = new FileOutputStream(new File(certDir, "certificate.pem"))) {
            certOut.write(result.getCertificatePem().getBytes());
        }
        
        // Save private key as PEM
        try (FileOutputStream keyOut = new FileOutputStream(new File(certDir, "private-key.pem"))) {
            keyOut.write(result.getPrivateKeyPem().getBytes());
        }
    }
    
    /**
     * Load certificate from file
     */
    private X509Certificate loadCertificateFromFile(CertificateInfo certInfo) throws Exception {
        File certFile = new File(certificateStoragePath, certInfo.getId().toString() + "/certificate.pem");
        
        try (FileInputStream certIn = new FileInputStream(certFile)) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(certIn);
        }
    }
    
    /**
     * Load trusted certificate
     */
    private X509Certificate loadTrustedCertificate(TrustedCertificate trustedCert) throws Exception {
        File certFile = new File(certificateStoragePath, "trusted/" + trustedCert.getId().toString() + ".pem");
        
        try (FileInputStream certIn = new FileInputStream(certFile)) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(certIn);
        }
    }
    
    /**
     * Delete certificate from filesystem
     */
    private void deleteCertificateFromFileSystem(CertificateInfo certInfo) {
        File certDir = new File(certificateStoragePath, certInfo.getId().toString());
        if (certDir.exists()) {
            deleteDirectory(certDir);
        }
    }
    
    /**
     * Delete directory recursively
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}