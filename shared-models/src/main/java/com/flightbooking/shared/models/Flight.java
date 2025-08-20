package com.flightbooking.shared.models;

import java.util.List;

public class Flight {
    private String flightId;
    private List<String> daysOfWeek; // List of days when flight operates
    private String source;
    private String destination;
    private double cost;

    public Flight() {}

    public Flight(String flightId, List<String> daysOfWeek, String source, String destination, double cost) {
        this.flightId = flightId;
        this.daysOfWeek = daysOfWeek;
        this.source = source;
        this.destination = destination;
        this.cost = cost;
    }

    // Getters and Setters
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    @Override
    public String toString() {
        return "Flight{" +
                "flightId='" + flightId + '\'' +
                ", daysOfWeek=" + daysOfWeek +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", cost=" + cost +
                '}';
    }
} 