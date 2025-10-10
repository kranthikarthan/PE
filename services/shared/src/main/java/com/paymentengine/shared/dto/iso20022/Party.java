package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * ISO 20022 Party Information (Debtor/Creditor)
 */
public class Party {
    
    @JsonProperty("Nm")
    @Size(max = 140)
    private String name;
    
    @JsonProperty("PstlAdr")
    @Valid
    private PostalAddress postalAddress;
    
    @JsonProperty("Id")
    @Valid
    private PartyIdentification identification;
    
    @JsonProperty("CtryOfRes")
    @Size(min = 2, max = 2)
    private String countryOfResidence;
    
    @JsonProperty("CtctDtls")
    @Valid
    private ContactDetails contactDetails;
    
    public Party() {}
    
    public Party(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public PostalAddress getPostalAddress() {
        return postalAddress;
    }
    
    public void setPostalAddress(PostalAddress postalAddress) {
        this.postalAddress = postalAddress;
    }
    
    public PartyIdentification getIdentification() {
        return identification;
    }
    
    public void setIdentification(PartyIdentification identification) {
        this.identification = identification;
    }
    
    public String getCountryOfResidence() {
        return countryOfResidence;
    }
    
    public void setCountryOfResidence(String countryOfResidence) {
        this.countryOfResidence = countryOfResidence;
    }
    
    public ContactDetails getContactDetails() {
        return contactDetails;
    }
    
    public void setContactDetails(ContactDetails contactDetails) {
        this.contactDetails = contactDetails;
    }
    
    /**
     * Postal Address
     */
    public static class PostalAddress {
        
        @JsonProperty("AdrTp")
        @Size(max = 4)
        private String addressType; // ADDR, PBOX, HOME, BIZZ
        
        @JsonProperty("Dept")
        @Size(max = 70)
        private String department;
        
        @JsonProperty("SubDept")
        @Size(max = 70)
        private String subDepartment;
        
        @JsonProperty("StrtNm")
        @Size(max = 70)
        private String streetName;
        
        @JsonProperty("BldgNb")
        @Size(max = 16)
        private String buildingNumber;
        
        @JsonProperty("PstCd")
        @Size(max = 16)
        private String postCode;
        
        @JsonProperty("TwnNm")
        @Size(max = 35)
        private String townName;
        
        @JsonProperty("CtrySubDvsn")
        @Size(max = 35)
        private String countrySubDivision;
        
        @JsonProperty("Ctry")
        @Size(min = 2, max = 2)
        private String country;
        
        @JsonProperty("AdrLine")
        private String[] addressLine; // Max 7 lines, 70 chars each
        
        public PostalAddress() {}
        
        // Getters and Setters
        public String getAddressType() {
            return addressType;
        }
        
        public void setAddressType(String addressType) {
            this.addressType = addressType;
        }
        
        public String getDepartment() {
            return department;
        }
        
        public void setDepartment(String department) {
            this.department = department;
        }
        
        public String getSubDepartment() {
            return subDepartment;
        }
        
        public void setSubDepartment(String subDepartment) {
            this.subDepartment = subDepartment;
        }
        
        public String getStreetName() {
            return streetName;
        }
        
        public void setStreetName(String streetName) {
            this.streetName = streetName;
        }
        
        public String getBuildingNumber() {
            return buildingNumber;
        }
        
        public void setBuildingNumber(String buildingNumber) {
            this.buildingNumber = buildingNumber;
        }
        
        public String getPostCode() {
            return postCode;
        }
        
        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }
        
        public String getTownName() {
            return townName;
        }
        
        public void setTownName(String townName) {
            this.townName = townName;
        }
        
        public String getCountrySubDivision() {
            return countrySubDivision;
        }
        
        public void setCountrySubDivision(String countrySubDivision) {
            this.countrySubDivision = countrySubDivision;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String[] getAddressLine() {
            return addressLine;
        }
        
        public void setAddressLine(String[] addressLine) {
            this.addressLine = addressLine;
        }
    }
    
    /**
     * Party Identification
     */
    public static class PartyIdentification {
        
        @JsonProperty("OrgId")
        @Valid
        private OrganisationIdentification organisationIdentification;
        
        @JsonProperty("PrvtId")
        @Valid
        private PersonIdentification privateIdentification;
        
        public PartyIdentification() {}
        
        public OrganisationIdentification getOrganisationIdentification() {
            return organisationIdentification;
        }
        
        public void setOrganisationIdentification(OrganisationIdentification organisationIdentification) {
            this.organisationIdentification = organisationIdentification;
        }
        
        public PersonIdentification getPrivateIdentification() {
            return privateIdentification;
        }
        
        public void setPrivateIdentification(PersonIdentification privateIdentification) {
            this.privateIdentification = privateIdentification;
        }
    }
    
    /**
     * Organisation Identification
     */
    public static class OrganisationIdentification {
        
        @JsonProperty("AnyBIC")
        @Size(min = 8, max = 11)
        private String anyBIC;
        
        @JsonProperty("LEI")
        @Size(min = 20, max = 20)
        private String legalEntityIdentifier;
        
        @JsonProperty("Othr")
        @Valid
        private CommonTypes.GenericOrganisationIdentification[] other;
        
        public OrganisationIdentification() {}
        
        // Getters and Setters
        public String getAnyBIC() {
            return anyBIC;
        }
        
        public void setAnyBIC(String anyBIC) {
            this.anyBIC = anyBIC;
        }
        
        public String getLegalEntityIdentifier() {
            return legalEntityIdentifier;
        }
        
        public void setLegalEntityIdentifier(String legalEntityIdentifier) {
            this.legalEntityIdentifier = legalEntityIdentifier;
        }
        
        public CommonTypes.GenericOrganisationIdentification[] getOther() {
            return other;
        }
        
        public void setOther(CommonTypes.GenericOrganisationIdentification[] other) {
            this.other = other;
        }
    }
    
    /**
     * Person Identification
     */
    public static class PersonIdentification {
        
        @JsonProperty("DtAndPlcOfBirth")
        @Valid
        private CommonTypes.DateAndPlaceOfBirth dateAndPlaceOfBirth;
        
        @JsonProperty("Othr")
        @Valid
        private CommonTypes.GenericPersonIdentification[] other;
        
        public PersonIdentification() {}
        
        // Getters and Setters
        public CommonTypes.DateAndPlaceOfBirth getDateAndPlaceOfBirth() {
            return dateAndPlaceOfBirth;
        }
        
        public void setDateAndPlaceOfBirth(CommonTypes.DateAndPlaceOfBirth dateAndPlaceOfBirth) {
            this.dateAndPlaceOfBirth = dateAndPlaceOfBirth;
        }
        
        public CommonTypes.GenericPersonIdentification[] getOther() {
            return other;
        }
        
        public void setOther(CommonTypes.GenericPersonIdentification[] other) {
            this.other = other;
        }
    }
    
    /**
     * Contact Details
     */
    public static class ContactDetails {
        
        @JsonProperty("NmPrfx")
        @Size(max = 4)
        private String namePrefix;
        
        @JsonProperty("Nm")
        @Size(max = 140)
        private String name;
        
        @JsonProperty("PhneNb")
        @Size(max = 35)
        private String phoneNumber;
        
        @JsonProperty("MobNb")
        @Size(max = 35)
        private String mobileNumber;
        
        @JsonProperty("FaxNb")
        @Size(max = 35)
        private String faxNumber;
        
        @JsonProperty("EmailAdr")
        @Size(max = 256)
        private String emailAddress;
        
        @JsonProperty("Othr")
        @Size(max = 35)
        private String other;
        
        public ContactDetails() {}
        
        // Getters and Setters
        public String getNamePrefix() {
            return namePrefix;
        }
        
        public void setNamePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getMobileNumber() {
            return mobileNumber;
        }
        
        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }
        
        public String getFaxNumber() {
            return faxNumber;
        }
        
        public void setFaxNumber(String faxNumber) {
            this.faxNumber = faxNumber;
        }
        
        public String getEmailAddress() {
            return emailAddress;
        }
        
        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }
        
        public String getOther() {
            return other;
        }
        
        public void setOther(String other) {
            this.other = other;
        }
    }
}