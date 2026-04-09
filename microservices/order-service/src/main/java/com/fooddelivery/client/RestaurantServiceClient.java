package com.fooddelivery.client;

import com.fooddelivery.client.dto.RemoteMenuItemResponse;
import com.fooddelivery.client.dto.RemoteRestaurantResponse;
import com.fooddelivery.config.FeignConfig;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FeignClient(
        name = "restaurant-service",
    fallbackFactory = RestaurantServiceClientFallbackFactory.class,
        configuration = FeignConfig.class
)
public interface RestaurantServiceClient {

    @GetMapping("/api/restaurants/{id}")
    @Retry(name = "restaurantService")
    RemoteRestaurantResponse getRestaurantById(@PathVariable("id") Long id);

    @GetMapping("/api/restaurants/{id}/menu")
    @Retry(name = "restaurantService")
    List<RemoteMenuItemResponse> getMenuByRestaurantId(@PathVariable("id") Long restaurantId);

    default RemoteMenuItemResponse getMenuItem(Long restaurantId, Long itemId) {
        return getMenuByRestaurantId(restaurantId).stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    default List<RemoteMenuItemResponse> validateMenuItems(Long restaurantId, List<Long> itemIds) {
        Set<Long> requested = new HashSet<>(itemIds);
        return getMenuByRestaurantId(restaurantId).stream()
                .filter(item -> requested.contains(item.getId()))
                .filter(RemoteMenuItemResponse::isAvailable)
                .collect(Collectors.toList());
    }
}
