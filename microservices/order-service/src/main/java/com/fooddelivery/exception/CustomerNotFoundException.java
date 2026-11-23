package com.fooddelivery.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: '" + customerId + "'");
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
