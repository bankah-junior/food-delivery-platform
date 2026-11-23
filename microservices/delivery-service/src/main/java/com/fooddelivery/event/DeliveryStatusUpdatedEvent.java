package com.fooddelivery.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusUpdatedEvent {
    private Long deliveryId;
    private Long orderId;
    private String status;
    private LocalDateTime timestamp;
}
