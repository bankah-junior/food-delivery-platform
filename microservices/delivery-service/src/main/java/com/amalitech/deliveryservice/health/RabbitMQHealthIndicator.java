package com.amalitech.deliveryservice.health;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for RabbitMQ connectivity and functionality.
 */
@Component
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQHealthIndicator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        try {
            // Verify connection to RabbitMQ by checking the channel
            if (rabbitTemplate.getConnectionFactory() != null &&
                rabbitTemplate.getConnectionFactory().createConnection() != null) {
                return Health.up()
                        .withDetail("status", "RabbitMQ connection successful")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Unable to establish RabbitMQ connection")
                        .build();
            }
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .withException(ex)
                    .build();
        }
    }
}
