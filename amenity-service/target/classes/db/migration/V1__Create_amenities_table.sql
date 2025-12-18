CREATE TABLE IF NOT EXISTS amenities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    capacity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample amenities
INSERT INTO amenities (name, description, capacity) VALUES
    ('Gym', 'Fully equipped fitness center with cardio and weight training equipment', 20),
    ('Party Hall', 'Spacious hall for events and celebrations', 50),
    ('Swimming Pool', 'Outdoor swimming pool with changing facilities', 30),
    ('Tennis Court', 'Professional tennis court with lighting', 4),
    ('BBQ Area', 'Outdoor barbecue area with grills and seating', 15)
ON CONFLICT (name) DO NOTHING;
