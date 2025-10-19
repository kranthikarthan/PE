package com.payments.batch.sftp;

/**
 * Enumeration of supported SFTP authentication methods.
 *
 * <p>This enum defines the different authentication methods supported
 * by the SFTP client, allowing for flexible security configurations
 * based on organizational requirements.
 *
 * @since PE-403
 */
public enum SftpAuthMethod {
  
  /**
   * No authentication (not recommended for production).
   */
  NONE("none"),
  
  /**
   * Password-based authentication.
   * 
   * <p>Uses username and password for authentication. Suitable for
   * development and testing environments.
   */
  PASSWORD("password"),
  
  /**
   * SSH key-based authentication.
   * 
   * <p>Uses SSH private key for authentication. More secure than
   * password authentication and suitable for production environments.
   */
  KEY("publickey"),
  
  /**
   * Certificate-based authentication.
   * 
   * <p>Uses X.509 certificates for authentication. Most secure method
   * and suitable for enterprise environments.
   */
  CERTIFICATE("certificate");
  
  private final String methodName;
  
  SftpAuthMethod(String methodName) {
    this.methodName = methodName;
  }
  
  /**
   * Gets the authentication method name.
   *
   * @return the method name
   */
  public String getMethodName() {
    return methodName;
  }
  
  /**
   * Gets the authentication method from its name.
   *
   * @param methodName the method name
   * @return the authentication method, or null if not found
   */
  public static SftpAuthMethod fromMethodName(String methodName) {
    for (SftpAuthMethod method : values()) {
      if (method.methodName.equals(methodName)) {
        return method;
      }
    }
    return null;
  }
  
  /**
   * Checks if this authentication method requires credentials.
   *
   * @return true if credentials are required, false otherwise
   */
  public boolean requiresCredentials() {
    return this != NONE;
  }
  
  /**
   * Checks if this authentication method is secure.
   *
   * @return true if secure, false otherwise
   */
  public boolean isSecure() {
    return this == KEY || this == CERTIFICATE;
  }
}
