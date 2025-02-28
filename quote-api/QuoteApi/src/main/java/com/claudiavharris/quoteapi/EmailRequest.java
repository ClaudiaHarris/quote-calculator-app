package com.claudiavharris.quoteapi;

public class EmailRequest {
    private String email;
    private PremiumDetails details;

    // Default constructor
    public EmailRequest() {}

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PremiumDetails getDetails() {
        return details;
    }

    public void setDetails(PremiumDetails details) {
        this.details = details;
    }
}