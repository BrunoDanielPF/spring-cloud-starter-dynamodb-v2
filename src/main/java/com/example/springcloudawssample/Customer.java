package com.example.springcloudawssample;

import java.util.UUID;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Customer {
    private String id;
    private String name;
    private String age;

    public Customer(String name, String age) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public Customer() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }
}
