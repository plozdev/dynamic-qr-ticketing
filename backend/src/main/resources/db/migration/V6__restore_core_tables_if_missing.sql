-- Earlier development runs used Hibernate create-drop while Flyway history persisted.
-- Recreate only missing tables; existing data and tables remain untouched.
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    venue_name VARCHAR(255),
    venue_address VARCHAR(500),
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    banner_url VARCHAR(1024),
    category VARCHAR(100) NOT NULL DEFAULT 'Concert',
    base_price NUMERIC(15, 2) NOT NULL DEFAULT 450000,
    total_tickets INT NOT NULL DEFAULT 1000,
    available_tickets INT NOT NULL DEFAULT 850,
    is_hot_trend BOOLEAN NOT NULL DEFAULT FALSE,
    check_in_window_minutes INT NOT NULL DEFAULT 120
);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_category ON events(category);

CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    used_at_gate_id VARCHAR(100),
    seat_number VARCHAR(50) NOT NULL DEFAULT 'GA-01',
    attendee_name VARCHAR(255) NOT NULL DEFAULT 'Guest',
    gate_info VARCHAR(100) NOT NULL DEFAULT 'MAIN GATE'
);
CREATE INDEX IF NOT EXISTS idx_tickets_user_id ON tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_tickets_event_id ON tickets(event_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);

CREATE TABLE IF NOT EXISTS gate_scan_logs (
    id UUID PRIMARY KEY,
    ticket_id UUID,
    gate_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message VARCHAR(500),
    scanned_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_gate_scan_logs_ticket_id ON gate_scan_logs(ticket_id);
CREATE INDEX IF NOT EXISTS idx_gate_scan_logs_gate_id ON gate_scan_logs(gate_id);
CREATE INDEX IF NOT EXISTS idx_gate_scan_logs_scanned_at ON gate_scan_logs(scanned_at DESC);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    source_module VARCHAR(100) NOT NULL,
    principal VARCHAR(255) NOT NULL,
    details VARCHAR(2048),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_recorded_at ON audit_logs(recorded_at DESC);

CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_event_publication_by_completion_date ON event_publication(completion_date);

CREATE TABLE IF NOT EXISTS event_publication_archive (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_event_publication_archive_by_completion_date ON event_publication_archive(completion_date);
