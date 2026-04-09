package com.fooddelivery.client.dto;

import lombok.Data;

@Data
public class RemoteMenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private boolean available;
}
