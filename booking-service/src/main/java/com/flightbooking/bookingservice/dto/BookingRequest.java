package com.flightbooking.bookingservice.dto;

import java.util.List;

public class BookingRequest {
    private String userId;
    private List<String> flightIds;
    private String date;
    private String source;
    private String destination;
    private int numberOfPassengers;

    public BookingRequest() {}

    public BookingRequest(String userId, List<String> flightIds, String date, String source, String destination, int numberOfPassengers) {
        this.userId = userId;
        this.flightIds = flightIds;
        this.date = date;
        this.source = source;
        this.destination = destination;
        this.numberOfPassengers = numberOfPassengers;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getFlightIds() { return flightIds; }
    public void setFlightIds(List<String> flightIds) { this.flightIds = flightIds; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getNumberOfPassengers() { return numberOfPassengers; }
    public void setNumberOfPassengers(int numberOfPassengers) { this.numberOfPassengers = numberOfPassengers; }
} 