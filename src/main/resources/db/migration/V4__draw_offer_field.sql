-- Ermöglicht Remis-Angebote: speichere, WER das Remis angeboten hat
ALTER TABLE games
  ADD COLUMN draw_offer_by BIGINT NULL,
  ADD CONSTRAINT fk_games_draw_offer_by FOREIGN KEY (draw_offer_by) REFERENCES users(id);
