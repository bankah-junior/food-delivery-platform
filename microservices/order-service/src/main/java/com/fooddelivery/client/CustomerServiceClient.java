package com.fooddelivery.client;

import com.fooddelivery.client.dto.RemoteCustomerResponse;
import com.fooddelivery.config.FeignConfig;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "customer-service",
    fallbackFactory = CustomerServiceClientFallbackFactory.class,
        configuration = FeignConfig.class
)
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}")
    @Retry(name = "customerService")
    RemoteCustomerResponse getCustomerById(@PathVariable("id") Long id);
}
