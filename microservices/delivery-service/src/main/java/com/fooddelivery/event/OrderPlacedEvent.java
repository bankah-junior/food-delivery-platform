package com.fooddelivery.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private List<OrderItemSnapshot> items;
    private BigDecimal totalAmount;
    private String deliveryAddress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemSnapshot {
        private Long menuItemId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
