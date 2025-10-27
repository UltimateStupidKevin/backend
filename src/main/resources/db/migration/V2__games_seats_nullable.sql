-- Beide Seats nullable machen und evtl. Altwerte (0) bereinigen
ALTER TABLE games
  MODIFY white_id BIGINT NULL,
  MODIFY black_id BIGINT NULL;

UPDATE games SET white_id = NULL WHERE white_id = 0;
UPDATE games SET black_id = NULL WHERE black_id = 0;
