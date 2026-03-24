
INSERT INTO puzzles (fen, side_to_move)
VALUES ('6k1/5ppp/7Q/8/8/2B5/8/6K1 w - - 0 1', 'w');

SET @puzzle_id = LAST_INSERT_ID();

INSERT INTO puzzle_moves (puzzle_id, seq, color, from_sq, to_sq, promotion)
VALUES
  (@puzzle_id, 1, 'w', 'h6', 'g7', NULL);

INSERT INTO puzzles (fen, side_to_move)
VALUES ('4k3/8/8/1N1q4/8/8/8/6K1 w - - 0 1', 'w');

SET @puzzle_id = LAST_INSERT_ID();

INSERT INTO puzzle_moves (puzzle_id, seq, color, from_sq, to_sq, promotion)
VALUES
  (@puzzle_id, 1, 'w', 'b5', 'c7', NULL);
