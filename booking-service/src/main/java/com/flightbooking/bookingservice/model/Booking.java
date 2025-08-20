package com.flightbooking.bookingservice.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.List;

@DynamoDBTable(tableName = "bookings")
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
        this.flightIds = flightIds;
        this.date = date;
        this.source = source;
        this.destination = destination;
        this.status = status;
        this.cost = cost;
    }

    // Getters and Setters - Simple and direct
    @DynamoDBHashKey(attributeName = "bookingId")
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDBAttribute(attributeName = "flightIds")
    public String getFlightIds() { return flightIds; }
    public void setFlightIds(String flightIds) { this.flightIds = flightIds; }

    @DynamoDBAttribute(attributeName = "date")
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @DynamoDBAttribute(attributeName = "source")
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @DynamoDBAttribute(attributeName = "destination")
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @DynamoDBAttribute(attributeName = "cost")
    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }

    // Simple convenience methods
    public List<String> getFlightIdsAsList() {
        if (flightIds == null || flightIds.trim().isEmpty()) {
            return List.of();
        }
        return List.of(flightIds.split(","));
    }
    
    public void setFlightIdsAsList(List<String> flightIds) {
        this.flightIds = flightIds != null ? String.join(",", flightIds) : "";
    }

    public double getCostAsDouble() {
        try {
            return Double.parseDouble(cost != null ? cost : "0.0");
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public void setCostAsDouble(double cost) {
        this.cost = String.valueOf(cost);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", userId='" + userId + '\'' +
                ", flightIds='" + flightIds + '\'' +
                ", date='" + date + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", status='" + status + '\'' +
                ", cost='" + cost + '\'' +
                '}';
    }
} 