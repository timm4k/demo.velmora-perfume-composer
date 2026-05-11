-- Довідник інгредієнтів. тип (фаза розкриття), інтенсивність (стійкість), опис
-- розкриття аромату TOP (0-15хв), HEART (15хв-3год), BASE (3год+)

-- TOP
INSERT INTO notes (name, category, note_type, intensity, description, color_code) VALUES
('Bergamot', 'Citrus', 'TOP', 4, 'Bright, citrusy and slightly floral scent.', '#DFFF00'),
('Lemon', 'Citrus', 'TOP', 3, 'Sharp, fresh and energetic citrus.', '#FFF700'),
('Sour Cherry', 'Fruit', 'TOP', 6, 'Sweet and tart dark cherry aroma.', '#800020'),
('Cannabis', 'Green', 'TOP', 9, 'Provocative, herbal and earthy green note.', '#2E8B57'),
('Watermelon', 'Fruit', 'TOP', 2, 'Refreshing, watery and sweet summer fruit.', '#FF7F50'),
('Pink Pepper', 'Spicy', 'TOP', 5, 'Spicy, rosy and slightly woody pepper.', '#FFC0CB'),
('Mandarin', 'Citrus', 'TOP', 4, 'Sweet, juicy and sunny citrus.', '#FFA500'),
('Mint', 'Green', 'TOP', 4, 'Cool, fresh and peppery aromatic leaf.', '#98FF98'),
('Sea Salt', 'Ozonic', 'TOP', 3, 'Mineral, salty and breezy marine note.', '#AFEEEE'),
('Apple', 'Fruit', 'TOP', 4, 'Crisp, fresh and slightly tart fruit.', '#ADFF2F'),
('Pear', 'Fruit', 'TOP', 4, 'Sweet, juicy and mellow fruity aroma.', '#98FB98'),
('Aldehydes', 'Synthetic', 'TOP', 8, 'Clean, soapy and sparkling airy sensation.', '#F0FFFF'),
('Lavender', 'Floral', 'TOP', 6, 'Aromatic, herbal and clean relaxing flower.', '#E6E6FA'),
('Fig Leaf', 'Green', 'TOP', 5, 'Bitter, green and milky Mediterranean leaf.', '#6B8E23');

-- HEART
INSERT INTO notes (name, category, note_type, intensity, description, color_code) VALUES
('Turkish Rose', 'Floral', 'HEART', 8, 'Velvety, honey-like and rich classical rose.', '#E0115F'),
('Jasmine Sambac', 'Floral', 'HEART', 7, 'Opulent, warm and animalic white floral.', '#FFFFFF'),
('Coffee', 'Gourmand', 'HEART', 7, 'Dark, roasted and slightly bitter beans.', '#6F4E37'),
('Saffron', 'Spicy', 'HEART', 9, 'Bittersweet, leathery and earthy luxury spice.', '#FFD700'),
('Tobacco', 'Tobacco', 'HEART', 8, 'Rich, smoky, sweet and hay-like leaf.', '#8B4513'),
('Rum', 'Gourmand', 'HEART', 7, 'Boozy, sweet and warm sugar cane spirit.', '#CD853F'),
('Cinnamon', 'Spicy', 'HEART', 8, 'Warm, powdery and sweet woody spice.', '#D2691E'),
('Orris Root', 'Floral', 'HEART', 9, 'Powdery, earthy and violet-like luxury root.', '#EBDDE2'),
('Honey', 'Gourmand', 'HEART', 7, 'Sweet, balsamic and animalic golden nectar.', '#FFD700'),
('Incense', 'Smoky', 'HEART', 9, 'Mystical, smoky and resinous balsamic scent.', '#4B3621');

-- BASE
INSERT INTO notes (name, category, note_type, intensity, description, color_code) VALUES
('Oud (Agarwood)', 'Woody', 'BASE', 10, 'Deep, resinous and smoky precious wood.', '#2C1B18'),
('Vanilla', 'Gourmand', 'BASE', 7, 'Sweet, creamy and comforting bean.', '#F3E5AB'),
('White Musk', 'Musky', 'BASE', 5, 'Clean, powdery and soft skin-like aroma.', '#F5F5F5'),
('Amber', 'Amber', 'BASE', 9, 'Warm, resinous and sweet fossilized resin.', '#FFBF00'),
('Patchouli', 'Earthy', 'BASE', 8, 'Dark, woody and moist earth scent.', '#4E3B31'),
('Sandalwood', 'Woody', 'BASE', 8, 'Creamy, milky and warm woody note.', '#E3C9A1'),
('Leather', 'Leather', 'BASE', 9, 'Smoky, dry and masculine skin aroma.', '#3E2723'),
('Oakmoss', 'Earthy', 'BASE', 8, 'Inky, bitter and forest-like damp moss.', '#556B2F'),
('Vetiver', 'Woody', 'BASE', 7, 'Dry, earthy and smoky woody grass root.', '#708238'),
('Cedarwood', 'Woody', 'BASE', 6, 'Dry, clean and pencil-shaving woody scent.', '#C19A6B');