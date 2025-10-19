package com.payments.domain.valueobjects;

/**
 * Value object for payment priority
 */
public enum Priority {
    LOW("Low Priority"),
    NORMAL("Normal Priority"),
    HIGH("High Priority"),
    URGENT("Urgent Priority");
    
    private final String description;
    
    Priority(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
