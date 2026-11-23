package com.fooddelivery.dto;

import com.fooddelivery.model.Delivery;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryResponse {
    private Long id;
    private String status;
    private Long orderId;
    private String driverName;
    private String driverPhone;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;

    public static DeliveryResponse fromEntity(Delivery delivery) {
        DeliveryResponse dto = new DeliveryResponse();
        dto.setId(delivery.getId());
        dto.setStatus(delivery.getStatus().name());
        dto.setOrderId(delivery.getOrderId());
        dto.setDriverName(delivery.getDriverName());
        dto.setDriverPhone(delivery.getDriverPhone());
        dto.setPickupAddress(delivery.getPickupAddress());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setAssignedAt(delivery.getAssignedAt());
        dto.setPickedUpAt(delivery.getPickedUpAt());
        dto.setDeliveredAt(delivery.getDeliveredAt());
        dto.setCreatedAt(delivery.getCreatedAt());
        return dto;
    }
}
