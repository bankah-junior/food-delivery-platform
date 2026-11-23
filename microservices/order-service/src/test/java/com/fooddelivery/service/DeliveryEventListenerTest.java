package com.fooddelivery.service;

import com.fooddelivery.event.DeliveryStatusUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliveryEventListenerTest {

    @Test
    void handleDeliveryStatusUpdatedDelegatesToOrderService() {
        Capture capture = new Capture();

        OrderService orderService = new OrderService(null, null, null, null) {
            @Override
            public void handleDeliveryStatusUpdated(Long orderId, String deliveryStatus) {
                capture.orderId = orderId;
                capture.status = deliveryStatus;
            }
        };

        DeliveryEventListener listener = new DeliveryEventListener(orderService, new RabbitTemplate());

        DeliveryStatusUpdatedEvent event = DeliveryStatusUpdatedEvent.builder()
                .deliveryId(501L)
                .orderId(1001L)
                .status("DELIVERED")
                .build();

        listener.handleDeliveryStatusUpdated(event);

        assertEquals(1001L, capture.orderId);
        assertEquals("DELIVERED", capture.status);
    }

    private static class Capture {
        private Long orderId;
        private String status;
    }
}
