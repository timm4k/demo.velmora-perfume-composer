INSERT INTO compositions (user_id, perfume_id, name, description, is_public)
SELECT NULL, id, name, 'Official fragrance pyramid ' || name, TRUE FROM perfumes;

-- наповнення інгредієнтами
INSERT INTO composition_items (composition_id, note_id, percentage) VALUES
((SELECT id FROM compositions WHERE name ILIKE '%Black Afgano%'), (SELECT id FROM notes WHERE name = 'Cannabis'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Black Afgano%'), (SELECT id FROM notes WHERE name = 'Coffee'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Black Afgano%'), (SELECT id FROM notes WHERE name = 'Oud (Agarwood)'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%Sadonaso%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Sadonaso%'), (SELECT id FROM notes WHERE name = 'Coffee'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Sadonaso%'), (SELECT id FROM notes WHERE name = 'White Musk'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Baccarat Rouge 540%'), (SELECT id FROM notes WHERE name = 'Saffron'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Baccarat Rouge 540%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Baccarat Rouge 540%'), (SELECT id FROM notes WHERE name = 'Amber'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Bal d''Afrique%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Bal d''Afrique%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Bal d''Afrique%'), (SELECT id FROM notes WHERE name = 'Amber'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Santal 33%'), (SELECT id FROM notes WHERE name = 'Fig Leaf'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Santal 33%'), (SELECT id FROM notes WHERE name = 'Orris Root'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Santal 33%'), (SELECT id FROM notes WHERE name = 'Sandalwood'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%N°5%'), (SELECT id FROM notes WHERE name = 'Aldehydes'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%N°5%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%N°5%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Libre%'), (SELECT id FROM notes WHERE name = 'Lavender'), 35),
((SELECT id FROM compositions WHERE name ILIKE '%Libre%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 25),
((SELECT id FROM compositions WHERE name ILIKE '%Libre%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Elysium%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Elysium%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Elysium%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Sauvage%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Sauvage%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Sauvage%'), (SELECT id FROM notes WHERE name = 'Amber'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Si Passione%'), (SELECT id FROM notes WHERE name = 'Pear'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Si Passione%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Si Passione%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Noir Intense%'), (SELECT id FROM notes WHERE name = 'Watermelon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Noir Intense%'), (SELECT id FROM notes WHERE name = 'Lavender'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Noir Intense%'), (SELECT id FROM notes WHERE name = 'Patchouli'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Acqua Di Gioia%'), (SELECT id FROM notes WHERE name = 'Lemon'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Acqua Di Gioia%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Acqua Di Gioia%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Megamare%'), (SELECT id FROM notes WHERE name = 'Sea Salt'), 50),
((SELECT id FROM compositions WHERE name ILIKE '%Megamare%'), (SELECT id FROM notes WHERE name = 'Amber'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%Armani Code%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Armani Code%'), (SELECT id FROM notes WHERE name = 'Lavender'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Armani Code%'), (SELECT id FROM notes WHERE name = 'Leather'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Ultra Male%'), (SELECT id FROM notes WHERE name = 'Pear'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Ultra Male%'), (SELECT id FROM notes WHERE name = 'Cinnamon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Ultra Male%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Terre d’Hermes%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Terre d’Hermes%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Terre d’Hermes%'), (SELECT id FROM notes WHERE name = 'Vetiver'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Fahrenheit%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Fahrenheit%'), (SELECT id FROM notes WHERE name = 'Lavender'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Fahrenheit%'), (SELECT id FROM notes WHERE name = 'Leather'), 60),

((SELECT id FROM compositions WHERE name ILIKE '%Allure Homme Sport%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Allure Homme Sport%'), (SELECT id FROM notes WHERE name = 'Sea Salt'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Allure Homme Sport%'), (SELECT id FROM notes WHERE name = 'White Musk'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Bleu de Chanel%'), (SELECT id FROM notes WHERE name = 'Lemon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Bleu de Chanel%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Bleu de Chanel%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%1 Million%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%1 Million%'), (SELECT id FROM notes WHERE name = 'Cinnamon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%1 Million%'), (SELECT id FROM notes WHERE name = 'Leather'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Light Blue (Eau de Toilette)%'), (SELECT id FROM notes WHERE name = 'Lemon'), 35),
((SELECT id FROM compositions WHERE name ILIKE '%Light Blue (Eau de Toilette)%'), (SELECT id FROM notes WHERE name = 'Apple'), 35),
((SELECT id FROM compositions WHERE name ILIKE '%Light Blue (Eau de Toilette)%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%L’Imperatrice%'), (SELECT id FROM notes WHERE name = 'Watermelon'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%L’Imperatrice%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%L’Imperatrice%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Black Opium%'), (SELECT id FROM notes WHERE name = 'Pear'), 25),
((SELECT id FROM compositions WHERE name ILIKE '%Black Opium%'), (SELECT id FROM notes WHERE name = 'Coffee'), 35),
((SELECT id FROM compositions WHERE name ILIKE '%Black Opium%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Good Girl Gone Bad%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Good Girl Gone Bad%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Good Girl Gone Bad%'), (SELECT id FROM notes WHERE name = 'Amber'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Delina%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 25),
((SELECT id FROM compositions WHERE name ILIKE '%Delina%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 45),
((SELECT id FROM compositions WHERE name ILIKE '%Delina%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Lost Cherry%'), (SELECT id FROM notes WHERE name = 'Sour Cherry'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Lost Cherry%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Lost Cherry%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Angels%Share%'), (SELECT id FROM notes WHERE name = 'Rum'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Angels%Share%'), (SELECT id FROM notes WHERE name = 'Cinnamon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Angels%Share%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Aventus%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 25),
((SELECT id FROM compositions WHERE name ILIKE '%Aventus%'), (SELECT id FROM notes WHERE name = 'Apple'), 25),
((SELECT id FROM compositions WHERE name ILIKE '%Aventus%'), (SELECT id FROM notes WHERE name = 'Oakmoss'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%Flowerbomb%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Flowerbomb%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Flowerbomb%'), (SELECT id FROM notes WHERE name = 'Patchouli'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Not A Perfume%'), (SELECT id FROM notes WHERE name = 'White Musk'), 100),

((SELECT id FROM compositions WHERE name ILIKE '%Molecule 01%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 100),

((SELECT id FROM compositions WHERE name ILIKE '%Kirke%'), (SELECT id FROM notes WHERE name = 'Pear'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Kirke%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Kirke%'), (SELECT id FROM notes WHERE name = 'White Musk'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Erba Pura%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Erba Pura%'), (SELECT id FROM notes WHERE name = 'Lemon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Erba Pura%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Tygar%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Tygar%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Tygar%'), (SELECT id FROM notes WHERE name = 'Amber'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Guidance%'), (SELECT id FROM notes WHERE name = 'Pear'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Guidance%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Guidance%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Tobacco Vanille%'), (SELECT id FROM notes WHERE name = 'Tobacco'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Tobacco Vanille%'), (SELECT id FROM notes WHERE name = 'Cinnamon'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Tobacco Vanille%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Oud Wood%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Oud Wood%'), (SELECT id FROM notes WHERE name = 'Sandalwood'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Oud Wood%'), (SELECT id FROM notes WHERE name = 'Oud (Agarwood)'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%Another 13%'), (SELECT id FROM notes WHERE name = 'Pear'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Another 13%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Another 13%'), (SELECT id FROM notes WHERE name = 'Amber'), 60),

((SELECT id FROM compositions WHERE name ILIKE '%Gypsy Water%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Gypsy Water%'), (SELECT id FROM notes WHERE name = 'Lavender'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Gypsy Water%'), (SELECT id FROM notes WHERE name = 'Sandalwood'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Portrait of a Lady%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 50),
((SELECT id FROM compositions WHERE name ILIKE '%Portrait of a Lady%'), (SELECT id FROM notes WHERE name = 'Patchouli'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%Philosykos%'), (SELECT id FROM notes WHERE name = 'Fig Leaf'), 60),
((SELECT id FROM compositions WHERE name ILIKE '%Philosykos%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Jazz Club%'), (SELECT id FROM notes WHERE name = 'Rum'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Jazz Club%'), (SELECT id FROM notes WHERE name = 'Tobacco'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Jazz Club%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%By the Fireplace%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%By the Fireplace%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%By the Fireplace%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%English Pear%'), (SELECT id FROM notes WHERE name = 'Pear'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%English Pear%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%English Pear%'), (SELECT id FROM notes WHERE name = 'Patchouli'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Interlude Man%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Interlude Man%'), (SELECT id FROM notes WHERE name = 'Amber'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Interlude Man%'), (SELECT id FROM notes WHERE name = 'Oud (Agarwood)'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Lord George%'), (SELECT id FROM notes WHERE name = 'Rum'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Lord George%'), (SELECT id FROM notes WHERE name = 'Amber'), 35),
((SELECT id FROM compositions WHERE name ILIKE '%Lord George%'), (SELECT id FROM notes WHERE name = 'Sandalwood'), 35),

((SELECT id FROM compositions WHERE name ILIKE '%Layton%'), (SELECT id FROM notes WHERE name = 'Apple'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Layton%'), (SELECT id FROM notes WHERE name = 'Lavender'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Layton%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Paradoxe%'), (SELECT id FROM notes WHERE name = 'Pear'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Paradoxe%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Paradoxe%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Shalimar%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Shalimar%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 20),
((SELECT id FROM compositions WHERE name ILIKE '%Shalimar%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 50),

((SELECT id FROM compositions WHERE name ILIKE '%Daisy%'), (SELECT id FROM notes WHERE name = 'Apple'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Daisy%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Daisy%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Porpora%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Porpora%'), (SELECT id FROM notes WHERE name = 'Amber'), 60),

((SELECT id FROM compositions WHERE name ILIKE '%Brutus%'), (SELECT id FROM notes WHERE name = 'Bergamot'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Brutus%'), (SELECT id FROM notes WHERE name = 'Patchouli'), 70),

((SELECT id FROM compositions WHERE name ILIKE '%Lady Million%'), (SELECT id FROM notes WHERE name = 'Lemon'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Lady Million%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Lady Million%'), (SELECT id FROM notes WHERE name = 'Honey'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Light Blue Pour Homme%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Light Blue Pour Homme%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Light Blue Pour Homme%'), (SELECT id FROM notes WHERE name = 'Oakmoss'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Happy for Men%'), (SELECT id FROM notes WHERE name = 'Lemon'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Happy for Men%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Happy for Men%'), (SELECT id FROM notes WHERE name = 'Sea Salt'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Weekend%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Weekend%'), (SELECT id FROM notes WHERE name = 'Pear'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Weekend%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Egoiste Platinum%'), (SELECT id FROM notes WHERE name = 'Lavender'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Egoiste Platinum%'), (SELECT id FROM notes WHERE name = 'Cedarwood'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Egoiste Platinum%'), (SELECT id FROM notes WHERE name = 'Amber'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%L.12.12 Jaune%'), (SELECT id FROM notes WHERE name = 'Apple'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%L.12.12 Jaune%'), (SELECT id FROM notes WHERE name = 'Pink Pepper'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%L.12.12 Jaune%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%African Leather%'), (SELECT id FROM notes WHERE name = 'Saffron'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%African Leather%'), (SELECT id FROM notes WHERE name = 'Leather'), 70),

((SELECT id FROM compositions WHERE name ILIKE '%Bright Crystal%'), (SELECT id FROM notes WHERE name = 'Watermelon'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Bright Crystal%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Bright Crystal%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%La Vie Est Belle%'), (SELECT id FROM notes WHERE name = 'Pear'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%La Vie Est Belle%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%La Vie Est Belle%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 40),

((SELECT id FROM compositions WHERE name ILIKE '%Pure Musc%'), (SELECT id FROM notes WHERE name = 'Jasmine Sambac'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Pure Musc%'), (SELECT id FROM notes WHERE name = 'White Musk'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Pure Musc%'), (SELECT id FROM notes WHERE name = 'Amber'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%Miss Dior%'), (SELECT id FROM notes WHERE name = 'Mandarin'), 30),
((SELECT id FROM compositions WHERE name ILIKE '%Miss Dior%'), (SELECT id FROM notes WHERE name = 'Turkish Rose'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%Miss Dior%'), (SELECT id FROM notes WHERE name = 'White Musk'), 30),

((SELECT id FROM compositions WHERE name ILIKE '%212 VIP Black%'), (SELECT id FROM notes WHERE name = 'Lavender'), 40),
((SELECT id FROM compositions WHERE name ILIKE '%212 VIP Black%'), (SELECT id FROM notes WHERE name = 'Vanilla'), 60);