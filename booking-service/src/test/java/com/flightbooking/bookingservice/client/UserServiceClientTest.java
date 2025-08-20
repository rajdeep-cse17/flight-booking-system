package com.flightbooking.bookingservice.client;

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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserServiceClient userServiceClient;

    @Test
    void testUpdateTotalBookingValue_Success() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        userServiceClient.updateTotalBookingValue("U001", 150.0);

        // Then
        verify(restTemplate).exchange(
            anyString(),
            eq(org.springframework.http.HttpMethod.PUT),
            argThat(request -> {
                Map<String, Object> body = (Map<String, Object>) request.getBody();
                return body.get("userId").equals("U001") &&
                       body.get("amount").equals(150.0);
            }),
            eq(Map.class)
        );
    }

    @Test
    void testUpdateTotalBookingValue_NonSuccessStatusCode() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        userServiceClient.updateTotalBookingValue("U001", 150.0);

        // Then
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Map.class));
        // Should not throw exception, just log error
    }

    @Test
    void testUpdateTotalBookingValue_Exception() {
        // Given
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When
        userServiceClient.updateTotalBookingValue("U001", 150.0);

        // Then
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Map.class));
        // Should not throw exception, just log error
    }

    @Test
    void testUpdateTotalBookingValue_RequestStructure() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        userServiceClient.updateTotalBookingValue("U002", 299.99);

        // Then
        verify(restTemplate).exchange(
            anyString(),
            eq(org.springframework.http.HttpMethod.PUT),
            argThat(request -> {
                Map<String, Object> body = (Map<String, Object>) request.getBody();
                return body.get("userId").equals("U002") &&
                       body.get("amount").equals(299.99);
            }),
            eq(Map.class)
        );
    }

    @Test
    void testUpdateTotalBookingValue_ZeroAmount() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        userServiceClient.updateTotalBookingValue("U001", 0.0);

        // Then
        verify(restTemplate).exchange(
            anyString(),
            eq(org.springframework.http.HttpMethod.PUT),
            argThat(request -> {
                Map<String, Object> body = (Map<String, Object>) request.getBody();
                return body.get("amount").equals(0.0);
            }),
            eq(Map.class)
        );
    }

    @Test
    void testUpdateTotalBookingValue_LargeAmount() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(response);

        // When
        userServiceClient.updateTotalBookingValue("U001", 999999.99);

        // Then
        verify(restTemplate).exchange(
            anyString(),
            eq(org.springframework.http.HttpMethod.PUT),
            argThat(request -> {
                Map<String, Object> body = (Map<String, Object>) request.getBody();
                return body.get("amount").equals(999999.99);
            }),
            eq(Map.class)
        );
    }
} 