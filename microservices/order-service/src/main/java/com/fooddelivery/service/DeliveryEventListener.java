package com.fooddelivery.service;

import com.fooddelivery.config.RabbitMqConfig;
import com.fooddelivery.event.DeliveryStatusUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeliveryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventListener.class);

    private final OrderService orderService;
    private final RabbitTemplate rabbitTemplate;

    public DeliveryEventListener(OrderService orderService, RabbitTemplate rabbitTemplate) {
        this.orderService = orderService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_DELIVERY_STATUS_UPDATED_QUEUE)
    public void handleDeliveryStatusUpdated(DeliveryStatusUpdatedEvent event) {
        try {
            orderService.handleDeliveryStatusUpdated(event.getOrderId(), event.getStatus());
            logger.info("Consumed DeliveryStatusUpdatedEvent for orderId={} status={}",
                    event.getOrderId(), event.getStatus());
        } catch (Exception ex) {
            logger.error("Failed to process DeliveryStatusUpdatedEvent for orderId={}", event.getOrderId(), ex);
            publishToDlq("DeliveryStatusUpdatedEvent", event, ex.getMessage());
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
