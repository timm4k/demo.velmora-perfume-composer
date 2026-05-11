-- імітація BCrypt для тестів
INSERT INTO users (nickname, email, password_hash, role, is_confirmed, invite_code) VALUES
('tate_admin', 'admin@velmora.dev', '$2a$12$LQv3fSsh.JhzZ6v.Gv99DeU.mK.Y.uN6E8C2N9G8E7D6C5B4A3A2', 'ADMIN', TRUE, 'VELMORA-2026-DEV'),
('tester_user', 'user@velmora.dev', '$2a$12$LQv3fSsh.JhzZ6v.Gv99DeU.mK.Y.uN6E8C2N9G8E7D6C5B4A3A1', 'USER', TRUE, NULL);