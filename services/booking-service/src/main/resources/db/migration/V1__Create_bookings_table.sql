-- Create bookings table with unique constraint for concurrency control
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    amenity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    slot_start TIMESTAMP NOT NULL,
    slot_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_amenity_slot UNIQUE (amenity_id, slot_start, slot_end)
);

CREATE INDEX idx_bookings_amenity_id ON bookings(amenity_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_slot_start ON bookings(slot_start);

