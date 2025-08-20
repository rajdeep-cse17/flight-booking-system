package com.flightbooking.paymentservice.enums;

public enum PaymentStatus {
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    PROCESSING("PROCESSING");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
} 