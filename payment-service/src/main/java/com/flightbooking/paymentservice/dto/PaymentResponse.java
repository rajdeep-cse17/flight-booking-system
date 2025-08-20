package com.flightbooking.paymentservice.dto;

import com.flightbooking.paymentservice.enums.PaymentStatus;

public class PaymentResponse {
    private String transactionId;
    private PaymentStatus status;
    private String message;
    private double amount;

    public PaymentResponse() {}

    public PaymentResponse(String transactionId, PaymentStatus status, String message, double amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.amount = amount;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
} 