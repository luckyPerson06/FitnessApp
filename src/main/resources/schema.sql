CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(150),
    password VARCHAR(255),
    status VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS client_subscriptions (
    client_id BIGINT REFERENCES clients(id) ON DELETE CASCADE,
    subscription_id BIGINT REFERENCES subscriptions(id) ON DELETE CASCADE,
    PRIMARY KEY (client_id, subscription_id)
    );