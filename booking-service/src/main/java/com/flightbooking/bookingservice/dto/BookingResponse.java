package com.flightbooking.bookingservice.dto;

import com.flightbooking.bookingservice.enums.BookingStatus;

public class BookingResponse {
    private String bookingId;
    private BookingStatus status;
    private String message;
    private double cost;

    public BookingResponse() {}

    public BookingResponse(String bookingId, BookingStatus status, String message, double cost) {
        this.bookingId = bookingId;
        this.status = status;
        this.message = message;
        this.cost = cost;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
} 