-- Sample data for demonstration
-- This will be loaded on application startup

-- Sample Players
INSERT INTO players (username, email, display_name, avatar_url, status, created_at, updated_at)
VALUES
    ('speedster', 'speedster@game.com', 'Speed King', 'https://api.dicebear.com/7.x/avataaars/svg?seed=speedster', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('puzzler', 'puzzler@game.com', 'Puzzle Master', 'https://api.dicebear.com/7.x/avataaars/svg?seed=puzzler', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sniper', 'sniper@game.com', 'Sharp Shooter', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sniper', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('strategist', 'strategist@game.com', 'Strategy Pro', 'https://api.dicebear.com/7.x/avataaars/svg?seed=strategist', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('adventurer', 'adventurer@game.com', 'Epic Explorer', 'https://api.dicebear.com/7.x/avataaars/svg?seed=adventurer', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Scores for RACING game
INSERT INTO scores (player_id, score_value, game_type, session_duration_seconds, level_reached, metadata, created_at)
VALUES
    (1, 15000, 'RACING', 300, 50, '{"vehicle":"Formula One","track":"Monaco"}', CURRENT_TIMESTAMP),
    (2, 12000, 'RACING', 250, 45, '{"vehicle":"Rally Car","track":"Safari"}', CURRENT_TIMESTAMP),
    (3, 10000, 'RACING', 280, 42, '{"vehicle":"Sports Car","track":"Tokyo"}', CURRENT_TIMESTAMP),
    (4, 8000, 'RACING', 220, 38, '{"vehicle":"Classic Car","track":"Route 66"}', CURRENT_TIMESTAMP),
    (5, 6000, 'RACING', 200, 35, '{"vehicle":"Go Kart","track":"City Circuit"}', CURRENT_TIMESTAMP);

-- Sample Scores for PUZZLE game
INSERT INTO scores (player_id, score_value, game_type, session_duration_seconds, level_reached, metadata, created_at)
VALUES
    (2, 25000, 'PUZZLE', 450, 80, '{"difficulty":"hard","hints_used":2}', CURRENT_TIMESTAMP),
    (1, 20000, 'PUZZLE', 420, 75, '{"difficulty":"hard","hints_used":3}', CURRENT_TIMESTAMP),
    (4, 18000, 'PUZZLE', 390, 70, '{"difficulty":"medium","hints_used":1}', CURRENT_TIMESTAMP),
    (3, 15000, 'PUZZLE', 360, 65, '{"difficulty":"medium","hints_used":4}', CURRENT_TIMESTAMP),
    (5, 12000, 'PUZZLE', 300, 60, '{"difficulty":"easy","hints_used":5}', CURRENT_TIMESTAMP);

-- Sample Scores for SHOOTER game
INSERT INTO scores (player_id, score_value, game_type, session_duration_seconds, level_reached, metadata, created_at)
VALUES
    (3, 30000, 'SHOOTER', 600, 100, '{"accuracy":92,"headshots":45}', CURRENT_TIMESTAMP),
    (5, 28000, 'SHOOTER', 580, 95, '{"accuracy":88,"headshots":42}', CURRENT_TIMESTAMP),
    (1, 25000, 'SHOOTER', 550, 90, '{"accuracy":85,"headshots":38}', CURRENT_TIMESTAMP),
    (2, 22000, 'SHOOTER', 520, 85, '{"accuracy":82,"headshots":35}', CURRENT_TIMESTAMP),
    (4, 20000, 'SHOOTER', 500, 80, '{"accuracy":78,"headshots":30}', CURRENT_TIMESTAMP);
