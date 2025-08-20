package com.flightbooking.bookingservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${payment.service.url:http://localhost:8083}")
    private String paymentServiceUrl;

    @Value("${payment.service.endpoint:/api/payment/pay}")
    private String paymentEndpoint;

    /**
     * Process payment by calling external Payment Service
     * @param amount The amount to charge
     * @return Payment status (SUCCESS/FAILED)
     */
    public String processPayment(double amount) {
        try {
            // Prepare payment request
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("amount", amount);
            paymentRequest.put("cardNumber", "1234-5678-9012-3456"); // Mock card number
            paymentRequest.put("expiryMonth", "12");
            paymentRequest.put("expiryYear", "2025");
            paymentRequest.put("cvv", "123");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentRequest, headers);

            // Make HTTP call to Payment Service
            ResponseEntity<Map> response = restTemplate.exchange(
                paymentServiceUrl + paymentEndpoint,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> paymentResponse = response.getBody();
                return (String) paymentResponse.get("status");
            } else {
                return "FAILED";
            }

        } catch (Exception e) {
            // Log the error and return FAILED
            System.err.println("Error calling Payment Service: " + e.getMessage());
            return "FAILED";
        }
    }
} 