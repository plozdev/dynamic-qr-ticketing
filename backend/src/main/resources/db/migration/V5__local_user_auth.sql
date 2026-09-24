-- Keep existing user IDs and tickets while replacing Firebase identity with local credentials.
ALTER TABLE users RENAME COLUMN firebase_uid TO username;
ALTER TABLE users RENAME CONSTRAINT users_firebase_uid_key TO users_username_key;
ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);
UPDATE users SET username = 'demo-legacy-11111111' WHERE id = '11111111-2222-3333-4444-555555555555';
DROP INDEX IF EXISTS idx_users_firebase_uid;
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique ON users(LOWER(email)) WHERE email IS NOT NULL;

CREATE TABLE auth_sessions (
    token_hash VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_auth_sessions_user_id ON auth_sessions(user_id);
