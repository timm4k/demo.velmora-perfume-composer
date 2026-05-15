ALTER TABLE users RENAME COLUMN invite_code TO verification_code_hash;
ALTER TABLE users ALTER COLUMN verification_code_hash TYPE VARCHAR(255);
ALTER TABLE users ADD COLUMN verification_expires_at TIMESTAMP WITH TIME ZONE;
