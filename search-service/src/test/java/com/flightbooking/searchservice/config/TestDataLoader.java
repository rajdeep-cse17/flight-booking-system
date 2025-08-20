package com.flightbooking.searchservice.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.flightbooking.searchservice.model.Flight;
import com.flightbooking.searchservice.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("integration-test") // Only run during integration tests
public class TestDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(TestDataLoader.class);

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    /**
     * Manually load test data when called
     */
    public void loadTestData() {
        try {
            // First, ensure the Flight table exists
            createFlightTableIfNotExists();
            
            // Load sample flight data
            List<Flight> sampleFlights = Arrays.asList(
                new Flight("F001", Arrays.asList("Monday", "Tuesday"), "DEL", "BOM", 299.99),
                new Flight("F002", Arrays.asList("Wednesday", "Thursday"), "DEL", "BOM", 349.99),
                new Flight("F003", Arrays.asList("Monday", "Friday"), "DEL", "BLR", 199.99),
                new Flight("F004", Arrays.asList("Tuesday", "Saturday"), "BOM", "HYD", 249.99),
                new Flight("F005", Arrays.asList("Monday", "Wednesday"), "BLR", "BOM", 279.99)
            );

            for (Flight flight : sampleFlights) {
                flightRepository.save(flight);
            }

            logger.info("Successfully loaded {} test flights", sampleFlights.size());

        } catch (Exception e) {
            logger.warn("⚠️  Warning: Could not load test data: {}", e.getMessage());
        }
    }

    private void createFlightTableIfNotExists() {
        String tableName = "flights"; // Changed from "Flight" to "flights" to match @DynamoDBTable annotation
        
        try {
            // Check if table exists
            DescribeTableRequest describeRequest = new DescribeTableRequest().withTableName(tableName);
            amazonDynamoDB.describeTable(describeRequest);
            logger.info("Table {} already exists", tableName);
            return;
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            logger.info("Table {} doesn't exist, creating it...", tableName);
        }

        try {
            CreateTableRequest createRequest = new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(
                    new AttributeDefinition("flightId", ScalarAttributeType.S)
                )
                .withKeySchema(
                    new KeySchemaElement("flightId", KeyType.HASH)
                )
                .withProvisionedThroughput(
                    new ProvisionedThroughput(5L, 5L)
                );

            CreateTableResult result = amazonDynamoDB.createTable(createRequest);
            logger.info("Table {} created successfully", tableName);

            // Wait for table to be active
            waitForTableToBeActive(tableName);

        } catch (Exception e) {
            logger.error("Failed to create table {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create DynamoDB table", e);
        }
    }

    private void waitForTableToBeActive(String tableName) {
        try {
            DescribeTableRequest describeRequest = new DescribeTableRequest().withTableName(tableName);
            
            while (true) {
                DescribeTableResult result = amazonDynamoDB.describeTable(describeRequest);
                String status = result.getTable().getTableStatus();
                
                if ("ACTIVE".equals(status)) {
                    logger.info("Table {} is now ACTIVE", tableName);
                    break;
                }
                
                logger.debug("Table {} status: {}, waiting...", tableName, status);
                Thread.sleep(1000); // Wait 1 second before checking again
            }
        } catch (Exception e) {
            logger.error("Error waiting for table {} to be active: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to wait for table to be active", e);
        }
    }
} 