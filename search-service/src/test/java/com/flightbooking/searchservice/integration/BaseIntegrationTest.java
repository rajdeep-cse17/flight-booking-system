package com.flightbooking.searchservice.integration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.flightbooking.searchservice.model.Flight;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Container
    protected static GenericContainer<?> dynamoDB = new GenericContainer<>(
            DockerImageName.parse("amazon/dynamodb-local:latest"))
            .withExposedPorts(8000)
            .withCommand("-jar DynamoDBLocal.jar -sharedDb -dbPath ./data")
            .waitingFor(Wait.forLogMessage(".*CorsParams:.*", 1))
            .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    protected static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
            .withStartupTimeout(Duration.ofMinutes(1));

    protected static AmazonDynamoDB dynamoDBClient;
    protected static DynamoDB dynamoDBDocument;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.dynamodb.endpoint", 
            () -> "http://localhost:" + dynamoDB.getMappedPort(8000));
        registry.add("spring.redis.host", 
            () -> "localhost");
        registry.add("spring.redis.port", 
            () -> redis.getMappedPort(6379));
    }

    @BeforeAll
    static void setUpDynamoDB() {
        // Create AWS credentials for local testing
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials("local", "local");
        
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:" + dynamoDB.getMappedPort(8000),
                        "us-east-1"
                    )
                )
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        dynamoDBDocument = new DynamoDB(dynamoDBClient);
        createTables();
    }

    @BeforeEach
    void setUpTestData() {
        clearTables();
        insertTestData();
    }

    private static void createTables() {
        // Create Flight table
        CreateTableRequest flightTableRequest = new CreateTableRequest()
                .withTableName("Flight")
                .withKeySchema(new KeySchemaElement("flightId", KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition("flightId", ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        try {
            dynamoDBClient.createTable(flightTableRequest);
        } catch (ResourceInUseException e) {
            // Table already exists
        }

        // Wait for table to be active
        waitForTableToBeActive("Flight");
    }

    private static void waitForTableToBeActive(String tableName) {
        try {
            // Wait for table to be active using a simple polling approach
            boolean tableActive = false;
            int attempts = 0;
            while (!tableActive && attempts < 30) {
                try {
                    DescribeTableResult result = dynamoDBClient.describeTable(tableName);
                    if ("ACTIVE".equals(result.getTable().getTableStatus())) {
                        tableActive = true;
                    } else {
                        Thread.sleep(2000);
                        attempts++;
                    }
                } catch (Exception e) {
                    Thread.sleep(2000);
                    attempts++;
                }
            }
            if (!tableActive) {
                throw new RuntimeException("Table " + tableName + " did not become active in time");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to wait for table " + tableName + " to be active", e);
        }
    }

    private void clearTables() {
        try {
            Table flightTable = dynamoDBDocument.getTable("Flight");
            flightTable.scan().forEach(item -> {
                try {
                    flightTable.deleteItem("flightId", item.getString("flightId"));
                } catch (Exception e) {
                    // Ignore errors during cleanup
                }
            });
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
    }

    private void insertTestData() {
        // Insert sample flights
        List<Flight> testFlights = Arrays.asList(
            new Flight("F001", Arrays.asList("Monday", "Tuesday"), "DEL", "BOM", 299.99),
            new Flight("F002", Arrays.asList("Wednesday", "Thursday"), "DEL", "BOM", 349.99),
            new Flight("F003", Arrays.asList("Monday", "Friday"), "DEL", "BLR", 199.99),
            new Flight("F004", Arrays.asList("Tuesday", "Saturday"), "BOM", "HYD", 249.99),
            new Flight("F005", Arrays.asList("Monday", "Wednesday"), "BLR", "HYD", 179.99)
        );

        Table flightTable = dynamoDBDocument.getTable("Flight");
        testFlights.forEach(flight -> {
            try {
                flightTable.putItem(createDynamoDBItem(flight));
            } catch (Exception e) {
                // Ignore errors during data insertion
            }
        });
    }

    private com.amazonaws.services.dynamodbv2.document.Item createDynamoDBItem(Flight flight) {
        return new com.amazonaws.services.dynamodbv2.document.Item()
                .withString("flightId", flight.getFlightId())
                .withString("daysOfWeek", flight.getDaysOfWeek())
                .withString("source", flight.getSource())
                .withString("destination", flight.getDestination())
                .withString("cost", flight.getCost());
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    protected void waitForServiceReady() {
        try {
            Thread.sleep(5000); // Wait for service to be fully ready
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 