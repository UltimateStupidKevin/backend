-- Uhren für beide Seiten + Inkrement + Zeitstempel des letzten Zug-Wechsels
ALTER TABLE games
  ADD COLUMN white_ms_left INT NOT NULL DEFAULT 0,
  ADD COLUMN black_ms_left INT NOT NULL DEFAULT 0,
  ADD COLUMN increment_ms INT NOT NULL DEFAULT 0,
  ADD COLUMN last_move_at TIMESTAMP NULL;