-- Users
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Email Verification Tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  PRIMARY KEY (token),
  CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Games
CREATE TABLE IF NOT EXISTS games (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  white_id BIGINT NOT NULL,
  black_id BIGINT NOT NULL,
  status ENUM('CREATED','ONGOING','WHITE_WIN','BLACK_WIN','DRAW','TIMEOUT','RESIGN') NOT NULL DEFAULT 'CREATED',
  initial_fen VARCHAR(128) NOT NULL DEFAULT 'startpos',
  rated BOOLEAN NOT NULL DEFAULT FALSE,
  time_control VARCHAR(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at TIMESTAMP NULL,
  CONSTRAINT fk_games_white FOREIGN KEY (white_id) REFERENCES users(id),
  CONSTRAINT fk_games_black FOREIGN KEY (black_id) REFERENCES users(id),
  INDEX idx_games_white (white_id),
  INDEX idx_games_black (black_id),
  INDEX idx_games_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Moves
CREATE TABLE IF NOT EXISTS moves (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  game_id BIGINT NOT NULL,
  ply INT NOT NULL,
  san VARCHAR(16) NOT NULL,
  uci VARCHAR(8) NOT NULL,
  fen_after VARCHAR(128) NOT NULL,
  played_ms INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_moves_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
  UNIQUE KEY uq_moves_game_ply (game_id, ply),
  INDEX idx_moves_game (game_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Master games (Lite)
CREATE TABLE IF NOT EXISTS master_games (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event VARCHAR(255) NULL,
  site VARCHAR(255) NULL,
  game_date DATE NULL,
  white VARCHAR(255) NULL,
  black VARCHAR(255) NULL,
  result CHAR(7) NULL,
  pgn LONGTEXT NOT NULL,
  INDEX idx_master_date (game_date),
  INDEX idx_master_white (white),
  INDEX idx_master_black (black)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tactics
CREATE TABLE IF NOT EXISTS tactics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_game_id BIGINT NULL,
  fen VARCHAR(128) NOT NULL,
  side_to_move CHAR(1) NOT NULL,
  solution_san VARCHAR(32) NOT NULL,
  best_line_pgn TEXT NULL,
  CONSTRAINT fk_tactics_source FOREIGN KEY (source_game_id) REFERENCES master_games(id) ON DELETE SET NULL,
  INDEX idx_tactics_side (side_to_move)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
