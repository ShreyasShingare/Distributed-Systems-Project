-- Add Guest Car Parking amenity
INSERT INTO amenities (name, description, capacity) VALUES
    ('Guest Car Parking', 'Designated parking area for guest vehicles', 10)
ON CONFLICT (name) DO NOTHING;

