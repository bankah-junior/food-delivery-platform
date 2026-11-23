package com.fooddelivery.dto;

import com.fooddelivery.model.MenuItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean available;
    private String imageUrl;
    private Long restaurantId;
    private String restaurantName;

    public static MenuItemResponse fromEntity(MenuItem menuItem) {
        MenuItemResponse dto = new MenuItemResponse();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setCategory(menuItem.getCategory());
        dto.setAvailable(menuItem.isAvailable());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setRestaurantId(menuItem.getRestaurant().getId());
        dto.setRestaurantName(menuItem.getRestaurant().getName());
        return dto;
    }
}
