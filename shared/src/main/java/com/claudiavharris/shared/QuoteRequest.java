package com.claudiavharris.shared;

public class QuoteRequest {
    private int age;
    private int years;
    private boolean accidents;

    // Default constructor
    public QuoteRequest() {}

    // Getters and setters
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getYears() { return years; }
    public void setYears(int years) { this.years = years; }
    public boolean isAccidents() { return accidents; }
    public void setAccidents(boolean accidents) { this.accidents = accidents; }
}