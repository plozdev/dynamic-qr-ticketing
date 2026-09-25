-- Development demo accounts. All use password 12345678, encoded with Spring BCryptPasswordEncoder.
-- Keep these accounts out of production datasets.
INSERT INTO users (id, username, email, password_hash, display_name, created_at, updated_at, role_id)
SELECT seed.id::uuid, seed.username, seed.email,
       '$2a$10$emdWKea1//VjeCqaOi6/u.Idu6U4y2Jg3Ex4T4dZrA1p6MyxT8Z4i',
       seed.display_name, NOW(), NOW(), roles.id
FROM (VALUES
    ('d0000000-0000-0000-0000-000000000001', 'demo01', 'demo01@example.test', 'Nguyễn Minh Anh'),
    ('d0000000-0000-0000-0000-000000000002', 'demo02', 'demo02@example.test', 'Trần Hoàng Nam'),
    ('d0000000-0000-0000-0000-000000000003', 'demo03', 'demo03@example.test', 'Lê Thu Hà'),
    ('d0000000-0000-0000-0000-000000000004', 'demo04', 'demo04@example.test', 'Phạm Quốc Bảo'),
    ('d0000000-0000-0000-0000-000000000005', 'demo05', 'demo05@example.test', 'Võ Ngọc Linh'),
    ('d0000000-0000-0000-0000-000000000006', 'demo06', 'demo06@example.test', 'Đặng Gia Huy'),
    ('d0000000-0000-0000-0000-000000000007', 'demo07', 'demo07@example.test', 'Bùi Khánh Vy'),
    ('d0000000-0000-0000-0000-000000000008', 'demo08', 'demo08@example.test', 'Đỗ Minh Khang'),
    ('d0000000-0000-0000-0000-000000000009', 'demo09', 'demo09@example.test', 'Hoàng Phương Thảo'),
    ('d0000000-0000-0000-0000-000000000010', 'demo10', 'demo10@example.test', 'Phan Đức Anh'),
    ('d0000000-0000-0000-0000-000000000011', 'demo11', 'demo11@example.test', 'Mai Hải Yến'),
    ('d0000000-0000-0000-0000-000000000012', 'demo12', 'demo12@example.test', 'Ngô Nhật Minh')
) AS seed(id, username, email, display_name)
CROSS JOIN roles
WHERE roles.name = 'USER'
ON CONFLICT (id) DO NOTHING;

-- Future-dated catalog entries for marketplace, booking, and gate dashboard demos.
INSERT INTO events (
    id, name, description, venue_name, venue_address, start_date_time, end_date_time,
    status, banner_url, category, base_price, total_tickets, available_tickets,
    is_hot_trend, check_in_window_minutes, check_in_enabled
)
SELECT seed.id::uuid, seed.name, seed.description, seed.venue_name, seed.venue_address,
       NOW() + seed.days_ahead * INTERVAL '1 day',
       NOW() + seed.days_ahead * INTERVAL '1 day' + seed.duration_hours * INTERVAL '1 hour',
       'PUBLISHED', seed.banner_url, seed.category, seed.base_price,
       seed.total_tickets, seed.total_tickets, seed.hot_trend, 120, FALSE
FROM (VALUES
    ('e4000000-0000-0000-0000-000000000001', 'Saigon Indie Nights 2026', 'Đêm nhạc indie cùng các nghệ sĩ trẻ tại TP.HCM.', 'Nhà thi đấu Phú Thọ', '1 Lữ Gia, Quận 11, TP.HCM', 4, 4, 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=1200&q=85', 'Âm nhạc & Concert', 350000, 2500, TRUE),
    ('e4000000-0000-0000-0000-000000000002', 'Da Nang Beach Music Festival', 'Âm nhạc điện tử bên bờ biển Đà Nẵng.', 'Công viên Biển Đông', 'Võ Nguyên Giáp, Sơn Trà, Đà Nẵng', 8, 6, 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=1200&q=85', 'Âm nhạc & Concert', 590000, 6000, TRUE),
    ('e4000000-0000-0000-0000-000000000003', 'Ha Noi Jazz Weekend', 'Không gian jazz cuối tuần tại trung tâm Hà Nội.', 'Cung Văn hóa Hữu nghị', '91 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 11, 3, 'https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?auto=format&fit=crop&w=1200&q=85', 'Âm nhạc & Concert', 420000, 1800, FALSE),
    ('e4000000-0000-0000-0000-000000000004', 'Vietnam Basketball All-Star', 'Trận bóng rổ giao hữu giữa những ngôi sao hàng đầu.', 'Nhà thi đấu Quân khu 7', '202 Hoàng Văn Thụ, Phú Nhuận, TP.HCM', 15, 3, 'https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=1200&q=85', 'Thể thao', 280000, 4000, FALSE),
    ('e4000000-0000-0000-0000-000000000005', 'Hue Heritage Live', 'Đêm biểu diễn âm nhạc và di sản tại Huế.', 'Quảng trường Ngọ Môn', 'Đại Nội Huế, TP. Huế', 18, 4, 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=1200&q=85', 'Văn hóa', 320000, 3500, FALSE),
    ('e4000000-0000-0000-0000-000000000006', 'Mekong Food & Music Fair', 'Ẩm thực miền Tây kết hợp sân khấu âm nhạc ngoài trời.', 'Bến Ninh Kiều', 'Hai Bà Trưng, Ninh Kiều, Cần Thơ', 22, 5, 'https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=1200&q=85', 'Lễ hội', 150000, 5000, TRUE),
    ('e4000000-0000-0000-0000-000000000007', 'V-League Derby Ha Noi', 'Trận derby hấp dẫn của bóng đá thủ đô.', 'Sân vận động Hàng Đẫy', '9 Trịnh Hoài Đức, Đống Đa, Hà Nội', 26, 2, 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1200&q=85', 'Thể thao', 220000, 12000, FALSE),
    ('e4000000-0000-0000-0000-000000000008', 'Cyberpass Tech Expo', 'Triển lãm công nghệ, trải nghiệm sản phẩm và hội thảo.', 'SECC', '799 Nguyễn Văn Linh, Quận 7, TP.HCM', 31, 8, 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=85', 'Triển lãm', 120000, 8000, FALSE),
    ('e4000000-0000-0000-0000-000000000009', 'Da Lat Acoustic Garden', 'Đêm nhạc acoustic giữa không gian thông Đà Lạt.', 'Quảng trường Lâm Viên', 'Trần Quốc Toản, Đà Lạt, Lâm Đồng', 36, 3, 'https://images.unsplash.com/photo-1516280440614-37939bbacd81?auto=format&fit=crop&w=1200&q=85', 'Âm nhạc & Concert', 390000, 2200, TRUE),
    ('e4000000-0000-0000-0000-000000000010', 'Nha Trang Summer Wave', 'Lễ hội âm nhạc mùa hè bên bờ biển Nha Trang.', 'Quảng trường 2/4', 'Trần Phú, Nha Trang, Khánh Hòa', 42, 6, 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=85', 'Lễ hội', 490000, 6500, TRUE),
    ('e4000000-0000-0000-0000-000000000011', 'Vietnam Esports Championship', 'Chung kết giải thể thao điện tử toàn quốc.', 'Trung tâm Hội nghị Quốc gia', 'Đại lộ Thăng Long, Nam Từ Liêm, Hà Nội', 49, 7, 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=85', 'Esports', 180000, 5000, FALSE),
    ('e4000000-0000-0000-0000-000000000012', 'Saigon Countdown Live', 'Đại tiệc âm nhạc và ánh sáng cuối năm.', 'Phố đi bộ Nguyễn Huệ', 'Nguyễn Huệ, Quận 1, TP.HCM', 60, 5, 'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?auto=format&fit=crop&w=1200&q=85', 'Âm nhạc & Concert', 650000, 10000, TRUE)
) AS seed(id, name, description, venue_name, venue_address, days_ahead, duration_hours,
          banner_url, category, base_price, total_tickets, hot_trend)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_venue_gates (event_id, gate_name)
SELECT events.id, gates.gate_name
FROM events
CROSS JOIN (VALUES ('A1'), ('B1')) AS gates(gate_name)
WHERE events.id IN (
    'e4000000-0000-0000-0000-000000000001', 'e4000000-0000-0000-0000-000000000002',
    'e4000000-0000-0000-0000-000000000003', 'e4000000-0000-0000-0000-000000000004',
    'e4000000-0000-0000-0000-000000000005', 'e4000000-0000-0000-0000-000000000006',
    'e4000000-0000-0000-0000-000000000007', 'e4000000-0000-0000-0000-000000000008',
    'e4000000-0000-0000-0000-000000000009', 'e4000000-0000-0000-0000-000000000010',
    'e4000000-0000-0000-0000-000000000011', 'e4000000-0000-0000-0000-000000000012'
)
ON CONFLICT (event_id, gate_name) DO NOTHING;
