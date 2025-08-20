package com.flightbooking.searchservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.flightbooking.searchservice.model.Flight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoDBFlightRepository implements FlightRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Override
    public List<Flight> findAll() {
        return dynamoDBMapper.scan(Flight.class, new DynamoDBScanExpression());
    }

    @Override
    public Flight findByFlightId(String flightId) {
        return dynamoDBMapper.load(Flight.class, flightId);
    }

    @Override
    public List<Flight> findBySourceAndDestination(String source, String destination) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":source", new AttributeValue().withS(source));
        expressionAttributeValues.put(":destination", new AttributeValue().withS(destination));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("source = :source AND destination = :destination")
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Flight.class, scanExpression);
    }

    @Override
    public Flight save(Flight flight) {
        dynamoDBMapper.save(flight);
        return flight;
    }

    @Override
    public void delete(String flightId) {
        Flight flight = new Flight();
        flight.setFlightId(flightId);
        dynamoDBMapper.delete(flight);
    }
} 