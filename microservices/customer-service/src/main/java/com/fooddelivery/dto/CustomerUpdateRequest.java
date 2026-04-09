package com.fooddelivery.dto;

import lombok.Data;

@Data
public class CustomerUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String deliveryAddress;
    private String city;
}
