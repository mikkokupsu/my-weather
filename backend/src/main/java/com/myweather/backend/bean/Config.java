package com.myweather.backend.bean;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    
    @Bean
    public AmazonDynamoDB getAmazonDynamoDB() {
        final String dynamoDbLocalAddress = System.getenv("AWS_DYNAMODB_LOCAL_ADDRESS");
        if (dynamoDbLocalAddress != null && !"".equals(dynamoDbLocalAddress.trim())) {
            return AmazonDynamoDBAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(dynamoDbLocalAddress, "eu-west-1"))
                .build();
        } else {
            return AmazonDynamoDBAsyncClientBuilder.defaultClient();
        }
    }
}
