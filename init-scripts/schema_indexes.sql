-- Database Optimization & Indexing Script for PlaySwap (PostgreSQL / MySQL)

-- 1. Indexing transfer_sessions for fast history lookup by user_id
CREATE INDEX IF NOT EXISTS idx_transfer_sessions_user_created ON transfer_sessions(user_id, created_at DESC);

-- 2. Indexing transfer_jobs for fast lookup by session_id
CREATE INDEX IF NOT EXISTS idx_transfer_jobs_session_id ON transfer_jobs(session_id);

-- 3. Indexing transfer_items for fast job items aggregation
CREATE INDEX IF NOT EXISTS idx_transfer_items_job_id_status ON transfer_items(job_id, status);

-- 4. transfer_history table and indexes for instantaneous user history retrieval
CREATE TABLE IF NOT EXISTS transfer_history (
    id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    user_id UUID NOT NULL,
    session_id UUID,
    job_id UUID,
    source_platform VARCHAR(50),
    target_platform VARCHAR(50),
    source_playlist_id VARCHAR(255),
    source_playlist_name VARCHAR(255),
    target_playlist_id VARCHAR(255),
    target_playlist_name VARCHAR(255),
    target_playlist_url TEXT,
    total_tracks INTEGER,
    transferred_tracks_count INTEGER,
    status VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transfer_history_user_created ON transfer_history(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transfer_history_session_id ON transfer_history(session_id);

-- 5. Indexing song_mappings for zero-latency cache lookups during track matching
CREATE INDEX IF NOT EXISTS idx_song_mappings_lookup ON song_mappings(source_platform, source_track_id, destination_platform);
