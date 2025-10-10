package com.paymentengine.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Set<String> permissions;
    private boolean active;
    private LocalDateTime lastLoginAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(UUID id,
                               String username,
                               String email,
                               String firstName,
                               String lastName,
                               Set<String> roles,
                               Set<String> permissions,
                               boolean active,
                               LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.permissions = permissions;
        this.active = active;
        this.lastLoginAt = lastLoginAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
