-- Restore sample catalog rows only where prior create-drop removed them.
INSERT INTO events (
    id, name, description, venue_name, venue_address, start_date_time, end_date_time,
    status, banner_url, category, base_price, total_tickets, available_tickets,
    is_hot_trend, check_in_window_minutes
) VALUES
(
    'e1111111-1111-1111-1111-111111111111', 'Ha Noi Rock Fest 2026',
    'Live rock concert', 'My Dinh National Stadium, Ha Noi', '1 Le Duc Tho, Ha Noi',
    NOW() + INTERVAL '7 days', NOW() + INTERVAL '7 days 4 hours', 'PUBLISHED',
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80',
    'Concert', 450000, 5000, 600, TRUE, 120
),
(
    'e2222222-2222-2222-2222-222222222222', 'Monsoon EDM 2026',
    'Live electronic music festival', 'National Convention Center, Ha Noi', 'Thang Long Avenue, Ha Noi',
    NOW() + INTERVAL '14 days', NOW() + INTERVAL '14 days 4 hours', 'PUBLISHED',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80',
    'Concert', 690000, 3000, 1350, TRUE, 120
),
(
    'e3333333-3333-3333-3333-333333333333', 'National Cup Final 2026',
    'Football final', 'Hang Day Stadium, Ha Noi', '9 Trinh Hoai Duc, Ha Noi',
    NOW() + INTERVAL '21 days', NOW() + INTERVAL '21 days 2 hours', 'PUBLISHED',
    'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80',
    'Sports', 200000, 15000, 1200, FALSE, 180
)
ON CONFLICT (id) DO NOTHING;
