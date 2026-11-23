package com.fooddelivery.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String FOOD_DELIVERY_EXCHANGE = "food-delivery-exchange";

    public static final String DELIVERY_ORDER_PLACED_QUEUE = "delivery.order-placed";
    public static final String DELIVERY_ORDER_CANCELLED_QUEUE = "delivery.order-cancelled";
    public static final String ORDER_DELIVERY_STATUS_UPDATED_QUEUE = "order.delivery-status-updated";
    public static final String DELIVERY_DLQ = "dlq.delivery";

    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";
    public static final String ROUTING_KEY_ORDER_CANCELLED = "order.cancelled";
    public static final String ROUTING_KEY_DELIVERY_STATUS_UPDATED = "delivery.status.updated";
    public static final String ROUTING_KEY_DELIVERY_DEAD_LETTER = "delivery.dead-letter";

    @Bean
    public TopicExchange foodDeliveryExchange() {
        return new TopicExchange(FOOD_DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public Queue deliveryOrderPlacedQueue() {
        return new Queue(DELIVERY_ORDER_PLACED_QUEUE, true);
    }

    @Bean
    public Queue deliveryOrderCancelledQueue() {
        return new Queue(DELIVERY_ORDER_CANCELLED_QUEUE, true);
    }

    @Bean
    public Queue orderDeliveryStatusUpdatedQueue() {
        return new Queue(ORDER_DELIVERY_STATUS_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue deliveryDeadLetterQueue() {
        return new Queue(DELIVERY_DLQ, true);
    }

    @Bean
    public Binding orderPlacedBinding(Queue deliveryOrderPlacedQueue, TopicExchange foodDeliveryExchange) {
        return BindingBuilder.bind(deliveryOrderPlacedQueue)
                .to(foodDeliveryExchange)
                .with(ROUTING_KEY_ORDER_PLACED);
    }

    @Bean
    public Binding orderCancelledBinding(Queue deliveryOrderCancelledQueue, TopicExchange foodDeliveryExchange) {
        return BindingBuilder.bind(deliveryOrderCancelledQueue)
                .to(foodDeliveryExchange)
                .with(ROUTING_KEY_ORDER_CANCELLED);
    }

    @Bean
    public Binding deliveryStatusUpdatedBinding(Queue orderDeliveryStatusUpdatedQueue, TopicExchange foodDeliveryExchange) {
        return BindingBuilder.bind(orderDeliveryStatusUpdatedQueue)
                .to(foodDeliveryExchange)
                .with(ROUTING_KEY_DELIVERY_STATUS_UPDATED);
    }

    @Bean
    public Binding deliveryDeadLetterBinding(Queue deliveryDeadLetterQueue, TopicExchange foodDeliveryExchange) {
        return BindingBuilder.bind(deliveryDeadLetterQueue)
                .to(foodDeliveryExchange)
                .with(ROUTING_KEY_DELIVERY_DEAD_LETTER);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
