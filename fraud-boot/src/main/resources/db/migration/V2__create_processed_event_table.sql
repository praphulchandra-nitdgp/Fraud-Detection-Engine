CREATE TABLE IF NOT EXISTS processed_event (
    event_id VARCHAR(100) PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);