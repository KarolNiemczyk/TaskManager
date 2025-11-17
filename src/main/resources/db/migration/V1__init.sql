-- src/main/resources/db/migration/V1__init.sql

CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            color VARCHAR(7) DEFAULT '#3B82F6'
);

CREATE TABLE tasks (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(100) NOT NULL,
                       description TEXT,
                       status VARCHAR(20) NOT NULL DEFAULT 'TODO',
                       due_date DATE,
                       category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dane testowe
INSERT INTO categories (name, color) VALUES
                                         ('Praca', '#EF4444'),
                                         ('Osobiste', '#10B981'),
                                         ('Zakupy', '#F59E0B');