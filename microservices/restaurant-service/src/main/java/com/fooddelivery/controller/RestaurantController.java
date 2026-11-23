package com.fooddelivery.controller;

import com.fooddelivery.dto.MenuItemRequest;
import com.fooddelivery.dto.MenuItemResponse;
import com.fooddelivery.dto.RestaurantRequest;
import com.fooddelivery.dto.RestaurantResponse;
import com.fooddelivery.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/search/city/{city}")
    public ResponseEntity<List<RestaurantResponse>> searchByCity(@PathVariable String city) {
        return ResponseEntity.ok(restaurantService.searchByCity(city));
    }

    @GetMapping("/search/cuisine/{type}")
    public ResponseEntity<List<RestaurantResponse>> searchByCuisine(@PathVariable String type) {
        return ResponseEntity.ok(restaurantService.searchByCuisine(type));
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<RestaurantResponse>> getAllActive() {
        return ResponseEntity.ok(restaurantService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenu(id));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(customerId, request));
    }

    @PostMapping("/{restaurantId}/menu")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.addMenuItem(restaurantId, customerId, request));
    }

    @PutMapping("/menu/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(itemId, customerId, request));
    }

    @PatchMapping("/menu/{itemId}/toggle")
    public ResponseEntity<Void> toggleAvailability(
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") Long customerId) {
        restaurantService.toggleMenuItemAvailability(itemId, customerId);
        return ResponseEntity.noContent().build();
    }
}
