package com.claudiavharris.shared;

public class PremiumDetails {
    private double basePremium;
    private double fullPaymentDiscount;
    private double downPayment;
    private double remainingBalance;
    private double monthlyPayment;

    // Default constructor
    public PremiumDetails() {}

    public PremiumDetails(double basePremium, double fullPaymentDiscount, double downPayment, double remainingBalance, double monthlyPayment) {
        this.basePremium = basePremium;
        this.fullPaymentDiscount = fullPaymentDiscount;
        this.downPayment = downPayment;
        this.remainingBalance = remainingBalance;
        this.monthlyPayment = monthlyPayment;
    }

    // Getters and setters
    public double getBasePremium() { return basePremium; }
    public void setBasePremium(double basePremium) { this.basePremium = basePremium; }
    public double getFullPaymentDiscount() { return fullPaymentDiscount; }
    public void setFullPaymentDiscount(double fullPaymentDiscount) { this.fullPaymentDiscount = fullPaymentDiscount; }
    public double getDownPayment() { return downPayment; }
    public void setDownPayment(double downPayment) { this.downPayment = downPayment; }
    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(double monthlyPayment) { this.monthlyPayment = monthlyPayment; }
}