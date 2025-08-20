package com.flightbooking.bookingservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfig {

    @Value("${aws.access.key.id}")
    private String accessKeyId;

    @Value("${aws.secret.access.key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoDBEndpoint;

    @Bean
    public BasicAWSCredentials basicAWSCredentials() {
        return new BasicAWSCredentials(accessKeyId, secretAccessKey);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()));

        // If we have a local endpoint, use it (for testing)
        // Otherwise, use the region (for production)
        if (dynamoDBEndpoint != null && !dynamoDBEndpoint.trim().isEmpty()) {
            builder.withEndpointConfiguration(
                new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(
                    dynamoDBEndpoint, region));
        } else {
            builder.withRegion(region);
        }

        return builder.build();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        // Configure the mapper to be more lenient with type validation
        DynamoDBMapperConfig config = new DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
            .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
            .build();
        
        return new DynamoDBMapper(amazonDynamoDB(), config);
    }
} 