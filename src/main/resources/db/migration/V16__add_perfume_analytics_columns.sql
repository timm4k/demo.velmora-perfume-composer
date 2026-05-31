ALTER TABLE perfumes
  ADD COLUMN IF NOT EXISTS release_year INTEGER,
  ADD COLUMN IF NOT EXISTS style VARCHAR(60),
  ADD COLUMN IF NOT EXISTS complexity VARCHAR(20),
  ADD COLUMN IF NOT EXISTS accords TEXT,
  ADD COLUMN IF NOT EXISTS occasions TEXT;

-- 1 Nasomatto Black Afgano
UPDATE perfumes SET style='Dark Woody', complexity='Complex', accords='Woody, Smoky, Coffee, Dark', occasions='Formal Events, Night Out', release_year=2013 WHERE name ILIKE '%Black Afgano%';

-- 2 Nasomatto Sadonaso
UPDATE perfumes SET style='Spicy Woody', complexity='Complex', accords='Spicy, Woody, Powdery', occasions='Night Out, Cold Weather', release_year=2015 WHERE name ILIKE '%Sadonaso%';

-- 3 Chanel N°5
UPDATE perfumes SET style='Aldehydic Floral', complexity='Complex', accords='Aldehydic, Floral, Powdery, Soapy', occasions='Formal Events, Evening', release_year=1921 WHERE name ILIKE '%N°5%' AND brand ILIKE '%Chanel%';

-- 4 YSL Libre
UPDATE perfumes SET style='Lavender Oriental', complexity='Moderate', accords='Lavender, Vanilla, Floral, Fresh', occasions='Daily Wear, Office, Date Night', release_year=2019 WHERE name ILIKE '%Libre%';

-- 5 Roja Elysium
UPDATE perfumes SET style='Fresh Aromatic', complexity='Moderate', accords='Citrus, Aromatic, Woody, Fresh', occasions='Daily Wear, Office, Summer', release_year=2018 WHERE name ILIKE '%Elysium%';

-- 6 Lacoste L.12.12 Noir Intense
UPDATE perfumes SET style='Aromatic Fresh', complexity='Moderate', accords='Fresh, Aquatic, Woody', occasions='Sport, Daily Wear', release_year=2015 WHERE name ILIKE '%Noir Intense%';

-- 7 Armani Si Passione
UPDATE perfumes SET style='Fruity Floral', complexity='Moderate', accords='Fruity, Floral, Vanilla, Sweet', occasions='Daily Wear, Romantic', release_year=2018 WHERE brand ILIKE '%Armani%' AND name ILIKE '%Si Passione%';

-- 8 Amouage Guidance
UPDATE perfumes SET style='Floral Woody', complexity='Complex', accords='Floral, Woody, Amber, Spicy', occasions='Formal Events, Evening', release_year=2021 WHERE brand ILIKE '%Amouage%' AND name ILIKE '%Guidance%';

-- 9 Armani Acqua Di Gioia
UPDATE perfumes SET style='Aquatic Fresh', complexity='Simple', accords='Aquatic, Citrus, Fresh, Green', occasions='Daily Wear, Summer, Sport', release_year=2010 WHERE brand ILIKE '%Armani%' AND name ILIKE '%Acqua Di Gioia%';

-- 10 Orto Parisi Megamare
UPDATE perfumes SET style='Aromatic Maritime', complexity='Complex', accords='Sea Salt, Amber, Aromatic, Mineral', occasions='Summer, Beach, Statement', release_year=2018 WHERE name ILIKE '%Megamare%';

-- 11 Tiziana Terenzi Porpora
UPDATE perfumes SET style='Floral Oriental', complexity='Complex', accords='Floral, Oriental, Spicy, Amber', occasions='Evening, Formal Events', release_year=2015 WHERE name ILIKE '%Porpora%';

-- 12 Orto Parisi Brutus
UPDATE perfumes SET style='Leather Woody', complexity='Complex', accords='Leather, Woody, Dark, Spicy', occasions='Night Out, Cold Weather', release_year=2017 WHERE name ILIKE '%Brutus%';

-- 13 Armani Code
UPDATE perfumes SET style='Aromatic Spicy', complexity='Moderate', accords='Aromatic, Spicy, Leather, Warm', occasions='Date Night, Evening', release_year=2004 WHERE brand ILIKE '%Armani%' AND name ILIKE '%Code%';

-- 14 Paco Rabanne Lady Million
UPDATE perfumes SET style='Fruity Floral', complexity='Moderate', accords='Fruity, Floral, Raspberry, Honey', occasions='Date Night, Evening, Party', release_year=2010 WHERE name ILIKE '%Lady Million%';

-- 15 D&G L'Imperatrice 3
UPDATE perfumes SET style='Fresh Fruity', complexity='Simple', accords='Watermelon, Fruity, Fresh, Aquatic', occasions='Daily Wear, Summer', release_year=2009 WHERE name ILIKE '%Imperatrice%';

-- 16 Paco Rabanne 1 Million
UPDATE perfumes SET style='Spicy Oriental', complexity='Moderate', accords='Spicy, Leather, Woody, Warm', occasions='Night Out, Party, Cold Weather', release_year=2008 WHERE name ILIKE '%1 Million%' AND brand ILIKE '%Paco%';

-- 17 D&G Light Blue
UPDATE perfumes SET style='Citrus Fresh', complexity='Simple', accords='Citrus, Floral, Woody, Fresh', occasions='Daily Wear, Summer, Office', release_year=2001 WHERE name ILIKE '%Light Blue%' AND brand ILIKE '%Dolce%' AND name NOT ILIKE '%Homme%';

-- 18 JPG Ultra Male
UPDATE perfumes SET style='Aromatic Spicy', complexity='Moderate', accords='Aromatic, Spicy, Vanilla, Fresh', occasions='Night Out, Party, Date Night', release_year=2015 WHERE name ILIKE '%Ultra Male%';

-- 19 Hermes Terre d'Hermes
UPDATE perfumes SET style='Earthy Woody', complexity='Complex', accords='Earthy, Woody, Citrus, Mineral', occasions='Daily Wear, Office, All Seasons', release_year=2006 WHERE name ILIKE '%Terre%';

-- 20 Dior Fahrenheit
UPDATE perfumes SET style='Leather Woody', complexity='Complex', accords='Leather, Woody, Spicy, Gasoline', occasions='Cold Weather, Evening, Statement', release_year=1988 WHERE name ILIKE '%Fahrenheit%';

-- 21 Chanel Allure Homme Sport
UPDATE perfumes SET style='Fresh Aromatic', complexity='Moderate', accords='Fresh, Aquatic, Aromatic, Citrus', occasions='Sport, Daily Wear, Summer', release_year=2004 WHERE name ILIKE '%Allure Homme Sport%';

-- 22 Chanel Egoiste Platinum
UPDATE perfumes SET style='Aromatic Woody', complexity='Moderate', accords='Aromatic, Woody, Lavender, Fresh', occasions='Office, Daily Wear', release_year=1993 WHERE name ILIKE '%Egoiste Platinum%';

-- 23 Burberry Weekend
UPDATE perfumes SET style='Fresh Citrus', complexity='Simple', accords='Citrus, Fresh, Woody, Light', occasions='Weekend, Casual, Summer', release_year=1997 WHERE name ILIKE '%Weekend%';

-- 24 Lacoste L.12.12 Jaune
UPDATE perfumes SET style='Fresh Fruity', complexity='Simple', accords='Fresh, Fruity, Woody, Aquatic', occasions='Sport, Daily Wear, Summer', release_year=2013 WHERE name ILIKE '%Jaune%';

-- 25 D&G Light Blue Pour Homme
UPDATE perfumes SET style='Citrus Aromatic', complexity='Moderate', accords='Citrus, Woody, Aromatic, Fresh', occasions='Daily Wear, Summer, Office', release_year=2007 WHERE name ILIKE '%Light Blue Pour Homme%';

-- 26 Clinique Happy for Men
UPDATE perfumes SET style='Fresh Citrus', complexity='Simple', accords='Citrus, Fresh, Aromatic, Clean', occasions='Daily Wear, Office, Sport', release_year=1999 WHERE name ILIKE '%Happy for Men%';

-- 27 YSL Black Opium
UPDATE perfumes SET style='Oriental Gourmand', complexity='Moderate', accords='Coffee, Vanilla, Floral, Sweet', occasions='Night Out, Evening, Cold Weather', release_year=2014 WHERE name ILIKE '%Black Opium%';

-- 28 Carolina Herrera 212 VIP Black
UPDATE perfumes SET style='Spicy Leather', complexity='Moderate', accords='Spicy, Leather, Rum, Warm', occasions='Night Out, Party, Evening', release_year=2012 WHERE name ILIKE '%212 VIP%';

-- 29 MFK Baccarat Rouge 540
UPDATE perfumes SET style='Amber Saffron', complexity='Complex', accords='Saffron, Amber, Woody, Sweet', occasions='Evening, Formal Events, Statement', release_year=2015 WHERE name ILIKE '%Baccarat Rouge%';

-- 30 Byredo Bal d'Afrique
UPDATE perfumes SET style='Citrus Woody', complexity='Moderate', accords='Citrus, Woody, Floral, Warm', occasions='Daily Wear, Office, Spring', release_year=2010 WHERE name ILIKE '%Bal d''Afrique%';

-- 31 Le Labo Santal 33
UPDATE perfumes SET style='Woody Aromatic', complexity='Moderate', accords='Woody, Leather, Spicy, Warm', occasions='Daily Wear, Office, All Seasons', release_year=2011 WHERE name ILIKE '%Santal 33%';

-- 32 Kilian Angels' Share
UPDATE perfumes SET style='Gourmand Spicy', complexity='Moderate', accords='Cognac, Cinnamon, Vanilla, Sweet', occasions='Evening, Cold Weather, Romantic', release_year=2018 WHERE name ILIKE '%Angels'' Share%';

-- 33 Parfums de Marly Delina
UPDATE perfumes SET style='Floral Fruity', complexity='Moderate', accords='Floral, Fruity, Vanilla, Powdery', occasions='Date Night, Spring, Romantic', release_year=2017 WHERE name ILIKE '%Delina%';

-- 34 Tom Ford Lost Cherry
UPDATE perfumes SET style='Gourmand Fruity', complexity='Moderate', accords='Cherry, Vanilla, Sweet, Warm', occasions='Date Night, Evening, Romantic', release_year=2018 WHERE brand ILIKE '%Tom Ford%' AND name ILIKE '%Lost Cherry%';

-- 35 Creed Aventus
UPDATE perfumes SET style='Fruity Aromatic', complexity='Complex', accords='Fruity, Woody, Smoky, Fresh', occasions='Daily Wear, Office, Statement', release_year=2010 WHERE name ILIKE '%Aventus%';

-- 36 Dior Sauvage
UPDATE perfumes SET style='Aromatic Spicy', complexity='Moderate', accords='Aromatic, Spicy, Fresh, Amber', occasions='Daily Wear, Office, Evening', release_year=2015 WHERE brand ILIKE '%Dior%' AND name ILIKE '%Sauvage%';

-- 37 Viktor&Rolf Flowerbomb
UPDATE perfumes SET style='Floral Oriental', complexity='Moderate', accords='Floral, Sweet, Oriental, Powdery', occasions='Evening, Romantic, Party', release_year=2005 WHERE name ILIKE '%Flowerbomb%';

-- 38 Tom Ford Oud Wood
UPDATE perfumes SET style='Woody Spicy', complexity='Moderate', accords='Woody, Spicy, Warm, Smooth', occasions='Evening, Cold Weather, Formal', release_year=2007 WHERE brand ILIKE '%Tom Ford%' AND name ILIKE '%Oud Wood%';

-- 39 Juliette Has A Gun Not A Perfume
UPDATE perfumes SET style='Clean Musk', complexity='Simple', accords='Musk, Clean, Minimal, Soft', occasions='Daily Wear, Office, Layering', release_year=2010 WHERE name ILIKE '%Not A Perfume%';

-- 40 Escentric Molecules Molecule 01
UPDATE perfumes SET style='Woody Minimal', complexity='Simple', accords='Woody, Clean, Iso E Super, Warm', occasions='Daily Wear, Office, Layering', release_year=2006 WHERE name ILIKE '%Molecule 01%';

-- 41 Memo African Leather
UPDATE perfumes SET style='Spicy Leather', complexity='Complex', accords='Leather, Spicy, Woody, Warm', occasions='Evening, Cold Weather, Statement', release_year=2013 WHERE name ILIKE '%African Leather%';

-- 42 Xerjoff Erba Pura
UPDATE perfumes SET style='Fruity Oriental', complexity='Moderate', accords='Fruity, Vanilla, Oriental, Sweet', occasions='Summer, Party, Daily Wear', release_year=2015 WHERE name ILIKE '%Erba Pura%';

-- 43 Tiziana Terenzi Kirke
UPDATE perfumes SET style='Fruity Floral', complexity='Moderate', accords='Fruity, Floral, Amber, Sweet', occasions='Summer, Evening, Romantic', release_year=2014 WHERE name ILIKE '%Kirke%';

-- 44 Marc Jacobs Daisy
UPDATE perfumes SET style='Fresh Floral', complexity='Simple', accords='Floral, Fresh, Fruity, Clean', occasions='Daily Wear, Spring, Casual', release_year=2007 WHERE name ILIKE '%Daisy%';

-- 45 Versace Bright Crystal
UPDATE perfumes SET style='Fresh Floral', complexity='Simple', accords='Floral, Fresh, Aquatic, Clean', occasions='Daily Wear, Summer, Office', release_year=2006 WHERE name ILIKE '%Bright Crystal%';

-- 46 Lancome La Vie Est Belle
UPDATE perfumes SET style='Sweet Gourmand', complexity='Moderate', accords='Vanilla, Sweet, Floral, Powdery', occasions='Daily Wear, Romantic, Evening', release_year=2012 WHERE name ILIKE '%La Vie Est Belle%';

-- 47 Narciso Rodriguez For Her Pure Musc
UPDATE perfumes SET style='Clean Floral', complexity='Moderate', accords='Floral, Musk, Clean, Soft', occasions='Daily Wear, Office, Romantic', release_year=2018 WHERE name ILIKE '%Pure Musc%';

-- 48 Jo Malone Wood Sage & Sea Salt
UPDATE perfumes SET style='Aromatic Fresh', complexity='Simple', accords='Aromatic, Woody, Salty, Fresh', occasions='Daily Wear, Summer, Weekend', release_year=2014 WHERE name ILIKE '%Wood Sage%';

-- 49 Guerlain Shalimar
UPDATE perfumes SET style='Oriental Spicy', complexity='Complex', accords='Oriental, Vanilla, Spicy, Powdery', occasions='Evening, Formal, Cold Weather', release_year=1925 WHERE name ILIKE '%Shalimar%';

-- 50 Prada Paradoxe
UPDATE perfumes SET style='Floral Fresh', complexity='Moderate', accords='Floral, Fresh, Woody, Clean', occasions='Daily Wear, Office, Spring', release_year=2022 WHERE name ILIKE '%Paradoxe%';

-- 51 Parfums de Marly Layton
UPDATE perfumes SET style='Aromatic Spicy', complexity='Moderate', accords='Aromatic, Spicy, Vanilla, Warm', occasions='Daily Wear, Office, Cold Weather', release_year=2016 WHERE name ILIKE '%Layton%';

-- 52 Penhaligon's Lord George
UPDATE perfumes SET style='Woody Spicy', complexity='Complex', accords='Woody, Spicy, Rum, Warm', occasions='Evening, Formal, Cold Weather', release_year=2019 WHERE name ILIKE '%Lord George%';

-- 53 Tom Ford Tobacco Vanille
UPDATE perfumes SET style='Sweet Tobacco', complexity='Moderate', accords='Tobacco, Vanilla, Sweet, Warm', occasions='Evening, Cold Weather, Romantic', release_year=2007 WHERE brand ILIKE '%Tom Ford%' AND name ILIKE '%Tobacco Vanille%';

-- 54 Byredo Gypsy Water
UPDATE perfumes SET style='Woody Fresh', complexity='Moderate', accords='Woody, Fresh, Citrus, Aromatic', occasions='Daily Wear, Spring, Summer', release_year=2008 WHERE name ILIKE '%Gypsy Water%';

-- 55 Frederic Malle Portrait of a Lady
UPDATE perfumes SET style='Spicy Floral', complexity='Complex', accords='Floral, Spicy, Woody, Rich', occasions='Evening, Formal, Romantic', release_year=2010 WHERE name ILIKE '%Portrait of a Lady%';

-- 56 Diptyque Philosykos
UPDATE perfumes SET style='Green Woody', complexity='Moderate', accords='Green, Woody, Fig, Fresh', occasions='Daily Wear, Summer, Spring', release_year=1996 WHERE name ILIKE '%Philosykos%';

-- 57 Maison Margiela Jazz Club
UPDATE perfumes SET style='Spicy Warm', complexity='Moderate', accords='Spicy, Tobacco, Rum, Warm', occasions='Evening, Cold Weather, Casual', release_year=2013 WHERE name ILIKE '%Jazz Club%';

-- 58 Maison Margiela By the Fireplace
UPDATE perfumes SET style='Smoky Gourmand', complexity='Moderate', accords='Smoky, Vanilla, Warm, Sweet', occasions='Evening, Cold Weather, Cozy', release_year=2015 WHERE name ILIKE '%By the Fireplace%';

-- 59 Kilian Good Girl Gone Bad
UPDATE perfumes SET style='Floral Fruity', complexity='Moderate', accords='Floral, Fruity, Musk, Fresh', occasions='Date Night, Evening, Spring', release_year=2012 WHERE name ILIKE '%Good Girl Gone Bad%';

-- 60 Le Labo Another 13
UPDATE perfumes SET style='Warm Musk', complexity='Moderate', accords='Musk, Woody, Amber, Clean', occasions='Daily Wear, Office, All Seasons', release_year=2010 WHERE name ILIKE '%Another 13%';

-- 61 Jo Malone English Pear & Freesia
UPDATE perfumes SET style='Fresh Fruity', complexity='Simple', accords='Fruity, Floral, Fresh, Clean', occasions='Daily Wear, Office, Spring', release_year=2010 WHERE name ILIKE '%English Pear%';

-- 62 Chanel Bleu de Chanel
UPDATE perfumes SET style='Aromatic Fresh', complexity='Moderate', accords='Aromatic, Citrus, Woody, Fresh', occasions='Daily Wear, Office, All Seasons', release_year=2010 WHERE name ILIKE '%Bleu de Chanel%';

-- 63 Dior Miss Dior
UPDATE perfumes SET style='Floral Green', complexity='Moderate', accords='Floral, Green, Powdery, Fresh', occasions='Daily Wear, Spring, Romantic', release_year=2017 WHERE brand ILIKE '%Dior%' AND name ILIKE '%Miss Dior%';

-- 64 Bvlgari Tygar
UPDATE perfumes SET style='Citrus Aromatic', complexity='Moderate', accords='Citrus, Ginger, Amber, Fresh', occasions='Daily Wear, Summer, Office', release_year=2020 WHERE name ILIKE '%Tygar%';

-- 65 Amouage Interlude Man
UPDATE perfumes SET style='Dark Oriental', complexity='Complex', accords='Oriental, Spicy, Woody, Dark', occasions='Evening, Cold Weather, Statement', release_year=2012 WHERE name ILIKE '%Interlude Man%';
