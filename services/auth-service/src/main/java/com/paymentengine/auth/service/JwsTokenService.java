package com.paymentengine.auth.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * JWS (JSON Web Signature) Token Service
 * 
 * Provides JWS token generation and validation as an alternative to JWT.
 * Supports both HMAC and RSA signing algorithms.
 */
@Service
public class JwsTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwsTokenService.class);
    
    @Value("${app.jws.secret:myJwsSecretKey}")
    private String jwsSecret;
    
    @Value("${app.jws.expiration:3600}")
    private int jwsExpiration;
    
    @Value("${app.jws.refresh-expiration:86400}")
    private int refreshExpiration;
    
    @Value("${app.jws.issuer:payment-engine-auth}")
    private String jwsIssuer;
    
    @Value("${app.jws.algorithm:HS256}")
    private String jwsAlgorithm;
    
    @Value("${app.jws.key-size:2048}")
    private int rsaKeySize;
    
    private JWSSigner signer;
    private JWSVerifier verifier;
    private RSAKey rsaKey;
    
    @PostConstruct
    public void initialize() {
        try {
            if ("HS256".equals(jwsAlgorithm) || "HS384".equals(jwsAlgorithm) || "HS512".equals(jwsAlgorithm)) {
                // HMAC-based signing
                JWSAlgorithm algorithm = JWSAlgorithm.parse(jwsAlgorithm);
                signer = new MACSigner(jwsSecret.getBytes());
                verifier = new MACVerifier(jwsSecret.getBytes());
                logger.info("Initialized JWS service with HMAC algorithm: {}", algorithm);
            } else if ("RS256".equals(jwsAlgorithm) || "RS384".equals(jwsAlgorithm) || "RS512".equals(jwsAlgorithm)) {
                // RSA-based signing
                JWSAlgorithm algorithm = JWSAlgorithm.parse(jwsAlgorithm);
                rsaKey = generateRSAKey();
                signer = new RSASSASigner(rsaKey.toPrivateKey());
                verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
                logger.info("Initialized JWS service with RSA algorithm: {}", algorithm);
            } else {
                throw new IllegalArgumentException("Unsupported JWS algorithm: " + jwsAlgorithm);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize JWS service", e);
            throw new RuntimeException("JWS service initialization failed", e);
        }
    }
    
    /**
     * Generate access token using JWS
     */
    public String generateAccessToken(UUID userId, String username, String email, Set<String> roles, Set<String> permissions) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId.toString());
            claims.put("username", username);
            claims.put("email", email);
            claims.put("roles", roles);
            claims.put("permissions", permissions);
            claims.put("type", "access");
            
            return createJwsToken(claims, username, jwsExpiration);
        } catch (Exception e) {
            logger.error("Failed to generate JWS access token", e);
            throw new RuntimeException("JWS access token generation failed", e);
        }
    }
    
    /**
     * Generate refresh token using JWS
     */
    public String generateRefreshToken(UUID userId, String username) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId.toString());
            claims.put("username", username);
            claims.put("type", "refresh");
            
            return createJwsToken(claims, username, refreshExpiration);
        } catch (Exception e) {
            logger.error("Failed to generate JWS refresh token", e);
            throw new RuntimeException("JWS refresh token generation failed", e);
        }
    }
    
    /**
     * Create JWS token with claims
     */
    private String createJwsToken(Map<String, Object> claims, String subject, int expirationSeconds) throws JOSEException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusSeconds(expirationSeconds);
        
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(jwsIssuer)
                .audience("payment-engine-api")
                .issueTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .jwtID(UUID.randomUUID().toString());
        
        // Add custom claims
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            claimsBuilder.claim(entry.getKey(), entry.getValue());
        }
        
        JWTClaimsSet jwtClaims = claimsBuilder.build();
        
        // Create JWS header
        JWSHeader header = new JWSHeader(JWSAlgorithm.parse(jwsAlgorithm));
        
        // Create signed JWT
        SignedJWT signedJWT = new SignedJWT(header, jwtClaims);
        signedJWT.sign(signer);
        
        return signedJWT.serialize();
    }
    
    /**
     * Validate JWS token
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.verify(verifier) && !isTokenExpired(signedJWT);
        } catch (Exception e) {
            logger.debug("JWS token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get claims from JWS token
     */
    public JWTClaimsSet getClaimsFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid JWS signature");
            }
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            logger.error("Failed to parse JWS token", e);
            throw new RuntimeException("JWS token parsing failed", e);
        }
    }
    
    /**
     * Extract username from JWS token
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * Extract user ID from JWS token
     */
    public UUID getUserIdFromToken(String token) {
        try {
            String userIdStr = getClaimsFromToken(token).getStringClaim("userId");
            return UUID.fromString(userIdStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract userId from token", e);
        }
    }
    
    /**
     * Extract email from JWS token
     */
    public String getEmailFromToken(String token) {
        try {
            return getClaimsFromToken(token).getStringClaim("email");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract email from token", e);
        }
    }
    
    /**
     * Extract roles from JWS token
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        return (Set<String>) getClaimsFromToken(token).getClaim("roles");
    }
    
    /**
     * Extract permissions from JWS token
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPermissionsFromToken(String token) {
        return (Set<String>) getClaimsFromToken(token).getClaim("permissions");
    }
    
    /**
     * Get token type from JWS token
     */
    public String getTokenType(String token) {
        try {
            return getClaimsFromToken(token).getStringClaim("type");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract token type", e);
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return isTokenExpired(signedJWT);
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Check if signed JWT is expired
     */
    private boolean isTokenExpired(SignedJWT signedJWT) {
        try {
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Check if token is access token
     */
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if token is refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get token expiration time
     */
    public long getExpirationTimeFromToken(String token) {
        return getClaimsFromToken(token).getExpirationTime().getTime();
    }
    
    /**
     * Get time until token expiration
     */
    public long getTimeUntilExpiration(String token) {
        long expirationTime = getExpirationTimeFromToken(token);
        long currentTime = System.currentTimeMillis();
        return expirationTime - currentTime;
    }
    
    /**
     * Get public key for RSA-based JWS (for external verification)
     */
    public String getPublicKey() {
        if (rsaKey != null) {
            return rsaKey.toPublicJWK().toJSONString();
        }
        return null;
    }
    
    /**
     * Generate RSA key pair for JWS signing
     */
    private RSAKey generateRSAKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(rsaKeySize);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
    }
    
    /**
     * Get JWS algorithm being used
     */
    public String getJwsAlgorithm() {
        return jwsAlgorithm;
    }
    
    /**
     * Get JWS issuer
     */
    public String getJwsIssuer() {
        return jwsIssuer;
    }
}