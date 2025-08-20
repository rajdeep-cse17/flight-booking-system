package com.flightbooking.searchservice.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.List;

@DynamoDBTable(tableName = "flights")
public class Flight {
    private String flightId;
    private String daysOfWeek;  // Store as String to match DynamoDB format
    private String source;
    private String destination;
    private String cost;        // Store as String to match DynamoDB format
    
    public Flight() {}
    
    public Flight(String flightId, List<String> daysOfWeek, String source, String destination, double cost) {
        this.flightId = flightId;
        this.daysOfWeek = daysOfWeek != null ? String.join(",", daysOfWeek) : "";
        this.source = source;
        this.destination = destination;
        this.cost = String.valueOf(cost);
    }
    
    @DynamoDBHashKey(attributeName = "flightId")
    public String getFlightId() {
        return flightId;
    }
    
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }
    
    @DynamoDBAttribute(attributeName = "daysOfWeek")
    @DynamoDBTypeConverted(converter = DaysOfWeekConverter.class)
    public String getDaysOfWeek() {
        return daysOfWeek;
    }
    
    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    // Custom converter to handle both List and String formats using AttributeValue
    public static class DaysOfWeekConverter implements DynamoDBTypeConverter<AttributeValue, String> {
        @Override
        public AttributeValue convert(String input) {
            if (input == null || input.trim().isEmpty()) {
                return new AttributeValue().withS("");
            }
            return new AttributeValue().withS(input);
        }

        @Override
        public String unconvert(AttributeValue input) {
            if (input == null) {
                return "";
            }
            if (input.getS() != null) {
                return input.getS();
            }
            if (input.getL() != null) {
                // Convert List to comma-separated string
                return input.getL().stream()
                    .map(av -> av.getS() != null ? av.getS() : "")
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
            }
            return "";
        }
    }

    // Convenience method to get as List
    public List<String> getDaysOfWeekAsList() {
        if (daysOfWeek == null || daysOfWeek.trim().isEmpty()) {
            return List.of();
        }
        return List.of(daysOfWeek.split(","));
    }

    // Convenience method to set from List
    public void setDaysOfWeekFromList(List<String> daysList) {
        this.daysOfWeek = daysList != null ? String.join(",", daysList) : "";
    }
    
    @DynamoDBAttribute(attributeName = "source")
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    @DynamoDBAttribute(attributeName = "destination")
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    @DynamoDBAttribute(attributeName = "cost")
    @DynamoDBTypeConverted(converter = CostConverter.class)
    public String getCost() {
        return cost;
    }
    
    public void setCost(String cost) {
        this.cost = cost;
    }

    // Custom converter to handle both Number and String formats using AttributeValue
    public static class CostConverter implements DynamoDBTypeConverter<AttributeValue, String> {
        @Override
        public AttributeValue convert(String input) {
            if (input == null || input.trim().isEmpty()) {
                return new AttributeValue().withS("0.0");
            }
            return new AttributeValue().withS(input);
        }

        @Override
        public String unconvert(AttributeValue input) {
            if (input == null) {
                return "0.0";
            }
            if (input.getS() != null) {
                return input.getS();
            }
            if (input.getN() != null) {
                return input.getN();
            }
            return "0.0";
        }
    }

    // Convenience method to get as double
    public double getCostAsDouble() {
        try {
            return Double.parseDouble(cost);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Convenience method to set from double
    public void setCostFromDouble(double costValue) {
        this.cost = String.valueOf(costValue);
    }
    
    @Override
    public String toString() {
        return "Flight{" +
                "flightId='" + flightId + '\'' +
                ", daysOfWeek='" + daysOfWeek + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", cost='" + cost + '\'' +
                '}';
    }
} 