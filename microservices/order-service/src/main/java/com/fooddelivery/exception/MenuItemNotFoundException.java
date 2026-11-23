package com.fooddelivery.exception;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(Long menuItemId) {
        super("MenuItem not found with id: '" + menuItemId + "'");
    }

    public MenuItemNotFoundException(String message) {
        super(message);
    }
}
