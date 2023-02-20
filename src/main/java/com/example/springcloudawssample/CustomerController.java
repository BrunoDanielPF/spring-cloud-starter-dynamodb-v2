package com.example.springcloudawssample;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

@RestController
class CustomerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);
    private final DynamoDbTemplate dynamoDbTemplate;

    CustomerController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @GetMapping("/customers")
    List<Customer> customers() {
        return dynamoDbTemplate.scanAll(Customer.class).items().stream().toList();
    }

    @PostMapping("/customers")
    void create(@RequestBody CustomerDto customerDto) {
        LOGGER.info("Creating tweet: {}", customerDto);
        dynamoDbTemplate.save(new Customer(customerDto.name(), customerDto.age()));
    }
    @GetMapping("register/{id}")
    Customer getById(@PathVariable String id){
        LOGGER.info("get tweet by id: {}", id);
       return dynamoDbTemplate.load(Key.builder().partitionValue(id).build(), Customer.class);
    }
    @PutMapping
    void test(Customer w) {
        LOGGER.info("update tweet: {}", w);
        dynamoDbTemplate.update(w);
    }

    @GetMapping("/{value}")
    List<Customer> scanByAttributeByContent(@RequestBody CustomerName customerName, @PathVariable String value) {
        return dynamoDbTemplate.scan(ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression(value + " = :value")
                        .expressionValues(Map.of(":value", AttributeValue.builder()
                                .s(customerName.name())
                                .build()))
                        .build())
                .build(), Customer.class)
                .items().stream().toList();
    }

    @GetMapping
    List<Customer> scanByAttributeByContent(@RequestBody Customer customer, @PathVariable String value) {
        return dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression(value + ": value")
                        .expressionValues(Map.of(":value", AttributeValue.builder().s(customer.getAge()).build())).build())
                        .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                                .partitionValue(customer.getId())
                                .build()))
                .build(), Customer.class).items().stream().toList();
    }
}
