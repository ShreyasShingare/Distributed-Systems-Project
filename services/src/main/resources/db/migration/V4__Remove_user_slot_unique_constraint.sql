-- Remove the unique constraint that prevents same user from booking same slot multiple times
-- This allows users to make multiple bookings for the same time slot
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS unique_user_amenity_slot;

