package com.flightbooking.bookingservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.flightbooking.bookingservice.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoDBBookingRepository implements BookingRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Override
    public List<Booking> findByUserId(String userId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":userId", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :userId")
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Booking.class, scanExpression);
    }

    @Override
    public List<Booking> findByStatus(String status) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":status", new AttributeValue().withS(status));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("status = :status")
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Booking.class, scanExpression);
    }

    @Override
    public List<Booking> findByUserIdAndStatus(String userId, String status) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":userId", new AttributeValue().withS(userId));
        expressionAttributeValues.put(":status", new AttributeValue().withS(status));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :userId AND status = :status")
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Booking.class, scanExpression);
    }

    @Override
    public Booking save(Booking booking) {
        System.err.println("=== DYNAMODB SAVE OPERATION ===");
        System.err.println("About to save booking: " + booking);
        System.err.println("Booking class: " + booking.getClass().getName());
        System.err.println("Booking fields:");
        System.err.println("  - bookingId: " + booking.getBookingId() + " (type: " + (booking.getBookingId() != null ? booking.getBookingId().getClass().getName() : "null") + ")");
        System.err.println("  - userId: " + booking.getUserId() + " (type: " + (booking.getUserId() != null ? booking.getUserId().getClass().getName() : "null") + ")");
        System.err.println("  - flightIds: " + booking.getFlightIds() + " (type: " + (booking.getFlightIds() != null ? booking.getFlightIds().getClass().getName() : "null") + ")");
        System.err.println("  - date: " + booking.getDate() + " (type: " + (booking.getDate() != null ? booking.getDate().getClass().getName() : "null") + ")");
        System.err.println("  - source: " + booking.getSource() + " (type: " + (booking.getSource() != null ? booking.getSource().getClass().getName() : "null") + ")");
        System.err.println("  - destination: " + booking.getDestination() + " (type: " + (booking.getDestination() != null ? booking.getDestination().getClass().getName() : "null") + ")");
        System.err.println("  - status: " + booking.getStatus() + " (type: " + (booking.getStatus() != null ? booking.getStatus().getClass().getName() : "null") + ")");
        System.err.println("  - cost: " + booking.getCost() + " (type: " + (booking.getCost() != null ? booking.getCost().getClass().getName() : "null") + ")");
        
        try {
            System.err.println("About to call dynamoDBMapper.save()...");
            dynamoDBMapper.save(booking);
            System.err.println("DynamoDB save completed successfully");
            return booking;
        } catch (Exception e) {
            System.err.println("=== DYNAMODB SAVE EXCEPTION ===");
            System.err.println("Exception class: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace(System.err);
            System.err.println("=== END DYNAMODB SAVE EXCEPTION ===");
            throw e;
        }
    }

    @Override
    public void deleteById(String id) {
        Booking booking = new Booking();
        booking.setBookingId(id);
        dynamoDBMapper.delete(booking);
    }

    @Override
    public Booking findById(String id) {
        return dynamoDBMapper.load(Booking.class, id);
    }

    @Override
    public List<Booking> findAll() {
        return dynamoDBMapper.scan(Booking.class, new DynamoDBScanExpression());
    }
} 