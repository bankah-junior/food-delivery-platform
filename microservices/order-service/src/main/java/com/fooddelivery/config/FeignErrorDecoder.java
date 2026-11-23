package com.fooddelivery.config;

import com.fooddelivery.exception.CustomerNotFoundException;
import com.fooddelivery.exception.MenuItemNotFoundException;
import com.fooddelivery.exception.RestaurantNotFoundException;
import com.fooddelivery.exception.ServiceUnavailableException;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status == 404) {
            if (methodKey.contains("CustomerServiceClient#getCustomerById")) {
                return new CustomerNotFoundException("Customer not found in customer-service");
            }
            if (methodKey.contains("RestaurantServiceClient#getRestaurantById")) {
                return new RestaurantNotFoundException("Restaurant not found in restaurant-service");
            }
            if (methodKey.contains("RestaurantServiceClient#getMenuByRestaurantId")) {
                return new MenuItemNotFoundException("Menu items not found for the requested restaurant");
            }
        }

        if (status >= 500) {
            return new ServiceUnavailableException("Dependent service is unavailable");
        }

        Exception decoded = defaultDecoder.decode(methodKey, response);
        if (decoded instanceof FeignException || decoded instanceof RetryableException) {
            return new ServiceUnavailableException("Failed to communicate with dependent service", decoded);
        }

        return decoded;
    }
}
