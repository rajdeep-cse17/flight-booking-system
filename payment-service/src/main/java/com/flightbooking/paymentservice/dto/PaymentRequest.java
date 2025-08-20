package com.flightbooking.paymentservice.dto;

public class PaymentRequest {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;
    private double amount;

    public PaymentRequest() {}

    public PaymentRequest(String cardNumber, String expiryDate, String cvv, String cardHolderName, double amount) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardHolderName = cardHolderName;
        this.amount = amount;
    }

    // Getters and Setters
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
} 