package com.fooddelivery.client;

import com.fooddelivery.client.dto.RemoteCustomerResponse;
import com.fooddelivery.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "customer-service",
        url = "${services.customer.url}",
        configuration = FeignConfig.class
)
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}")
    RemoteCustomerResponse getCustomerById(@PathVariable("id") Long id);
}
