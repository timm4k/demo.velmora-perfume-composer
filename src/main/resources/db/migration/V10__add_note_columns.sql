-- колонки для Vault: зображення, походження, тривалість

ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS image_path VARCHAR(255);

ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS origin VARCHAR(100);

ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS longevity_hours INT DEFAULT 4;

ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS volatility_score INT DEFAULT 5;
