package com.example.springcloudawssample;

import java.util.UUID;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Customer {
    private String id;
    private String nameCustomer;
    private String age;

    public Customer(String nameCustomer, String age) {
        this.id = UUID.randomUUID().toString();
        this.nameCustomer = nameCustomer;
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

    public void setNameCustomer(String nameCustomer) {
        this.nameCustomer = nameCustomer;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getNameCustomer() {
        return nameCustomer;
    }

    public String getAge() {
        return age;
    }
}
