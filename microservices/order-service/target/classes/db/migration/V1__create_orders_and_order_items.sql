CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    delivery_fee NUMERIC(19,2),
    delivery_address VARCHAR(255),
    special_instructions TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    special_instructions TEXT,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);
