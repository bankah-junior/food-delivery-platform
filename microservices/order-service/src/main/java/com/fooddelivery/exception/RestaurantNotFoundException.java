package com.fooddelivery.exception;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(Long restaurantId) {
        super("Restaurant not found with id: '" + restaurantId + "'");
    }

    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
