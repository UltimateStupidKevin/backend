-- Puzzles
CREATE TABLE IF NOT EXISTS puzzles (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  fen           VARCHAR(120) NOT NULL,
  side_to_move  ENUM('w','b') NOT NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Moves je Puzzle (max 4 Halbzüge; wird in der App validiert)
CREATE TABLE IF NOT EXISTS puzzle_moves (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  puzzle_id   BIGINT      NOT NULL,
  seq         INT         NOT NULL,             -- 1..4
  color       ENUM('w','b') NOT NULL,
  from_sq     CHAR(2)     NOT NULL,             -- z.B. 'e2'
  to_sq       CHAR(2)     NOT NULL,             -- z.B. 'e4'
  promotion   ENUM('q','r','b','n') NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_puzzle_moves_puzzle
    FOREIGN KEY (puzzle_id) REFERENCES puzzles(id)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT uq_puzzle_move_seq UNIQUE (puzzle_id, seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indizes
CREATE INDEX IF NOT EXISTS idx_puzzle_moves_puzzle ON puzzle_moves(puzzle_id);
