ALTER TABLE compositions
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS is_favorite BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS version_number INT DEFAULT 1;

CREATE TABLE composition_versions (
    id BIGSERIAL PRIMARY KEY,
    composition_id BIGINT NOT NULL REFERENCES compositions(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    snapshot_json JSONB NOT NULL,
    change_description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_composition_version UNIQUE (composition_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_comp_versions_composition_id ON composition_versions(composition_id);
CREATE INDEX IF NOT EXISTS idx_compositions_status ON compositions(status);
CREATE INDEX IF NOT EXISTS idx_compositions_favorite ON compositions(is_favorite);
CREATE INDEX IF NOT EXISTS idx_compositions_public ON compositions(is_public);
