package com.amalitech.deliveryservice.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Custom health indicator for PostgreSQL database connectivity.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            // Attempt to get a connection from the pool
            var connection = dataSource.getConnection();
            connection.close();
            return Health.up()
                    .withDetail("status", "Database connection successful")
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .withException(ex)
                    .build();
        }
    }
}
