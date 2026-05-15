ALTER TABLE perfumes
ADD COLUMN top_notes TEXT,
ADD COLUMN heart_notes TEXT,
ADD COLUMN base_notes TEXT,
ADD COLUMN olfactory_family VARCHAR(100),
ADD COLUMN season VARCHAR(30),
ADD COLUMN projection VARCHAR(30),
ADD COLUMN longevity_score INTEGER,
ADD COLUMN gender_profile VARCHAR(30);
