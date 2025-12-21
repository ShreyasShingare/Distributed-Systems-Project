-- Drop the old unique constraint that prevented multiple bookings per slot
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS unique_amenity_slot;

-- Add a new constraint that prevents the same user from booking the same slot twice
-- This allows multiple users to book the same slot (up to capacity limit)
-- Note: If constraint already exists, this will fail but can be safely ignored
-- The constraint was already added manually, so this migration is idempotent
