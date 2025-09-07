package com.paymentengine.paymentprocessing.exception;

/**
 * Certificate Exception
 * 
 * Exception for certificate-related errors including generation,
 * import, validation, and management operations.
 */
public class CertificateException extends RuntimeException {
    
    private final String errorCode;
    private final String certificateId;
    
    public CertificateException(String message) {
        super(message);
        this.errorCode = "CERTIFICATE_ERROR";
        this.certificateId = null;
    }
    
    public CertificateException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.certificateId = null;
    }
    
    public CertificateException(String message, String errorCode, String certificateId) {
        super(message);
        this.errorCode = errorCode;
        this.certificateId = certificateId;
    }
    
    public CertificateException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CERTIFICATE_ERROR";
        this.certificateId = null;
    }
    
    public CertificateException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.certificateId = null;
    }
    
    public CertificateException(String message, String errorCode, String certificateId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.certificateId = certificateId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getCertificateId() {
        return certificateId;
    }
}