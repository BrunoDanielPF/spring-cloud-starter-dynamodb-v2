package com.example.springcloudawssample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@SpringBootApplication
public class SpringCloudAwsSampleApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudAwsSampleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudAwsSampleApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        return args -> {
            try {
                dynamoDbEnhancedClient.table("tweet", TableSchema.fromBean(Customer.class)).createTable();
            } catch (Exception e) {
                LOGGER.error("Error during creating a DynamoDB table");
            }
        };
    }

}

