-- =====================================================================
-- Migration V4: Add users table for Firebase Authentication & JIT Provisioning
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    firebase_uid VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(255),
    display_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Seed default demo user
INSERT INTO users (id, firebase_uid, email, display_name, avatar_url, created_at, updated_at)
VALUES (
    '11111111-2222-3333-4444-555555555555',
    'demo-firebase-uid-11111111',
    'hoanglong@dynamic-qr.vn',
    'Nguyễn Hoàng Long',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
    NOW() - INTERVAL '30 days',
    NOW()
)
ON CONFLICT (id) DO NOTHING;
