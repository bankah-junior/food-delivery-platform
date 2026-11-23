package com.fooddelivery.client;

import com.fooddelivery.client.dto.RemoteCustomerResponse;
import com.fooddelivery.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceClientFallbackFactory implements FallbackFactory<CustomerServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceClientFallbackFactory.class);

    @Override
    public CustomerServiceClient create(Throwable cause) {
        logger.warn("Customer service fallback triggered", cause);
        return customerId -> {
            throw new ServiceUnavailableException(
                    "Customer service is unavailable for customerId=" + customerId,
                    cause
            );
        };
    }
}
