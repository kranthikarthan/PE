package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.dto.certificate.*;
import com.paymentengine.middleware.entity.CertificateInfo;
import com.paymentengine.middleware.exception.CertificateException;
import com.paymentengine.middleware.service.CertificateManagementService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Certificate Management Controller
 * 
 * REST API for certificate and key management including
 * generation, import, validation, and management operations.
 */
@RestController
@RequestMapping("/api/v1/certificates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CertificateManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateManagementController.class);
    
    @Autowired
    private CertificateManagementService certificateManagementService;
    
    /**
     * Generate a new certificate and private key pair
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('certificate:create')")
    @Timed(value = "certificate.generate", description = "Time taken to generate a certificate")
    public ResponseEntity<Map<String, Object>> generateCertificate(
            @Valid @RequestBody CertificateGenerationRequest request) {
        
        logger.info("Generating certificate for subject: {}", request.getSubjectDN());
        
        try {
            CertificateGenerationResult result = certificateManagementService.generateCertificate(request);
            
            logger.info("Certificate generated successfully: {}", result.getCertificateId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("certificate", result));
            
        } catch (CertificateException e) {
            logger.warn("Certificate generation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error generating certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Import a .pfx certificate file
     */
    @PostMapping("/import/pfx")
    @PreAuthorize("hasAuthority('certificate:import')")
    @Timed(value = "certificate.import.pfx", description = "Time taken to import PFX certificate")
    public ResponseEntity<Map<String, Object>> importPfxCertificate(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute PfxImportRequest request) {
        
        logger.info("Importing PFX certificate: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "INVALID_FILE",
                        "message", "PFX file is empty"
                    ));
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pfx") && 
                !file.getOriginalFilename().toLowerCase().endsWith(".p12")) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "INVALID_FILE_TYPE",
                        "message", "File must be a .pfx or .p12 certificate file"
                    ));
            }
            
            PfxImportResult result = certificateManagementService.importPfxCertificate(file, request);
            
            logger.info("PFX certificate imported successfully: {}", result.getCertificateId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("certificate", result));
            
        } catch (CertificateException e) {
            logger.warn("PFX import error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error importing PFX certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Validate a certificate
     */
    @PostMapping("/{certificateId}/validate")
    @PreAuthorize("hasAuthority('certificate:validate')")
    @Timed(value = "certificate.validate", description = "Time taken to validate a certificate")
    public ResponseEntity<Map<String, Object>> validateCertificate(@PathVariable String certificateId) {
        
        logger.info("Validating certificate: {}", certificateId);
        
        try {
            CertificateValidationResult result = certificateManagementService.validateCertificate(certificateId);
            
            logger.info("Certificate validation completed: {}", result.getStatus());
            return ResponseEntity.ok(Map.of("validation", result));
            
        } catch (CertificateException e) {
            logger.warn("Certificate validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error validating certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Get all certificates with optional filtering
     */
    @GetMapping
    @PreAuthorize("hasAuthority('certificate:read')")
    @Timed(value = "certificate.list", description = "Time taken to list certificates")
    public ResponseEntity<List<CertificateInfo>> getAllCertificates(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String validFrom,
            @RequestParam(required = false) String validTo) {
        
        logger.debug("Getting certificates with filters - tenantId: {}, type: {}, status: {}", 
                    tenantId, certificateType, status);
        
        try {
            CertificateFilter filter = new CertificateFilter();
            filter.setTenantId(tenantId);
            filter.setCertificateType(certificateType);
            filter.setStatus(status);
            
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
    @PreAuthorize("hasAuthority('certificate:read')")
    @Timed(value = "certificate.get", description = "Time taken to get a certificate")
    public ResponseEntity<CertificateInfo> getCertificate(@PathVariable String certificateId) {
        
        logger.debug("Getting certificate by ID: {}", certificateId);
        
        try {
            CertificateInfo certificate = certificateManagementService.getCertificateById(certificateId);
            return ResponseEntity.ok(certificate);
            
        } catch (CertificateException e) {
            logger.warn("Certificate not found: {}", certificateId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete certificate
     */
    @DeleteMapping("/{certificateId}")
    @PreAuthorize("hasAuthority('certificate:delete')")
    @Timed(value = "certificate.delete", description = "Time taken to delete a certificate")
    public ResponseEntity<Map<String, Object>> deleteCertificate(@PathVariable String certificateId) {
        
        logger.info("Deleting certificate: {}", certificateId);
        
        try {
            certificateManagementService.deleteCertificate(certificateId);
            
            logger.info("Certificate deleted successfully: {}", certificateId);
            return ResponseEntity.ok(Map.of(
                "message", "Certificate deleted successfully",
                "certificateId", certificateId
            ));
            
        } catch (CertificateException e) {
            logger.warn("Certificate deletion error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error deleting certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Rotate certificate
     */
    @PostMapping("/{certificateId}/rotate")
    @PreAuthorize("hasAuthority('certificate:rotate')")
    @Timed(value = "certificate.rotate", description = "Time taken to rotate a certificate")
    public ResponseEntity<Map<String, Object>> rotateCertificate(
            @PathVariable String certificateId,
            @Valid @RequestBody CertificateGenerationRequest request) {
        
        logger.info("Rotating certificate: {}", certificateId);
        
        try {
            CertificateGenerationResult result = certificateManagementService.rotateCertificate(certificateId, request);
            
            logger.info("Certificate rotated successfully: {} -> {}", certificateId, result.getCertificateId());
            return ResponseEntity.ok(Map.of(
                "message", "Certificate rotated successfully",
                "oldCertificateId", certificateId,
                "newCertificate", result
            ));
            
        } catch (CertificateException e) {
            logger.warn("Certificate rotation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error rotating certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Renew certificate
     */
    @PostMapping("/{certificateId}/renew")
    @PreAuthorize("hasAuthority('certificate:renew')")
    @Timed(value = "certificate.renew", description = "Time taken to renew a certificate")
    public ResponseEntity<Map<String, Object>> renewCertificate(
            @PathVariable String certificateId,
            @Valid @RequestBody CertificateGenerationRequest request) {
        
        logger.info("Renewing certificate: {}", certificateId);
        
        try {
            CertificateGenerationResult result = certificateManagementService.renewCertificate(certificateId, request);
            
            logger.info("Certificate renewed successfully: {} -> {}", certificateId, result.getCertificateId());
            return ResponseEntity.ok(Map.of(
                "message", "Certificate renewed successfully",
                "oldCertificateId", certificateId,
                "newCertificate", result
            ));
            
        } catch (CertificateException e) {
            logger.warn("Certificate renewal error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error renewing certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Rollback certificate
     */
    @PostMapping("/{certificateId}/rollback")
    @PreAuthorize("hasAuthority('certificate:rollback')")
    @Timed(value = "certificate.rollback", description = "Time taken to rollback a certificate")
    public ResponseEntity<Map<String, Object>> rollbackCertificate(
            @PathVariable String certificateId) {
        
        logger.info("Rolling back certificate: {}", certificateId);
        
        try {
            CertificateRollbackResult result = certificateManagementService.rollbackCertificate(certificateId);
            
            logger.info("Certificate rolled back successfully: {} -> {}", certificateId, result.getPreviousCertificateId());
            return ResponseEntity.ok(Map.of(
                "message", "Certificate rolled back successfully",
                "currentCertificateId", result.getCurrentCertificateId(),
                "previousCertificateId", result.getPreviousCertificateId(),
                "rollbackTimestamp", result.getRollbackTimestamp()
            ));
            
        } catch (CertificateException e) {
            logger.warn("Certificate rollback error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getErrorCode(),
                    "message", e.getMessage(),
                    "certificateId", e.getCertificateId() != null ? e.getCertificateId() : ""
                ));
        } catch (Exception e) {
            logger.error("Unexpected error rolling back certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Get certificates expiring soon
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('certificate:read')")
    @Timed(value = "certificate.expiring", description = "Time taken to get expiring certificates")
    public ResponseEntity<List<CertificateInfo>> getExpiringCertificates(
            @RequestParam(defaultValue = "30") int daysAhead) {
        
        logger.debug("Getting certificates expiring within {} days", daysAhead);
        
        try {
            List<CertificateInfo> certificates = certificateManagementService.getExpiringCertificates(daysAhead);
            return ResponseEntity.ok(certificates);
            
        } catch (Exception e) {
            logger.error("Error getting expiring certificates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = Map.of(
            "status", "UP",
            "service", "certificate-management",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(status);
    }
}