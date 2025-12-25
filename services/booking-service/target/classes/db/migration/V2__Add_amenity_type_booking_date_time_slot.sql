-- Add amenity_type, booking_date, and time_slot columns to bookings table
-- First add columns as nullable
ALTER TABLE bookings 
ADD COLUMN IF NOT EXISTS amenity_type VARCHAR(20),
ADD COLUMN IF NOT EXISTS booking_date DATE,
ADD COLUMN IF NOT EXISTS time_slot VARCHAR(20);

-- Update existing rows with default values based on slot_start
-- Only update if columns were just added (they will be NULL)
UPDATE bookings 
SET amenity_type = 'GYM',
    booking_date = DATE(slot_start),
    time_slot = TO_CHAR(slot_start, 'HH24:MI') || '-' || TO_CHAR(slot_end, 'HH24:MI')
WHERE amenity_type IS NULL;

-- Now make columns NOT NULL with defaults
ALTER TABLE bookings 
ALTER COLUMN amenity_type SET DEFAULT 'GYM',
ALTER COLUMN amenity_type SET NOT NULL,
ALTER COLUMN booking_date SET DEFAULT CURRENT_DATE,
ALTER COLUMN booking_date SET NOT NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_bookings_amenity_type ON bookings(amenity_type);
CREATE INDEX IF NOT EXISTS idx_bookings_booking_date ON bookings(booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_time_slot ON bookings(time_slot);
CREATE INDEX IF NOT EXISTS idx_bookings_amenity_date ON bookings(amenity_id, booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_amenity_date_slot ON bookings(amenity_id, booking_date, time_slot);

