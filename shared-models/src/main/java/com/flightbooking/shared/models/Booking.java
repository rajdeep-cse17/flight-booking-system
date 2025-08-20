package com.flightbooking.shared.models;

public class Booking {
    private String bookingId;
    private String userId;
    private String flightIds;
    private String date;
    private String source;
    private String destination;
    private String status;
    private String cost;

    public Booking() {}

    public Booking(String bookingId, String userId, String flightIds, String date, 
                   String source, String destination, String status, String cost) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightIds = flightIds != null ? flightIds : "";
        this.date = date;
        this.source = source;
        this.destination = destination;
        this.status = status != null ? status : "PROCESSING";
        this.cost = cost != null ? cost : "0.0";
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFlightIds() { return flightIds; }
    public void setFlightIds(String flightIds) { this.flightIds = flightIds; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", userId='" + userId + '\'' +
                ", flightIds=" + flightIds +
                ", date=" + date +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", status='" + status + '\'' +
                ", cost=" + cost +
                '}';
    }
} 