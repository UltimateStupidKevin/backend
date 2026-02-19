-- master games examples

INSERT INTO master_games (event, site, game_date, white, black, result, pgn)
SELECT
  'Paris (Legall''s Mate)',
  'Paris, France',
  '1750-01-01',
  'Legall, Kermur Sire de',
  'Saint Brie, NN',
  '1-0',
  CONCAT(
    '[Event "Paris (Legall''s Mate)"]', CHAR(10),
    '[Site "Paris, France"]', CHAR(10),
    '[Date "1750.01.01"]', CHAR(10),
    '[White "Legall, Kermur Sire de"]', CHAR(10),
    '[Black "Saint Brie, NN"]', CHAR(10),
    '[Result "1-0"]', CHAR(10),
    CHAR(10),
    '1. e4 e5 2. Nf3 Nc6 3. Bc4 d6 4. Nc3 Bg4 5. Nxe5 Bxd1 6. Bxf7+ Ke7 7. Nd5# 1-0'
  )
WHERE NOT EXISTS (
  SELECT 1 FROM master_games
  WHERE white = 'Legall, Kermur Sire de' AND black = 'Saint Brie, NN' AND game_date = '1750-01-01'
);

INSERT INTO master_games (event, site, game_date, white, black, result, pgn)
SELECT
  'Opera Game',
  'Paris, France',
  '1858-01-01',
  'Morphy, Paul',
  'Duke Karl / Count Isouard',
  '1-0',
  CONCAT(
    '[Event "Opera Game"]', CHAR(10),
    '[Site "Paris, France"]', CHAR(10),
    '[Date "1858.01.01"]', CHAR(10),
    '[White "Morphy, Paul"]', CHAR(10),
    '[Black "Duke Karl / Count Isouard"]', CHAR(10),
    '[Result "1-0"]', CHAR(10),
    CHAR(10),
    '1. e4 e5 2. Nf3 d6 3. d4 Bg4 4. dxe5 Bxf3 5. Qxf3 dxe5 6. Bc4 Nf6 7. Qb3 Qe7 ',
    '8. Nc3 c6 9. Bg5 b5 10. Nxb5 cxb5 11. Bxb5+ Nbd7 12. O-O-O Rd8 13. Rxd7 Rxd7 ',
    '14. Rd1 Qe6 15. Bxd7+ Nxd7 16. Qb8+ Nxb8 17. Rd8# 1-0'
  )
WHERE NOT EXISTS (
  SELECT 1 FROM master_games
  WHERE white = 'Morphy, Paul' AND black = 'Duke Karl / Count Isouard' AND game_date = '1858-01-01'
);

INSERT INTO master_games (event, site, game_date, white, black, result, pgn)
SELECT
  'Miniature (Quick Mate)',
  'Sample DB Seed',
  '1889-01-01',
  'Steinitz, Wilhelm',
  'von Bardeleben, Curt',
  '1-0',
  CONCAT(
    '[Event "Miniature (Quick Mate)"]', CHAR(10),
    '[Site "Sample DB Seed"]', CHAR(10),
    '[Date "1889.01.01"]', CHAR(10),
    '[White "Steinitz, Wilhelm"]', CHAR(10),
    '[Black "von Bardeleben, Curt"]', CHAR(10),
    '[Result "1-0"]', CHAR(10),
    CHAR(10),
    '1. e4 e5 2. Nf3 Nc6 3. Bc4 Nf6 4. Ng5 d5 5. exd5 Na5 6. Bb5+ c6 7. dxc6 bxc6 ',
    '8. Be2 h6 9. Nf3 e4 10. Ne5 Bd6 11. d4 exd3 12. Nxd3 O-O 13. O-O Qc7 14. h3 Re8 ',
    '15. Re1 Bf5 16. Nc3 Rad8 17. Be3 Nc4 18. Bc1 Bh2+ 19. Kh1 Bd6 20. b3 Be5 21. Bb2 Nxb2 22. Nxb2 Bxc3 23. Rxe8+ Rxe8 24. Nd3 Bxa1 25. Qxa1 Bxd3 26. Bxd3 Qf4 27. Kg1 Qd2 28. Qf1 Re1 29. 1-0'
  )
WHERE NOT EXISTS (
  SELECT 1 FROM master_games
  WHERE white = 'Steinitz, Wilhelm' AND black = 'von Bardeleben, Curt' AND game_date = '1889-01-01'
);

-- You can add more seeds later (ECO, Elo, Round, etc.) once we extend the schema.
