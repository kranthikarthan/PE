package com.payments.samosadapter.service;

import com.payments.config.SecretManager;
import com.payments.domain.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * SAMOS Secret Management Service
 *
 * <p>Service for managing SAMOS adapter secrets and secure configuration: - API keys management - OAuth2 credentials - Encryption keys - Secure configuration storage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosSecretManagementService {

  private final SecretManager secretManager;

  /**
   * Get SAMOS API key for tenant
   *
   * @param tenantContext Tenant context
   * @return API key
   */
  public String getApiKey(TenantContext tenantContext) {
    String key = "SAMOS_API_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getEncryptedSecret(key);
  }

  /**
   * Get SAMOS API key with default
   *
   * @param tenantContext Tenant context
   * @param defaultValue Default value
   * @return API key
   */
  public String getApiKey(TenantContext tenantContext, String defaultValue) {
    String key = "SAMOS_API_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getEncryptedSecret(key, defaultValue);
  }

  /**
   * Store SAMOS API key
   *
   * @param tenantContext Tenant context
   * @param apiKey API key
   */
  public void storeApiKey(TenantContext tenantContext, String apiKey) {
    String key = "SAMOS_API_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    secretManager.storeSecret(key, apiKey);
    log.info("Stored SAMOS API key for tenant: {} and business unit: {}", 
             tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
  }

  /**
   * Get SAMOS OAuth2 client ID
   *
   * @param tenantContext Tenant context
   * @return OAuth2 client ID
   */
  public String getOAuth2ClientId(TenantContext tenantContext) {
    String key = "SAMOS_OAUTH2_CLIENT_ID_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getEncryptedSecret(key);
  }

  /**
   * Get SAMOS OAuth2 client secret
   *
   * @param tenantContext Tenant context
   * @return OAuth2 client secret
   */
  public String getOAuth2ClientSecret(TenantContext tenantContext) {
    String key = "SAMOS_OAUTH2_CLIENT_SECRET_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getEncryptedSecret(key);
  }

  /**
   * Store SAMOS OAuth2 credentials
   *
   * @param tenantContext Tenant context
   * @param clientId OAuth2 client ID
   * @param clientSecret OAuth2 client secret
   */
  public void storeOAuth2Credentials(TenantContext tenantContext, String clientId, String clientSecret) {
    String clientIdKey = "SAMOS_OAUTH2_CLIENT_ID_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    String clientSecretKey = "SAMOS_OAUTH2_CLIENT_SECRET_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    
    secretManager.storeSecret(clientIdKey, clientId);
    secretManager.storeSecret(clientSecretKey, clientSecret);
    
    log.info("Stored SAMOS OAuth2 credentials for tenant: {} and business unit: {}", 
             tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
  }

  /**
   * Get SAMOS encryption key
   *
   * @param tenantContext Tenant context
   * @return Encryption key
   */
  public String getEncryptionKey(TenantContext tenantContext) {
    String key = "SAMOS_ENCRYPTION_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getEncryptedSecret(key);
  }

  /**
   * Store SAMOS encryption key
   *
   * @param tenantContext Tenant context
   * @param encryptionKey Encryption key
   */
  public void storeEncryptionKey(TenantContext tenantContext, String encryptionKey) {
    String key = "SAMOS_ENCRYPTION_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    secretManager.storeSecret(key, encryptionKey);
    log.info("Stored SAMOS encryption key for tenant: {} and business unit: {}", 
             tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
  }

  /**
   * Get SAMOS endpoint URL
   *
   * @param tenantContext Tenant context
   * @return Endpoint URL
   */
  public String getEndpointUrl(TenantContext tenantContext) {
    String key = "SAMOS_ENDPOINT_URL_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getSecret(key);
  }

  /**
   * Get SAMOS endpoint URL with default
   *
   * @param tenantContext Tenant context
   * @param defaultValue Default value
   * @return Endpoint URL
   */
  public String getEndpointUrl(TenantContext tenantContext, String defaultValue) {
    String key = "SAMOS_ENDPOINT_URL_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getSecret(key, defaultValue);
  }

  /**
   * Store SAMOS endpoint URL
   *
   * @param tenantContext Tenant context
   * @param endpointUrl Endpoint URL
   */
  public void storeEndpointUrl(TenantContext tenantContext, String endpointUrl) {
    String key = "SAMOS_ENDPOINT_URL_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    secretManager.storePlainSecret(key, endpointUrl);
    log.info("Stored SAMOS endpoint URL for tenant: {} and business unit: {}", 
             tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
  }

  /**
   * Get SAMOS certificate
   *
   * @param tenantContext Tenant context
   * @return Certificate
   */
  public String getCertificate(TenantContext tenantContext) {
    String key = "SAMOS_CERTIFICATE_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.getEncryptedSecret(key);
  }

  /**
   * Store SAMOS certificate
   *
   * @param tenantContext Tenant context
   * @param certificate Certificate
   */
  public void storeCertificate(TenantContext tenantContext, String certificate) {
    String key = "SAMOS_CERTIFICATE_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    secretManager.storeSecret(key, certificate);
    log.info("Stored SAMOS certificate for tenant: {} and business unit: {}", 
             tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
  }

  /**
   * Get all SAMOS secrets for tenant
   *
   * @param tenantContext Tenant context
   * @return Map of secrets
   */
  public Map<String, String> getAllSecrets(TenantContext tenantContext) {
    Map<String, String> secrets = new HashMap<>();
    
    String apiKey = getApiKey(tenantContext);
    if (apiKey != null) {
      secrets.put("apiKey", apiKey);
    }
    
    String oauth2ClientId = getOAuth2ClientId(tenantContext);
    if (oauth2ClientId != null) {
      secrets.put("oauth2ClientId", oauth2ClientId);
    }
    
    String oauth2ClientSecret = getOAuth2ClientSecret(tenantContext);
    if (oauth2ClientSecret != null) {
      secrets.put("oauth2ClientSecret", oauth2ClientSecret);
    }
    
    String encryptionKey = getEncryptionKey(tenantContext);
    if (encryptionKey != null) {
      secrets.put("encryptionKey", encryptionKey);
    }
    
    String endpointUrl = getEndpointUrl(tenantContext);
    if (endpointUrl != null) {
      secrets.put("endpointUrl", endpointUrl);
    }
    
    String certificate = getCertificate(tenantContext);
    if (certificate != null) {
      secrets.put("certificate", certificate);
    }
    
    return secrets;
  }

  /**
   * Check if SAMOS secrets exist for tenant
   *
   * @param tenantContext Tenant context
   * @return True if secrets exist
   */
  public boolean hasSecrets(TenantContext tenantContext) {
    String apiKeyKey = "SAMOS_API_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    return secretManager.hasSecret(apiKeyKey);
  }

  /**
   * Remove all SAMOS secrets for tenant
   *
   * @param tenantContext Tenant context
   */
  public void removeAllSecrets(TenantContext tenantContext) {
    String apiKeyKey = "SAMOS_API_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    String oauth2ClientIdKey = "SAMOS_OAUTH2_CLIENT_ID_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    String oauth2ClientSecretKey = "SAMOS_OAUTH2_CLIENT_SECRET_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    String encryptionKeyKey = "SAMOS_ENCRYPTION_KEY_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    String endpointUrlKey = "SAMOS_ENDPOINT_URL_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    String certificateKey = "SAMOS_CERTIFICATE_" + tenantContext.getTenantId() + "_" + tenantContext.getBusinessUnitId();
    
    secretManager.removeSecret(apiKeyKey);
    secretManager.removeSecret(oauth2ClientIdKey);
    secretManager.removeSecret(oauth2ClientSecretKey);
    secretManager.removeSecret(encryptionKeyKey);
    secretManager.removeSecret(endpointUrlKey);
    secretManager.removeSecret(certificateKey);
    
    log.info("Removed all SAMOS secrets for tenant: {} and business unit: {}", 
             tenantContext.getTenantId(), tenantContext.getBusinessUnitId());
  }
}
