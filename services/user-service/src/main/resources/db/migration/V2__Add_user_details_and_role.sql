-- Add name, flat_no, contact_number, and role columns to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS name VARCHAR(255),
ADD COLUMN IF NOT EXISTS flat_no VARCHAR(50),
ADD COLUMN IF NOT EXISTS contact_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'USER';

-- Create default admin user (password: admin123)
-- NOTE: Password stored in plain text (NOT RECOMMENDED FOR PRODUCTION)
INSERT INTO users (username, password, role, name, flat_no, contact_number)
VALUES ('admin', 'admin123', 'ADMIN', 'Admin User', 'ADMIN-001', '0000000000')
ON CONFLICT (username) DO UPDATE 
SET role = 'ADMIN',
    password = 'admin123',
    name = COALESCE(users.name, 'Admin User'),
    flat_no = COALESCE(users.flat_no, 'ADMIN-001'),
    contact_number = COALESCE(users.contact_number, '0000000000');

