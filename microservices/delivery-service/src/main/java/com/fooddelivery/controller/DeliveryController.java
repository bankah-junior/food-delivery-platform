package com.fooddelivery.controller;

import com.fooddelivery.dto.CreateDeliveryRequest;
import com.fooddelivery.dto.DeliveryResponse;
import com.fooddelivery.dto.DeliveryStatusUpdateRequest;
import com.fooddelivery.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryService.getByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody DeliveryStatusUpdateRequest request) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request));
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> createForOrder(@RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.createDeliveryForOrder(request));
    }
}
