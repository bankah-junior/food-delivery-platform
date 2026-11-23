package com.fooddelivery.service;

import com.fooddelivery.dto.CreateDeliveryRequest;
import com.fooddelivery.dto.DeliveryResponse;
import com.fooddelivery.event.OrderCancelledEvent;
import com.fooddelivery.event.OrderPlacedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderEventListenerTest {

    @Test
    void handleOrderPlacedCreatesDelivery() {
        Capture capture = new Capture();

        DeliveryService deliveryService = new DeliveryService(null, null) {
            @Override
            public DeliveryResponse createDeliveryForOrder(CreateDeliveryRequest request) {
                capture.createdOrderId = request.getOrderId();
                capture.deliveryAddress = request.getDeliveryAddress();
                return new DeliveryResponse();
            }
        };

        OrderEventListener listener = new OrderEventListener(deliveryService, new RabbitTemplate());

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(2001L)
                .restaurantId(77L)
                .deliveryAddress("Spintex Road")
                .build();

        listener.handleOrderPlaced(event);

        assertEquals(2001L, capture.createdOrderId);
        assertEquals("Spintex Road", capture.deliveryAddress);
    }

    @Test
    void handleOrderCancelledCancelsDeliveryByOrderId() {
        Capture capture = new Capture();

        DeliveryService deliveryService = new DeliveryService(null, null) {
            @Override
            public void cancelDeliveryByOrderId(Long orderId) {
                capture.cancelledOrderId = orderId;
            }
        };

        OrderEventListener listener = new OrderEventListener(deliveryService, new RabbitTemplate());

        listener.handleOrderCancelled(OrderCancelledEvent.builder()
                .orderId(3001L)
                .reason("Customer cancelled")
                .build());

        assertEquals(3001L, capture.cancelledOrderId);
    }

    private static class Capture {
        private Long createdOrderId;
        private String deliveryAddress;
        private Long cancelledOrderId;
    }
}
