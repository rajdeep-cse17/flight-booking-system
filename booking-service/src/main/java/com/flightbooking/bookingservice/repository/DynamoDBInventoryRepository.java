package com.flightbooking.bookingservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.flightbooking.bookingservice.model.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoDBInventoryRepository implements InventoryRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Override
    public Inventory findByFlightIdAndDate(String flightId, String date) {
        System.err.println("=== DETAILED INVENTORY LOOKUP ===");
        System.err.println("Looking for flightId: " + flightId + ", date: " + date);
        
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            Map<String, String> expressionAttributeNames = new HashMap<>();
            
            expressionAttributeValues.put(":flightId", new AttributeValue().withS(flightId));
            expressionAttributeValues.put(":date", new AttributeValue().withS(date));
            expressionAttributeNames.put("#date", "date");

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("flightId = :flightId AND #date = :date")
                    .withExpressionAttributeValues(expressionAttributeValues)
                    .withExpressionAttributeNames(expressionAttributeNames);

            System.err.println("About to scan with expression: " + scanExpression.getFilterExpression());
            List<Inventory> results = dynamoDBMapper.scan(Inventory.class, scanExpression);
            System.err.println("Scan completed successfully. Found " + results.size() + " results");
            
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("=== FULL EXCEPTION DETAILS ===");
            System.err.println("Exception class: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace(System.err);
            System.err.println("=== END EXCEPTION DETAILS ===");
            throw e; // Re-throw to maintain original behavior
        }
    }

    @Override
    public List<Inventory> findByFlightId(String flightId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":flightId", new AttributeValue().withS(flightId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("flightId = :flightId")
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Inventory.class, scanExpression);
    }

    @Override
    public List<Inventory> findByDate(String date) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        Map<String, String> expressionAttributeNames = new HashMap<>();
        
        expressionAttributeValues.put(":date", new AttributeValue().withS(date));
        expressionAttributeNames.put("#date", "date");

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#date = :date")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withExpressionAttributeNames(expressionAttributeNames);

        return dynamoDBMapper.scan(Inventory.class, scanExpression);
    }

    @Override
    public List<Inventory> findByNumberOfSeatsLeftLessThan(int seats) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":seats", new AttributeValue().withN(String.valueOf(seats)));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("numberOfSeatsLeft < :seats")
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Inventory.class, scanExpression);
    }

    @Override
    public Inventory save(Inventory inventory) {
        dynamoDBMapper.save(inventory);
        return inventory;
    }

    @Override
    public void deleteById(String id) {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(id);
        dynamoDBMapper.delete(inventory);
    }

    @Override
    public Inventory findById(String id) {
        return dynamoDBMapper.load(Inventory.class, id);
    }

    @Override
    public List<Inventory> findAll() {
        return dynamoDBMapper.scan(Inventory.class, new DynamoDBScanExpression());
    }
} 