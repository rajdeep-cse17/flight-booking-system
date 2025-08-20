package com.flightbooking.bookingservice.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "inventory")
public class Inventory {
    private String inventoryId;
    private String flightId;
    private String date;
    private String numberOfSeatsLeft;  // Changed to String
    private String version;            // Changed to String

    public Inventory() {}

    public Inventory(String inventoryId, String flightId, String date, int numberOfSeatsLeft) {
        this.inventoryId = inventoryId;
        this.flightId = flightId;
        this.date = date;
        this.numberOfSeatsLeft = String.valueOf(numberOfSeatsLeft);
        this.version = "1";
    }

    public Inventory(String inventoryId, String flightId, String date, int numberOfSeatsLeft, Long version) {
        this.inventoryId = inventoryId;
        this.flightId = flightId;
        this.date = date;
        this.numberOfSeatsLeft = String.valueOf(numberOfSeatsLeft);
        this.version = String.valueOf(version);
    }

    // Getters and Setters
    @DynamoDBHashKey(attributeName = "inventoryId")
    public String getInventoryId() { return inventoryId; }
    public void setInventoryId(String inventoryId) { this.inventoryId = inventoryId; }

    @DynamoDBAttribute(attributeName = "flightId")
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    @DynamoDBAttribute(attributeName = "date")
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @DynamoDBAttribute(attributeName = "numberOfSeatsLeft")
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

    @DynamoDBAttribute(attributeName = "version")
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
                "inventoryId='" + inventoryId + '\'' +
                ", flightId='" + flightId + '\'' +
                ", date=" + date +
                ", numberOfSeatsLeft=" + numberOfSeatsLeft +
                ", version=" + version +
                '}';
    }
} 