-- =============================================================================
-- Таблиця: family_synergy
-- Правила сумісності нотних родин для аналізу композицій
-- score: -1 конфлікт, 0 нейтрально, +1 добре, +2 чудово
-- =============================================================================
CREATE TABLE family_synergy (
    id BIGSERIAL PRIMARY KEY,
    family_a VARCHAR(50) NOT NULL,
    family_b VARCHAR(50) NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    comment TEXT,
    CONSTRAINT uq_family_pair UNIQUE (family_a, family_b),
    CONSTRAINT chk_score CHECK (score BETWEEN -1 AND 2)
);

INSERT INTO family_synergy (family_a, family_b, score, comment) VALUES
('Citrus', 'Floral', 1, 'Fresh floral blends well with citrus — classic cologne structure'),
('Citrus', 'Fresh', 2, 'Citrus and fresh notes create bright, uplifting openings'),
('Citrus', 'Aquatic', 1, 'Light marine citrus — popular in summer fragrances'),
('Citrus', 'Woody', 0, 'Neutral transition — citrus brightens heavy woods'),
('Citrus', 'Spicy', 0, 'Can work but needs a bridge note'),
('Citrus', 'Gourmand', -1, 'Citrus and sweet gourmand rarely complement each other'),
('Citrus', 'Oriental', -1, 'Citrus lacks depth for oriental blends'),
('Citrus', 'Leather', -1, 'Bright citrus clashes with heavy leather'),

('Floral', 'Woody', 1, 'Classic elegant structure — flowers need woody base'),
('Floral', 'Gourmand', 1, 'Gourmand floral creates modern feminine scents'),
('Floral', 'Oriental', 1, 'Rich floral oriental — classic luxury perfume profile'),
('Floral', 'Spicy', 0, 'Depends on specific flowers — rose and saffron work well'),
('Floral', 'Fresh', 0, 'Light floral freshness — common in modern perfumery'),
('Floral', 'Aquatic', 0, 'Can create clean floral but risks being generic'),
('Floral', 'Leather', -1, 'Heavy floral leather often overwhelms the composition'),
('Floral', 'Citrus', 1, 'See Citrus + Floral'),

('Woody', 'Leather', 2, 'Perfect base pair — warm, deep, long-lasting'),
('Woody', 'Oriental', 1, 'Warm woody oriental — rich and sophisticated'),
('Woody', 'Spicy', 1, 'Spicy woods create warmth and complexity'),
('Woody', 'Floral', 1, 'See Floral + Woody'),
('Woody', 'Citrus', 0, 'See Citrus + Woody'),
('Woody', 'Fresh', 0, 'Fresh woods work but lack character'),
('Woody', 'Gourmand', 0, 'Can create creamy woody scents'),
('Woody', 'Aquatic', -1, 'Aquatic notes dilute woody warmth'),

('Spicy', 'Oriental', 2, 'Perfect synergy — spicy oriental is a classic accord'),
('Spicy', 'Woody', 1, 'See Woody + Spicy'),
('Spicy', 'Leather', 1, 'Spicy leather — bold and sophisticated'),
('Spicy', 'Floral', 0, 'See Floral + Spicy'),
('Spicy', 'Gourmand', 0, 'Spicy gourmand works in winter fragrances'),
('Spicy', 'Fresh', -1, 'Spice overpowers fresh notes'),
('Spicy', 'Aquatic', -1, 'Aquatic and spicy create an unpleasant contrast'),
('Spicy', 'Citrus', 0, 'See Citrus + Spicy'),

('Gourmand', 'Oriental', 2, 'Sweet oriental — perfect for amber gourmand profiles'),
('Gourmand', 'Floral', 1, 'See Floral + Gourmand'),
('Gourmand', 'Woody', 0, 'See Woody + Gourmand'),
('Gourmand', 'Spicy', 0, 'See Spicy + Gourmand'),
('Gourmand', 'Fresh', -1, 'Sweet and fresh rarely work together'),
('Gourmand', 'Aquatic', -1, 'Gourmand aquatic is conceptually dissonant'),
('Gourmand', 'Leather', -1, 'Sweet leather is an acquired taste'),
('Gourmand', 'Citrus', -1, 'See Citrus + Gourmand'),

('Oriental', 'Spicy', 2, 'See Spicy + Oriental'),
('Oriental', 'Woody', 1, 'See Woody + Oriental'),
('Oriental', 'Gourmand', 2, 'See Gourmand + Oriental'),
('Oriental', 'Floral', 1, 'See Floral + Oriental'),
('Oriental', 'Leather', 1, 'Oriental leather — rich and opulent'),
('Oriental', 'Fresh', -1, 'Fresh notes disrupt oriental depth'),
('Oriental', 'Aquatic', -1, 'Aquatic oriental loses both characters'),
('Oriental', 'Citrus', -1, 'See Citrus + Oriental'),

('Fresh', 'Citrus', 2, 'See Citrus + Fresh'),
('Fresh', 'Aquatic', 1, 'Fresh aquatic — classic clean scent profile'),
('Fresh', 'Floral', 0, 'See Floral + Fresh'),
('Fresh', 'Woody', 0, 'See Woody + Fresh'),
('Fresh', 'Spicy', -1, 'See Spicy + Fresh'),
('Fresh', 'Gourmand', -1, 'See Gourmand + Fresh'),
('Fresh', 'Oriental', -1, 'See Oriental + Fresh'),
('Fresh', 'Leather', -1, 'Fresh and leather create an odd contrast'),

('Leather', 'Woody', 2, 'See Woody + Leather'),
('Leather', 'Spicy', 1, 'See Spicy + Leather'),
('Leather', 'Oriental', 1, 'See Oriental + Leather'),
('Leather', 'Floral', -1, 'See Floral + Leather'),
('Leather', 'Citrus', -1, 'See Citrus + Leather'),
('Leather', 'Fresh', -1, 'See Fresh + Leather'),
('Leather', 'Gourmand', -1, 'See Gourmand + Leather'),
('Leather', 'Aquatic', -1, 'Aquatic leather is chemically dissonant'),

('Aquatic', 'Fresh', 1, 'See Fresh + Aquatic'),
('Aquatic', 'Citrus', 1, 'See Citrus + Aquatic'),
('Aquatic', 'Floral', 0, 'See Floral + Aquatic'),
('Aquatic', 'Woody', -1, 'See Woody + Aquatic'),
('Aquatic', 'Spicy', -1, 'See Spicy + Aquatic'),
('Aquatic', 'Gourmand', -1, 'See Gourmand + Aquatic'),
('Aquatic', 'Oriental', -1, 'See Oriental + Aquatic'),
('Aquatic', 'Leather', -1, 'See Leather + Aquatic');
