package com.fooddelivery.client.dto;

import lombok.Data;

@Data
public class RemoteCustomerResponse {
    private Long id;
    private String username;
    private String deliveryAddress;
    private String city;
}
