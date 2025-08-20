package com.flightbooking.paymentservice.service;

import com.flightbooking.paymentservice.dto.PaymentRequest;
import com.flightbooking.paymentservice.dto.PaymentResponse;
import com.flightbooking.paymentservice.enums.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResponse processPayment(PaymentRequest request) {
        // For this exercise, hardcoded to return success for all cases
        String transactionId = UUID.randomUUID().toString();
        
        return new PaymentResponse(
            transactionId,
            PaymentStatus.SUCCESS,
            "Payment processed successfully",
            request.getAmount()
        );
    }

    // Method for direct amount processing (used by booking service)
    public String processPayment(double amount) {
        // For this exercise, hardcoded to return success for all cases
        return PaymentStatus.SUCCESS.getValue();
    }
} 