-- Schéma de référence pour le système de badwords
-- Le démarrage JavaFX crée automatiquement ces tables si elles n'existent pas
CREATE TABLE IF NOT EXISTS badword (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(255) NOT NULL UNIQUE,
    action ENUM('MASK', 'BLOCK', 'ALERT') NOT NULL DEFAULT 'MASK',
    active BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Journal des tentatives de violation
CREATE TABLE IF NOT EXISTS badword_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    violated_word VARCHAR(255) NOT NULL,
    action ENUM('MASK', 'BLOCK', 'ALERT') NOT NULL,
    content LONGTEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Badwords d'exemple insérés automatiquement au démarrage
INSERT IGNORE INTO badword (word, action, active) VALUES 
('spam', 'BLOCK', 1),
('hack', 'BLOCK', 1),
('fraud', 'ALERT', 1),
('hate', 'ALERT', 1),
('violence', 'ALERT', 1),
('xxx', 'BLOCK', 1),
('malware', 'BLOCK', 1),
('phishing', 'BLOCK', 1)
;

CREATE INDEX IF NOT EXISTS idx_badword_word ON badword(word);
CREATE INDEX IF NOT EXISTS idx_badword_active ON badword(active);
CREATE INDEX IF NOT EXISTS idx_badword_log_user ON badword_log(user_id);
CREATE INDEX IF NOT EXISTS idx_badword_log_timestamp ON badword_log(timestamp);
