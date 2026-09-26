CREATE TABLE IF NOT EXISTS event_venue_gates (
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    gate_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (event_id, gate_name)
);
