package com.fooddelivery.dto;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String deliveryAddress;
    private String specialInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryTime;
    private Long customerId;
    private Long restaurantId;
    private List<OrderItemDetail> items;

    @Data
    public static class OrderItemDetail {
        private Long id;
        private Long menuItemId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String specialInstructions;
    }

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        dto.setCustomerId(order.getCustomerId());
        dto.setRestaurantId(order.getRestaurantId());
        dto.setItems(order.getItems().stream().map(OrderResponse::toItemDetail).toList());
        return dto;
    }

    private static OrderItemDetail toItemDetail(OrderItem item) {
        OrderItemDetail detail = new OrderItemDetail();
        detail.setId(item.getId());
        detail.setMenuItemId(item.getMenuItemId());
        detail.setQuantity(item.getQuantity());
        detail.setUnitPrice(item.getUnitPrice());
        detail.setSubtotal(item.getSubtotal());
        detail.setSpecialInstructions(item.getSpecialInstructions());
        return detail;
    }
}
