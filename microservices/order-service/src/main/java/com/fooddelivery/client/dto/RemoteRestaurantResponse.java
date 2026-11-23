package com.fooddelivery.client.dto;

import lombok.Data;

@Data
public class RemoteRestaurantResponse {
    private Long id;
    private String name;
    private boolean active;
    private int estimatedDeliveryMinutes;
}
