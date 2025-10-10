package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@JsonTypeName("CUSTOMER_CREATED")
public class CustomerCreatedEvent extends PaymentEvent {
    
    @NotNull
    private UUID customerId;
    
    @NotNull
    private String customerNumber;
    
    @NotNull
    private String firstName;
    
    @NotNull
    private String lastName;
    
    @NotNull
    private String email;
    
    private String phone;
    
    private LocalDate dateOfBirth;
    
    private String status;
    
    private String kycStatus;

    public CustomerCreatedEvent() {
        super("CUSTOMER_CREATED", "core-banking");
    }

    public CustomerCreatedEvent(UUID customerId, String customerNumber, String firstName, 
                              String lastName, String email) {
        this();
        this.customerId = customerId;
        this.customerNumber = customerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = "ACTIVE";
        this.kycStatus = "PENDING";
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }
}