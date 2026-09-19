-- =====================================================================
-- SEED DATA: SỰ KIỆN THỰC TẾ & VÉ NGƯỜI DÙNG TẠI VIỆT NAM
-- Thực thi tự động sau khi Hibernate drop-create schema
-- =====================================================================

-- 0. TÀI KHOẢN NGƯỜI DÙNG MẪU (USERS)
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

-- 1. DANH SÁCH SỰ KIỆN THẬT (EVENTS)
INSERT INTO events (
    id, name, description, venue_name, venue_address,
    start_date_time, end_date_time, status,
    banner_url, category, base_price, total_tickets, available_tickets, is_hot_trend, check_in_window_minutes
) VALUES
(
    'e1111111-1111-1111-1111-111111111111',
    'Hà Nội Rock Fest 2026',
    'Đại nhạc hội Rock cuồng nhiệt quy tụ các ban nhạc Rock hàng đầu Việt Nam và quốc tế như Bức Tường, Ngũ Cung, Microwave.',
    'SVĐ Quốc Gia Mỹ Đình, Hà Nội',
    '1 Lê Đức Thọ, Mỹ Đình, Nam Từ Liêm, Hà Nội',
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
    'Đại Nhạc Hội Monsoon EDM 2026',
    'Bữa tiệc âm thanh ánh sáng bùng nổ cùng dàn DJ quốc tế và nghệ sĩ trẻ hàng đầu V-Pop.',
    'TT Hội Nghị Quốc Gia, Hà Nội',
    'Đại Lộ Thăng Long, Mễ Trì, Nam Từ Liêm, Hà Nội',
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
    'Chung Kết Cúp Quốc Gia 2026: Hà Nội FC vs CAHN',
    'Trận derby rực lửa thủ đô quyết định chủ nhân cúp vô địch bóng đá Quốc gia 2026.',
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
),
(
    'e4444444-4444-4444-4444-444444444444',
    'Anh Trai "Say Hi" Live Concert 2026',
    'Đêm concert trực tiếp hoành tráng của 30 Anh Trai bùng nổ cùng hàng chục nghìn khán giả.',
    'Khu Đô Thị Vạn Phúc City, TP.HCM',
    'Quốc lộ 13, Hiệp Bình Phước, TP. Thủ Đức, TP. Hồ Chí Minh',
    NOW() + INTERVAL '10 days',
    NOW() + INTERVAL '10 days 5 hours',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80',
    'Âm nhạc & Concert',
    800000,
    20000,
    2150,
    TRUE,
    180
),
(
    'e5555555-5555-5555-5555-555555555555',
    'Vở Nhạc Kịch "Những Người Khốn Khổ" (Les Misérables)',
    'Tác phẩm kinh điển chuyển thể nhạc kịch dàn dựng công phu bởi Dàn nhạc Giao hưởng Việt Nam.',
    'Nhà Hát Lớn Hà Nội',
    '1 Tràng Tiền, Phan Chu Trinh, Hoàn Kiếm, Hà Nội',
    NOW() + INTERVAL '7 days',
    NOW() + INTERVAL '7 days 3 hours',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80',
    'Kịch nghệ & Sân khấu',
    500000,
    600,
    85,
    FALSE,
    90
),
(
    'e6666666-6666-6666-6666-666666666666',
    'Triển Lãm Nghệ Thuật Số Đa Giác Quang Tỏa',
    'Không gian nghệ thuật ánh sáng tương tác đa giác quan với công nghệ trình chiếu 3D mapping.',
    'Bảo Tàng Mỹ Thuật Việt Nam',
    '66 Nguyễn Thái Học, Điện Biên, Ba Đình, Hà Nội',
    NOW() + INTERVAL '2 days',
    NOW() + INTERVAL '14 days',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1579783902614-a3fb3927b675?auto=format&fit=crop&w=800&q=80',
    'Triển lãm & Festival',
    150000,
    2000,
    1450,
    FALSE,
    60
),
(
    'e7777777-7777-7777-7777-777777777777',
    'Giải VnExpress Marathon Hanoi Midnight 2026',
    'Hành trình chạy đêm qua các danh thắng lịch sử của thủ đô nghìn năm văn hiến.',
    'Quảng Trường Đông Kinh Nghĩa Thục, Hà Nội',
    'Khu vực Hồ Hoàn Kiếm, Hoàn Kiếm, Hà Nội',
    NOW() + INTERVAL '15 days',
    NOW() + INTERVAL '15 days 6 hours',
    'PUBLISHED',
    'https://images.unsplash.com/photo-1530549387789-4c1017266635?auto=format&fit=crop&w=800&q=80',
    'Thể thao',
    750000,
    10000,
    3200,
    TRUE,
    120
);

-- 2. DANH SÁCH VÉ CỦA NGƯỜI DÙNG DEMO (userId: 11111111-2222-3333-4444-555555555555)
INSERT INTO tickets (
    id, event_id, user_id, category_name, secret_key, status, issued_at, used_at, used_at_gate_id, seat_number, attendee_name, gate_info
) VALUES
(
    -- Vé 1: Hà Nội Rock Fest -> check-in opens 120m before start (start in 1 hour) -> READY_TO_CHECK_IN
    'a1111111-0000-0000-0000-000000000001',
    'e1111111-1111-1111-1111-111111111111',
    '11111111-2222-3333-4444-555555555555',
    'VIP Diamond',
    '47c9f87cb5e23631f24d1a6e9a7e02e86d0b674b3e813739a8c62b92ef51bcf6',
    'ACTIVE',
    NOW() - INTERVAL '1 day',
    NULL,
    NULL,
    'VIP-A12',
    'Nguyễn Hoàng Long',
    'CỔNG A1'
),
(
    -- Vé 2: Monsoon EDM -> start in 3 days -> NOT_YET_CHECK_IN
    'a2222222-0000-0000-0000-000000000002',
    'e2222222-2222-2222-2222-222222222222',
    '11111111-2222-3333-4444-555555555555',
    'Fanzone Standard',
    '81b9d45e7f12a34bc09ef4812398a1273948bf9123847acb123894719283741a',
    'ACTIVE',
    NOW() - INTERVAL '2 days',
    NULL,
    NULL,
    'ZONE-FANZ-08',
    'Nguyễn Hoàng Long',
    'CỔNG B2'
),
(
    -- Vé 3: Chung Kết Cúp Quốc Gia -> đã qua cổng check-in -> CHECKED_IN
    'a3333333-0000-0000-0000-000000000003',
    'e3333333-3333-3333-3333-333333333333',
    '11111111-2222-3333-4444-555555555555',
    'Khán Đài A',
    '1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
    'USED',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '10 days' + INTERVAL '30 minutes',
    'CỔNG CHÍNH',
    'STAND-A-45',
    'Nguyễn Hoàng Long',
    'CỔNG CHÍNH'
),
(
    -- Vé 4: Anh Trai "Say Hi" -> start in 10 days -> NOT_YET_CHECK_IN
    'a4444444-0000-0000-0000-000000000004',
    'e4444444-4444-4444-4444-444444444444',
    '11111111-2222-3333-4444-555555555555',
    'SVIP Vạn Phúc',
    'a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a9b8',
    'ACTIVE',
    NOW() - INTERVAL '3 days',
    NULL,
    NULL,
    'SVIP-01',
    'Nguyễn Hoàng Long',
    'CỔNG VIP-1'
),
(
    -- Vé 5: Vở Nhạc Kịch Những Người Khốn Khổ
    'a5555555-0000-0000-0000-000000000005',
    'e5555555-5555-5555-5555-555555555555',
    '11111111-2222-3333-4444-555555555555',
    'Hạng Nhất - Tầng 1',
    'b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2',
    'ACTIVE',
    NOW() - INTERVAL '4 days',
    NULL,
    NULL,
    'A-15',
    'Nguyễn Hoàng Long',
    'CỔNG CHÍNH'
),
(
    -- Vé 6: Triển Lãm Nghệ Thuật Số Đa Giác Quang Tỏa
    'a6666666-0000-0000-0000-000000000006',
    'e6666666-6666-6666-6666-666666666666',
    '11111111-2222-3333-4444-555555555555',
    'Vé Tiêu Chuẩn',
    'c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3',
    'ACTIVE',
    NOW() - INTERVAL '5 days',
    NULL,
    NULL,
    'TỰ DO',
    'Nguyễn Hoàng Long',
    'CỔNG VÀO'
),
(
    -- Vé 7: Giải Marathon Midnight 2026
    'a7777777-0000-0000-0000-000000000007',
    'e7777777-7777-7777-7777-777777777777',
    '11111111-2222-3333-4444-555555555555',
    'BIB 21KM',
    'd3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4',
    'ACTIVE',
    NOW() - INTERVAL '6 days',
    NULL,
    NULL,
    'BIB-21045',
    'Nguyễn Hoàng Long',
    'CỔNG XUẤT PHÁT'
);
