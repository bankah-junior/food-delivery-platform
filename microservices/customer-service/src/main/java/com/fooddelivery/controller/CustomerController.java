package com.fooddelivery.controller;

import com.fooddelivery.dto.CustomerResponse;
import com.fooddelivery.dto.CustomerUpdateRequest;
import com.fooddelivery.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(
            @RequestHeader("X-User-Username") String username) {
        return ResponseEntity.ok(customerService.getProfile(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateProfile(
            @RequestHeader("X-User-Username") String username,
            @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(username, request));
    }
}
