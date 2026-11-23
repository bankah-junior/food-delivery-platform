package com.fooddelivery.service;

import com.fooddelivery.config.RabbitMqConfig;
import com.fooddelivery.event.OrderCancelledEvent;
import com.fooddelivery.event.OrderPlacedEvent;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderPlaced(Order order) {
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .items(toOrderItemSnapshots(order.getItems()))
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FOOD_DELIVERY_EXCHANGE,
                RabbitMqConfig.ROUTING_KEY_ORDER_PLACED,
                event
        );

        logger.info("Published OrderPlacedEvent for orderId={} customerId={}", order.getId(), order.getCustomerId());
    }

    public void publishOrderCancelled(Long orderId, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(orderId)
                .reason(reason)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FOOD_DELIVERY_EXCHANGE,
                RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED,
                event
        );

        logger.info("Published OrderCancelledEvent for orderId={} reason={}", orderId, reason);
    }

    private List<OrderPlacedEvent.OrderItemSnapshot> toOrderItemSnapshots(List<OrderItem> items) {
        return items.stream()
                .map(item -> OrderPlacedEvent.OrderItemSnapshot.builder()
                        .menuItemId(item.getMenuItemId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();
    }
}
