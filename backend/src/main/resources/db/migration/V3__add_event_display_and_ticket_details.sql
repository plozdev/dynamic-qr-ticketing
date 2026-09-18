-- =====================================================================
-- Migration V3: Add event display details & ticket seating/attendee info
-- =====================================================================

-- 1. Extend events table
ALTER TABLE events
    ADD COLUMN IF NOT EXISTS banner_url VARCHAR(1024),
    ADD COLUMN IF NOT EXISTS category VARCHAR(100) NOT NULL DEFAULT 'Âm nhạc & Concert',
    ADD COLUMN IF NOT EXISTS base_price NUMERIC(15, 2) NOT NULL DEFAULT 450000,
    ADD COLUMN IF NOT EXISTS total_tickets INT NOT NULL DEFAULT 1000,
    ADD COLUMN IF NOT EXISTS available_tickets INT NOT NULL DEFAULT 850,
    ADD COLUMN IF NOT EXISTS is_hot_trend BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS check_in_window_minutes INT NOT NULL DEFAULT 120;

CREATE INDEX IF NOT EXISTS idx_events_category ON events(category);

-- 2. Extend tickets table
ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS seat_number VARCHAR(50) NOT NULL DEFAULT 'GA-01',
    ADD COLUMN IF NOT EXISTS attendee_name VARCHAR(255) NOT NULL DEFAULT 'Khán Giả',
    ADD COLUMN IF NOT EXISTS gate_info VARCHAR(100) NOT NULL DEFAULT 'CỔNG CHÍNH';

-- 3. Seed initial sample events for development & mobile testing
INSERT INTO events (
    id, name, description, venue_name, venue_address,
    start_date_time, end_date_time, status,
    banner_url, category, base_price, total_tickets, available_tickets, is_hot_trend, check_in_window_minutes
) VALUES
(
    'e1111111-1111-1111-1111-111111111111',
    'Hà Nội Rock Fest 2026',
    'Đại nhạc hội Rock cuồng nhiệt quy tụ các ban nhạc hàng đầu Đông Nam Á.',
    'SVĐ Quốc Gia Mỹ Đình, Hà Nội',
    '1 Lê Đức Thọ, Nam Từ Liêm, Hà Nội',
    NOW() + INTERVAL '1 hour',
    NOW() + INTERVAL '5 hours',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80',
    'Âm nhạc & Concert',
    450000,
    5000,
    600,
    TRUE,
    120
),
(
    'e2222222-2222-2222-2222-222222222222',
    'Đại Nhạc Hội Monsoon EDM',
    'Bữa tiệc âm thanh ánh sáng bùng nổ cùng dàn DJ quốc tế.',
    'TT Hội Nghị Quốc Gia, Hà Nội',
    'Đại Lộ Thăng Long, Mễ Trì, Hà Nội',
    NOW() + INTERVAL '3 days',
    NOW() + INTERVAL '3 days 4 hours',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80',
    'Âm nhạc & Concert',
    690000,
    3000,
    1350,
    TRUE,
    120
),
(
    'e3333333-3333-3333-3333-333333333333',
    'Chung Kết Cúp Quốc Gia 2026',
    'Trận cầu đỉnh cao tranh chức vô địch bóng đá quốc gia.',
    'SVĐ Hàng Đẫy, Hà Nội',
    '9 Trịnh Hoài Đức, Cát Linh, Đống Đa, Hà Nội',
    NOW() + INTERVAL '5 days',
    NOW() + INTERVAL '5 days 2 hours',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80',
    'Thể thao',
    200000,
    15000,
    1200,
    FALSE,
    180
)
ON CONFLICT (id) DO NOTHING;

-- 4. Seed initial tickets for default demo user (userId: 11111111-2222-3333-4444-555555555555)
INSERT INTO tickets (
    id, event_id, user_id, category_name, secret_key, status, issued_at, seat_number, attendee_name, gate_info
) VALUES
(
    'a1111111-0000-0000-0000-000000000001',
    'e1111111-1111-1111-1111-111111111111',
    '11111111-2222-3333-4444-555555555555',
    'VIP Diamond',
    '47c9f87cb5e23631f24d1a6e9a7e02e86d0b674b3e813739a8c62b92ef51bcf6',
    'ACTIVE',
    NOW() - INTERVAL '1 day',
    'VIP-A12',
    'Nguyễn Hoàng Long',
    'CỔNG A1'
),
(
    'a2222222-0000-0000-0000-000000000002',
    'e2222222-2222-2222-2222-222222222222',
    '11111111-2222-3333-4444-555555555555',
    'Fanzone Standard',
    '81b9d45e7f12a34bc09ef4812398a1273948bf9123847acb123894719283741a',
    'ACTIVE',
    NOW() - INTERVAL '2 days',
    'ZONE-FANZ-08',
    'Nguyễn Hoàng Long',
    'CỔNG B2'
),
(
    'a3333333-0000-0000-0000-000000000003',
    'e3333333-3333-3333-3333-333333333333',
    '11111111-2222-3333-4444-555555555555',
    'Khán Đài A',
    '1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
    'USED',
    NOW() - INTERVAL '10 days',
    'STAND-A-45',
    'Nguyễn Hoàng Long',
    'CỔNG CHÍNH'
)
ON CONFLICT (id) DO NOTHING;
