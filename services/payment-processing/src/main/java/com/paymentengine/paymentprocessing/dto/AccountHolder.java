package com.paymentengine.paymentprocessing.dto;

public class AccountHolder {
    private String id;
    private String name;
    // ... add more fields as needed

    public AccountHolder() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
