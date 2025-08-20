package com.flightbooking.bookingservice.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceClient paymentServiceClient;

    private Map<String, Object> mockPaymentResponse;

    @BeforeEach
    void setUp() {
        mockPaymentResponse = new HashMap<>();
        mockPaymentResponse.put("status", "SUCCESS");
    }

    @Test
    void testProcessPayment_Success() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(mockPaymentResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        String result = paymentServiceClient.processPayment(100.0);

        // Then
        assertEquals("SUCCESS", result);
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Map.class));
    }

    @Test
    void testProcessPayment_Failure() {
        // Given
        Map<String, Object> failureResponse = new HashMap<>();
        failureResponse.put("status", "FAILED");
        ResponseEntity<Map> response = new ResponseEntity<>(failureResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        String result = paymentServiceClient.processPayment(100.0);

        // Then
        assertEquals("FAILED", result);
    }

    @Test
    void testProcessPayment_NonSuccessStatusCode() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(mockPaymentResponse, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        String result = paymentServiceClient.processPayment(100.0);

        // Then
        assertEquals("FAILED", result);
    }

    @Test
    void testProcessPayment_NullResponse() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        String result = paymentServiceClient.processPayment(100.0);

        // Then
        assertEquals("FAILED", result);
    }

    @Test
    void testProcessPayment_Exception() {
        // Given
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When
        String result = paymentServiceClient.processPayment(100.0);

        // Then
        assertEquals("FAILED", result);
    }

    @Test
    void testProcessPayment_RequestStructure() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(mockPaymentResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        paymentServiceClient.processPayment(150.50);

        // Then
        verify(restTemplate).exchange(
            anyString(),
            eq(org.springframework.http.HttpMethod.POST),
            argThat(request -> {
                Map<String, Object> body = (Map<String, Object>) request.getBody();
                return body.get("amount").equals(150.50) &&
                       body.containsKey("cardNumber") &&
                       body.containsKey("expiryMonth") &&
                       body.containsKey("expiryYear") &&
                       body.containsKey("cvv");
            }),
            eq(Map.class)
        );
    }
} 