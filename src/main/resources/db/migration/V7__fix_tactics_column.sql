-- von CHAR(2) auf VARCHAR(2) ändern
ALTER TABLE puzzle_moves
  MODIFY from_sq VARCHAR(2) NOT NULL,
  MODIFY to_sq   VARCHAR(2) NOT NULL;
