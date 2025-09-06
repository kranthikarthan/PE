package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ISO 20022 Account Information
 */
public class Account {
    
    @JsonProperty("Id")
    @NotNull
    @Valid
    private AccountIdentification identification;
    
    @JsonProperty("Tp")
    @Valid
    private AccountType type;
    
    @JsonProperty("Ccy")
    @Size(min = 3, max = 3)
    private String currency;
    
    @JsonProperty("Nm")
    @Size(max = 70)
    private String name;
    
    @JsonProperty("Prxy")
    @Valid
    private ProxyAccountIdentification proxy;
    
    public Account() {}
    
    public Account(AccountIdentification identification) {
        this.identification = identification;
    }
    
    // Getters and Setters
    public AccountIdentification getIdentification() {
        return identification;
    }
    
    public void setIdentification(AccountIdentification identification) {
        this.identification = identification;
    }
    
    public AccountType getType() {
        return type;
    }
    
    public void setType(AccountType type) {
        this.type = type;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ProxyAccountIdentification getProxy() {
        return proxy;
    }
    
    public void setProxy(ProxyAccountIdentification proxy) {
        this.proxy = proxy;
    }
    
    /**
     * Account Identification
     */
    public static class AccountIdentification {
        
        @JsonProperty("IBAN")
        @Size(max = 34)
        private String iban;
        
        @JsonProperty("Othr")
        @Valid
        private GenericAccountIdentification other;
        
        public AccountIdentification() {}
        
        public AccountIdentification(String iban) {
            this.iban = iban;
        }
        
        public String getIban() {
            return iban;
        }
        
        public void setIban(String iban) {
            this.iban = iban;
        }
        
        public GenericAccountIdentification getOther() {
            return other;
        }
        
        public void setOther(GenericAccountIdentification other) {
            this.other = other;
        }
    }
    
    /**
     * Generic Account Identification
     */
    public static class GenericAccountIdentification {
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 34)
        private String identification;
        
        @JsonProperty("SchmeNm")
        @Valid
        private SchemeName schemeName;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        public GenericAccountIdentification() {}
        
        public GenericAccountIdentification(String identification) {
            this.identification = identification;
        }
        
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public SchemeName getSchemeName() {
            return schemeName;
        }
        
        public void setSchemeName(SchemeName schemeName) {
            this.schemeName = schemeName;
        }
        
        public String getIssuer() {
            return issuer;
        }
        
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
    
    /**
     * Account Type
     */
    public static class AccountType {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // CACC, CASH, CHAR, CISH, COMM, CPAC, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public AccountType() {}
        
        public AccountType(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getProprietary() {
            return proprietary;
        }
        
        public void setProprietary(String proprietary) {
            this.proprietary = proprietary;
        }
    }
    
    /**
     * Proxy Account Identification
     */
    public static class ProxyAccountIdentification {
        
        @JsonProperty("Tp")
        @Valid
        private ProxyAccountType type;
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 2048)
        private String identification;
        
        public ProxyAccountIdentification() {}
        
        public ProxyAccountType getType() {
            return type;
        }
        
        public void setType(ProxyAccountType type) {
            this.type = type;
        }
        
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
    }
    
    /**
     * Proxy Account Type
     */
    public static class ProxyAccountType {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // TELE, EMAL, DNAM, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public ProxyAccountType() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getProprietary() {
            return proprietary;
        }
        
        public void setProprietary(String proprietary) {
            this.proprietary = proprietary;
        }
    }
}