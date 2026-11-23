CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,
    driver_name VARCHAR(255),
    driver_phone VARCHAR(50),
    pickup_address VARCHAR(255),
    delivery_address VARCHAR(255),
    assigned_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);
