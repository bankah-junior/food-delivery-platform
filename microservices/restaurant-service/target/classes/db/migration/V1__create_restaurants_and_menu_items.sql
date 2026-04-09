CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cuisine_type VARCHAR(100),
    address VARCHAR(255),
    city VARCHAR(100),
    phone VARCHAR(30),
    active BOOLEAN NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    estimated_delivery_minutes INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    owner_id BIGINT NOT NULL
);

CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19,2) NOT NULL,
    category VARCHAR(100),
    available BOOLEAN NOT NULL,
    image_url VARCHAR(512),
    restaurant_id BIGINT NOT NULL,
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);
