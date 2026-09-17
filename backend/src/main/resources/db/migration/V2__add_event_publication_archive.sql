-- Spring Modulith Event Publication Archive Table (for completion-mode: archive)
CREATE TABLE IF NOT EXISTS event_publication_archive (
    id UUID NOT NULL PRIMARY KEY,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_event_publication_archive_by_completion_date ON event_publication_archive(completion_date);
