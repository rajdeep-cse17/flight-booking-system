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
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${user.service.endpoint:/api/user/update-booking-value}")
    private String updateBookingValueEndpoint;

    /**
     * Update user's total booking value by calling external User Service
     * @param userId The user ID
     * @param amount The amount to add to total booking value
     */
    public void updateTotalBookingValue(String userId, double amount) {
        try {
            // Prepare update request
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("userId", userId);
            updateRequest.put("amount", amount);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateRequest, headers);

            // Make HTTP call to User Service
            ResponseEntity<Map> response = restTemplate.exchange(
                userServiceUrl + updateBookingValueEndpoint,
                HttpMethod.PUT,
                request,
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Failed to update user booking value. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            // Log the error but don't throw - this is async processing
            System.err.println("Error calling User Service: " + e.getMessage());
        }
    }
} 