package com.fooddelivery.dto;

import com.fooddelivery.model.Restaurant;
import lombok.Data;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private String city;
    private String phone;
    private boolean active;
    private double rating;
    private int estimatedDeliveryMinutes;
    private int menuItemCount;
    private Long ownerId;

    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        RestaurantResponse dto = new RestaurantResponse();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setCuisineType(restaurant.getCuisineType());
        dto.setAddress(restaurant.getAddress());
        dto.setCity(restaurant.getCity());
        dto.setPhone(restaurant.getPhone());
        dto.setActive(restaurant.isActive());
        dto.setRating(restaurant.getRating());
        dto.setEstimatedDeliveryMinutes(restaurant.getEstimatedDeliveryMinutes());
        dto.setMenuItemCount(restaurant.getMenuItems() != null ? restaurant.getMenuItems().size() : 0);
        dto.setOwnerId(restaurant.getOwnerId());
        return dto;
    }
}
