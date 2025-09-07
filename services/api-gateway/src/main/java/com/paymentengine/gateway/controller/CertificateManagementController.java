package com.paymentengine.gateway.controller;

import com.paymentengine.gateway.dto.certificate.*;
import com.paymentengine.gateway.entity.CertificateInfo;
import com.paymentengine.gateway.service.CertificateManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Certificate Management Controller for API Gateway
 * 
 * Provides REST endpoints for managing certificates and keys in the API Gateway service,
 * including SSL/TLS certificates for secure communication and client certificate validation.
 */
@RestController
@RequestMapping("/api/v1/gateway/certificates")
@CrossOrigin(origins = "*")
public class CertificateManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateManagementController.class);
    
    @Autowired
    private CertificateManagementService certificateManagementService;
    
    /**
     * Generate a new SSL/TLS certificate for the API Gateway
     */
    @PostMapping("/generate")
    public ResponseEntity<CertificateGenerationResult> generateCertificate(
            @Valid @RequestBody CertificateGenerationRequest request) {
        try {
            logger.info("Generating API Gateway certificate for subject: {}", request.getSubjectDN());
            CertificateGenerationResult result = certificateManagementService.generateGatewayCertificate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            logger.error("Error generating API Gateway certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Import a client certificate from PFX file
     */
    @PostMapping("/import/pfx")
    public ResponseEntity<PfxImportResult> importPfxCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @RequestParam(value = "tenantId", required = false) String tenantId,
            @RequestParam(value = "certificateType", required = false) String certificateType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "validateCertificate", defaultValue = "true") boolean validateCertificate,
            @RequestParam(value = "extractPrivateKey", defaultValue = "true") boolean extractPrivateKey,
            @RequestParam(value = "extractCertificateChain", defaultValue = "true") boolean extractCertificateChain) {
        try {
            logger.info("Importing client certificate: {}", file.getOriginalFilename());
            
            PfxImportRequest request = new PfxImportRequest();
            request.setPassword(password);
            request.setTenantId(tenantId);
            request.setCertificateType(certificateType);
            request.setDescription(description);
            request.setValidateCertificate(validateCertificate);
            request.setExtractPrivateKey(extractPrivateKey);
            request.setExtractCertificateChain(extractCertificateChain);
            
            PfxImportResult result = certificateManagementService.importClientCertificate(file, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            logger.error("Error importing client certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Validate a certificate
     */
    @PostMapping("/{certificateId}/validate")
    public ResponseEntity<CertificateValidationResult> validateCertificate(
            @PathVariable String certificateId) {
        try {
            logger.info("Validating certificate: {}", certificateId);
            CertificateValidationResult result = certificateManagementService.validateCertificate(certificateId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error validating certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Renew a certificate
     */
    @PostMapping("/{certificateId}/renew")
    public ResponseEntity<CertificateGenerationResult> renewCertificate(
            @PathVariable String certificateId,
            @Valid @RequestBody CertificateGenerationRequest request) {
        try {
            logger.info("Renewing certificate: {}", certificateId);
            CertificateGenerationResult result = certificateManagementService.renewCertificate(certificateId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error renewing certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Rollback a certificate to previous version
     */
    @PostMapping("/{certificateId}/rollback")
    public ResponseEntity<CertificateRollbackResult> rollbackCertificate(
            @PathVariable String certificateId) {
        try {
            logger.info("Rolling back certificate: {}", certificateId);
            CertificateRollbackResult result = certificateManagementService.rollbackCertificate(certificateId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error rolling back certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all certificates with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<CertificateInfo>> getAllCertificates(
            @RequestParam(value = "tenantId", required = false) String tenantId,
            @RequestParam(value = "certificateType", required = false) String certificateType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "validFrom", required = false) String validFrom,
            @RequestParam(value = "validTo", required = false) String validTo) {
        try {
            CertificateFilter filter = new CertificateFilter();
            filter.setTenantId(tenantId);
            filter.setCertificateType(certificateType);
            filter.setStatus(status);
            // Parse dates if provided
            
            List<CertificateInfo> certificates = certificateManagementService.getAllCertificates(filter);
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            logger.error("Error getting certificates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get certificate by ID
     */
    @GetMapping("/{certificateId}")
    public ResponseEntity<CertificateInfo> getCertificateById(
            @PathVariable String certificateId) {
        try {
            CertificateInfo certificate = certificateManagementService.getCertificateById(certificateId);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error getting certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Delete certificate
     */
    @DeleteMapping("/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(
            @PathVariable String certificateId) {
        try {
            logger.info("Deleting certificate: {}", certificateId);
            certificateManagementService.deleteCertificate(certificateId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get certificates expiring soon
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<CertificateInfo>> getExpiringCertificates(
            @RequestParam(value = "daysAhead", defaultValue = "30") int daysAhead) {
        try {
            List<CertificateInfo> certificates = certificateManagementService.getExpiringCertificates(daysAhead);
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            logger.error("Error getting expiring certificates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get certificate statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<CertificateStats> getCertificateStats() {
        try {
            // Get all certificates
            List<CertificateInfo> allCertificates = certificateManagementService.getAllCertificates(null);
            
            // Calculate statistics
            long totalCertificates = allCertificates.size();
            long activeCertificates = allCertificates.stream()
                .filter(cert -> "ACTIVE".equals(cert.getStatus()))
                .count();
            long expiredCertificates = allCertificates.stream()
                .filter(cert -> "EXPIRED".equals(cert.getStatus()))
                .count();
            long expiringSoon = certificateManagementService.getExpiringCertificates(30).size();
            
            CertificateStats stats = new CertificateStats();
            stats.setTotalCertificates(totalCertificates);
            stats.setActiveCertificates(activeCertificates);
            stats.setExpiredCertificates(expiredCertificates);
            stats.setExpiringSoon(expiringSoon);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting certificate statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}