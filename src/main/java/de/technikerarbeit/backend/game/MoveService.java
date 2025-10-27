package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.SubmitMoveRequest;
import de.technikerarbeit.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoveService {

  private static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

  private final GameRepository games;
  private final MoveRepository moves;
  private final UserRepository users;

  public MoveService(GameRepository games, MoveRepository moves, UserRepository users) {
    this.games = games;
    this.moves = moves;
    this.users = users;
  }

  @Transactional
  public void submitMove(Long gameId, Long userId, SubmitMoveRequest req) {
    if (gameId == null || userId == null || req == null) {
      throw new IllegalArgumentException("bad_request");
    }

    Game g = games.findById(gameId)
        .orElseThrow(() -> new IllegalArgumentException("game_not_found"));
    users.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("user_not_found"));

    if (g.getStatus() != GameStatus.ONGOING) {
      throw new IllegalStateException("game_not_ongoing");
    }

    boolean isWhite = userId.equals(g.getWhiteId());
    boolean isBlack = userId.equals(g.getBlackId());
    if (!isWhite && !isBlack) {
      throw new IllegalStateException("not_a_player_of_this_game");
    }

    int nextPly = moves.findTopByGameIdOrderByPlyDesc(gameId)
        .map(m -> m.getPly() + 1)
        .orElse(1);
    boolean whiteToMove = (nextPly % 2 == 1);
    if ((whiteToMove && !isWhite) || (!whiteToMove && !isBlack)) {
      throw new IllegalStateException("wrong_turn");
    }

    long now = System.currentTimeMillis();
    if (g.getLastMoveAt() != null) {
      long elapsed = Math.max(0, now - g.getLastMoveAt().toEpochMilli());
      if (whiteToMove) {
        g.setWhiteMsLeft((int) Math.max(0, g.getWhiteMsLeft() - elapsed));
      } else {
        g.setBlackMsLeft((int) Math.max(0, g.getBlackMsLeft() - elapsed));
      }
    }

    // Timeout vor Ausführung?
    if (whiteToMove && g.getWhiteMsLeft() <= 0) {
      g.setStatus(GameStatus.BLACK_WIN);
      g.setEndedAt(Instant.now());
      games.save(g);
      return;
    }
    if (!whiteToMove && g.getBlackMsLeft() <= 0) {
      g.setStatus(GameStatus.WHITE_WIN);
      g.setEndedAt(Instant.now());
      games.save(g);
      return;
    }

    // Zug speichern
    Move m = new Move();
    m.setGameId(g.getId());
    m.setPly(nextPly);
    m.setSan(req.san);
    m.setUci(req.uci);
    m.setFenAfter(req.fenAfter);
    m.setPlayedMs(req.playedMs != null ? req.playedMs.intValue() : 0);
    moves.save(m);

    // Inkrement ermitteln – funktioniert für int **und** Integer (Auto-Boxing), ohne int-gegen-null Vergleich
    Integer incMs = g.getIncrementMs();  // wenn primitive int: wird zu Integer autogeboxt; nie null
    int inc = (incMs == null ? 0 : incMs);

    if (whiteToMove) g.setWhiteMsLeft(g.getWhiteMsLeft() + inc);
    else g.setBlackMsLeft(g.getBlackMsLeft() + inc);

    g.setLastMoveAt(Instant.ofEpochMilli(now));

    // Matt anhand SAN ('#')
    if (req.san != null && req.san.contains("#")) {
      g.setStatus(whiteToMove ? GameStatus.WHITE_WIN : GameStatus.BLACK_WIN);
      g.setEndedAt(Instant.now());
      games.save(g);
      return;
    }

    // === Dreifache Stellungswiederholung ===
    // Nur Stellung + Seite am Zug + Rochaderechte vergleichen (EP/Halbzug/Vollzug ignorieren)
    String finalFen = normalizeFen(req.fenAfter);

    List<String> positions = new ArrayList<>();
    String initFen = g.getInitialFen();
    if (initFen == null || initFen.isBlank()) initFen = START_FEN;
    positions.add(normalizeFen(initFen));

    List<Move> all = moves.findByGameIdOrderByPlyAsc(gameId);
    for (Move mv : all) {
      positions.add(normalizeFen(mv.getFenAfter()));
    }

    int same = 0;
    for (String pos : positions) {
      if (finalFen.equals(pos)) same++;
    }
    if (same >= 3) {
      g.setStatus(GameStatus.DRAW);
      g.setEndedAt(Instant.now());
      games.save(g);
      return;
    }

    games.save(g);
  }

  /**
   * Normalisiert eine FEN auf die Repetitions-relevanten Teile:
   *  - Feld 0: Stellung
   *  - Feld 1: Seite am Zug
   *  - Feld 2: Rochaderechte (leere Rechte → "-")
   *  EP sowie Halb-/Vollzugzähler werden ignoriert.
   */
  private static String normalizeFen(String fen) {
    if (fen == null || fen.isBlank()) return "";
    String[] p = fen.trim().split("\\s+");
    String pieces = p.length > 0 ? p[0] : "";
    String turn = p.length > 1 ? p[1] : "w";
    String castling = p.length > 2 ? p[2] : "-";
    if (castling == null || castling.isBlank()) castling = "-";
    return pieces + " " + turn + " " + castling;
  }
}
