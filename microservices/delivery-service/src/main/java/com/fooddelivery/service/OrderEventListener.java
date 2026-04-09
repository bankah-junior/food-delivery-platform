package com.fooddelivery.service;

import com.fooddelivery.config.RabbitMqConfig;
import com.fooddelivery.dto.CreateDeliveryRequest;
import com.fooddelivery.event.OrderCancelledEvent;
import com.fooddelivery.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.rabbit.listeners.enabled", havingValue = "true")
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    private final DeliveryService deliveryService;
    private final RabbitTemplate rabbitTemplate;

    public OrderEventListener(DeliveryService deliveryService, RabbitTemplate rabbitTemplate) {
        this.deliveryService = deliveryService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMqConfig.DELIVERY_ORDER_PLACED_QUEUE)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        try {
            CreateDeliveryRequest request = new CreateDeliveryRequest();
            request.setOrderId(event.getOrderId());
            request.setPickupAddress("Restaurant pickup (restaurantId=" + event.getRestaurantId() + ")");
            request.setDeliveryAddress(event.getDeliveryAddress());

            deliveryService.createDeliveryForOrder(request);
            logger.info("Consumed OrderPlacedEvent and created delivery for orderId={}", event.getOrderId());
        } catch (Exception ex) {
            logger.error("Failed to process OrderPlacedEvent for orderId={}", event.getOrderId(), ex);
            publishToDlq("OrderPlacedEvent", event, ex.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMqConfig.DELIVERY_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        try {
            deliveryService.cancelDeliveryByOrderId(event.getOrderId());
            logger.info("Consumed OrderCancelledEvent for orderId={}", event.getOrderId());
        } catch (Exception ex) {
            logger.error("Failed to process OrderCancelledEvent for orderId={}", event.getOrderId(), ex);
            publishToDlq("OrderCancelledEvent", event, ex.getMessage());
        }
    }

    private void publishToDlq(String eventType, Object payload, String reason) {
        Map<String, Object> dlqPayload = new HashMap<>();
        dlqPayload.put("eventType", eventType);
        dlqPayload.put("reason", reason);
        dlqPayload.put("timestamp", LocalDateTime.now().toString());
        dlqPayload.put("payload", payload);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FOOD_DELIVERY_EXCHANGE,
                RabbitMqConfig.ROUTING_KEY_DELIVERY_DEAD_LETTER,
                dlqPayload
        );
    }
}
