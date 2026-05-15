-- індекси для оптимізації пошуку

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

CREATE INDEX IF NOT EXISTS idx_notes_category ON notes(category);
CREATE INDEX IF NOT EXISTS idx_notes_type ON notes(note_type);

CREATE INDEX IF NOT EXISTS idx_compositions_user_id ON compositions(user_id);
CREATE INDEX IF NOT EXISTS idx_compositions_perfume_id ON compositions(perfume_id);

CREATE INDEX IF NOT EXISTS idx_composition_items_note_id ON composition_items(note_id);

CREATE INDEX IF NOT EXISTS idx_composition_history_composition_id ON composition_history(composition_id);

CREATE INDEX IF NOT EXISTS idx_family_synergy_a ON family_synergy(family_a);
CREATE INDEX IF NOT EXISTS idx_family_synergy_b ON family_synergy(family_b);
