-- =====================================================================
-- SEED DATA: SỰ KIỆN THỰC TẾ & VÉ NGƯỜI DÙNG TẠI VIỆT NAM
-- Thực thi tự động sau khi Hibernate drop-create schema
-- =====================================================================

-- 0. TÀI KHOẢN NGƯỜI DÙNG MẪU (USERS)
INSERT INTO users (id, username, email, display_name, avatar_url, created_at, updated_at)
VALUES (
    '11111111-2222-3333-4444-555555555555',
    'demo-legacy-11111111',
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
