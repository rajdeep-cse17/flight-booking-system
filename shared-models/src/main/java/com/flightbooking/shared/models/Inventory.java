package com.flightbooking.shared.models;

public class Inventory {
    private String inventoryId;
    private String flightId;
    private String date;
    private String numberOfSeatsLeft;
    private String version;

    public Inventory() {
        this.version = "1"; // Initialize version for optimistic locking
    }

    public Inventory(String inventoryId, String flightId, String date, int numberOfSeatsLeft) {
        this.inventoryId = inventoryId;
        this.flightId = flightId;
        this.date = date;
        this.numberOfSeatsLeft = String.valueOf(numberOfSeatsLeft);
        this.version = "1"; // Initialize version for optimistic locking
    }

    // Getters and Setters
    public String getInventoryId() { return inventoryId; }
    public void setInventoryId(String inventoryId) { this.inventoryId = inventoryId; }
    
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNumberOfSeatsLeft() { return numberOfSeatsLeft; }
    public void setNumberOfSeatsLeft(String numberOfSeatsLeft) { this.numberOfSeatsLeft = numberOfSeatsLeft; }

    // Convenience methods for int conversion
    public int getNumberOfSeatsLeftAsInt() { 
        try {
            return Integer.parseInt(numberOfSeatsLeft);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void setNumberOfSeatsLeftAsInt(int seats) { 
        this.numberOfSeatsLeft = String.valueOf(seats); 
    }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    // Convenience methods for Long conversion
    public Long getVersionAsLong() { 
        try {
            return Long.parseLong(version);
        } catch (NumberFormatException e) {
            return 1L;
        }
    }
    
    public void setVersionAsLong(Long version) { 
        this.version = String.valueOf(version); 
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "flightId='" + flightId + '\'' +
                ", date=" + date +
                ", numberOfSeatsLeft=" + numberOfSeatsLeft +
                '}';
    }
} 