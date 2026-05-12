-- =============================================================================
-- Таблиця: users
-- Нормальна форма: 3НФ. Усі атрибути залежать від PK (id), немає транзитивних залежностей
-- Тип: Strong Entity (незалежна сутність). Основний об'єкт системи
-- =============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    is_confirmed BOOLEAN DEFAULT FALSE,
    invite_code VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- Таблиця: notes
-- Нормальна форма: 3НФ. Дані атомарні, кожен неключовий атрибут описує саме ноту
-- Тип: Strong Entity. Довідник інгредієнтів
-- =============================================================================
CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    note_type VARCHAR(20),
    intensity INT CHECK (intensity BETWEEN 1 AND 10),
    description TEXT,
    color_code VARCHAR(7)
);

-- =============================================================================
-- Таблиця: perfumes
-- Нормальна форма: 3НФ. Немає повторюваних груп або часткових залежностей
-- Тип: Strong Entity. Об'єкт каталогу
-- =============================================================================
CREATE TABLE perfumes (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2),
    rating DECIMAL(3, 1),
    availability BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(255)
);

-- =============================================================================
-- Таблиця: compositions
-- Нормальна форма: 3НФ. Користувач прив'язаний через FK, дані не дублюються
-- Тип: Weak Entity (залежна сутність).
-- ОПИС: Якщо user_id IS NULL — це офіційна формула парфуму з каталогу
-- =============================================================================
CREATE TABLE compositions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE, -- NULL для системних формул
    perfume_id BIGINT REFERENCES perfumes(id) ON DELETE CASCADE, -- NULL для авторських ароматів юзера
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_perfume_formula UNIQUE (perfume_id) -- Один парфум — одна офіційна піраміда
);

-- =============================================================================
-- Таблиця: composition_items
-- Нормальна форма: 3НФ. Реалізує зв'язок Many-to-Many
-- Тип: Associative Entity (асоціативна сутність). Поєднує Compositions та Notes
-- =============================================================================
CREATE TABLE composition_items (
    composition_id BIGINT REFERENCES compositions(id) ON DELETE CASCADE,
    note_id BIGINT REFERENCES notes(id) ON DELETE CASCADE,
    percentage INT NOT NULL,
    PRIMARY KEY (composition_id, note_id)
);

-- =============================================================================
-- Таблиця: composition_history
-- Нормальна форма: 3НФ (враховуючи використання JSONB для snapshot)
-- Тип: Weak Entity. Архів версій для конкретної композиції
-- =============================================================================
CREATE TABLE composition_history (
    id BIGSERIAL PRIMARY KEY,
    composition_id BIGINT REFERENCES compositions(id) ON DELETE CASCADE,
    snapshot_data JSONB,
    version_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);