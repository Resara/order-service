CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255)   NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    created_at    TIMESTAMP      NOT NULL
);

CREATE TABLE order_items (
    order_id   BIGINT       NOT NULL,
    item       VARCHAR(255) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_customer_name ON orders(customer_name);
CREATE INDEX idx_orders_status ON orders(status);