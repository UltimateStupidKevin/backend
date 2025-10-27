-- V5: indexes_for_open_games (safe & idempotent)

-- Zusatzindex für Status + CreatedAt (Open-Games-Query + Sortierung)
CREATE INDEX IF NOT EXISTS idx_games_status_createdat
  ON games (status, created_at);

-- Zusatzindex für Time Control (Filter)
CREATE INDEX IF NOT EXISTS idx_games_timecontrol
  ON games (time_control);

-- WICHTIG: Keine Änderungen an Indizes für white_id / black_id!
-- Diese Indizes werden durch die Foreign Keys bereitgestellt und dürfen nicht gedroppt werden.
