package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.CreateMatchRequest;
import de.technikerarbeit.backend.game.dto.GameDetailsView;
import de.technikerarbeit.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class GameService {

  private final GameRepository games;
  private final MoveRepository moves;
  private final UserRepository users;

  public GameService(GameRepository games, MoveRepository moves, UserRepository users) {
    this.games = games;
    this.moves = moves;
    this.users = users;
  }

  /** Nächster Zug aus der Ply-Zahl. */
  public String nextToMove(long gameId) {
    int nextPly = moves.findTopByGameIdOrderByPlyDesc(gameId)
        .map(m -> m.getPly() + 1)
        .orElse(1);
    return (nextPly % 2 == 1) ? "WHITE" : "BLACK";
  }

  /** Reine Berechnung der Restzeiten (ohne Save). */
  public RemainingTime snapshotRemaining(Game g) {
    long w = g.getWhiteMsLeft();
    long b = g.getBlackMsLeft();
    boolean running = (g.getStatus() == GameStatus.ONGOING);

    if (running && g.getLastMoveAt() != null) {
      String side = nextToMove(g.getId());
      long elapsed = Math.max(0, Duration.between(g.getLastMoveAt(), Instant.now()).toMillis());
      if ("WHITE".equals(side)) w = Math.max(0, w - elapsed);
      else b = Math.max(0, b - elapsed);
    }
    return new RemainingTime(w, b, running);
  }

  /**
   * Zeitüberschreitung anwenden, falls fällig.
   * TIMEOUT gibt es nicht mehr; stattdessen gewinnt der Gegner.
   * @return true wenn Status geändert wurde
   */
  @Transactional
  public boolean applyTimeoutIfDue(Game g) {
    if (g.getStatus() != GameStatus.ONGOING) return false;

    RemainingTime rt = snapshotRemaining(g);
    String side = nextToMove(g.getId());
    long cur = "WHITE".equals(side) ? rt.whiteMs : rt.blackMs;
    if (cur > 0) return false;

    // Spieler am Zug hat Zeit überschritten -> Gegner gewinnt.
    g.setStatus("WHITE".equals(side) ? GameStatus.BLACK_WIN : GameStatus.WHITE_WIN);
    g.setEndedAt(Instant.now());
    g.setLastMoveAt(Instant.now());
    games.save(g);
    return true;
  }

  // ---------------------------
  // Vom GameController verwendete Methoden
  // ---------------------------

  /** Partie erstellen. timeControl z.B. "5+0", side "white"|"black"|"random". */
  @Transactional
  public Game createMatch(Long creatorUserId, CreateMatchRequest req) {
    if (creatorUserId == null) throw new IllegalArgumentException("user_required");
    users.findById(creatorUserId).orElseThrow(() -> new IllegalArgumentException("user_not_found"));

    // Zeitkontrolle parsen – DTO hat Felder (keine Getter)
    String tcRaw = (req != null && req.timeControl != null && !req.timeControl.isBlank())
        ? req.timeControl.trim()
        : "5+0";

    int minutes = 5;
    int incSec = 0;
    try {
      String[] parts = tcRaw.split("\\+");
      minutes = Integer.parseInt(parts[0].trim());
      if (parts.length > 1) incSec = Integer.parseInt(parts[1].trim());
    } catch (Exception ignored) { /* fallback unten */ }

    int startMs = Math.max(0, minutes) * 60_000;
    int incrementMs = Math.max(0, incSec) * 1_000;

    String sideRaw = (req != null && req.side != null) ? req.side : "white";
    String side = sideRaw.toLowerCase(Locale.ROOT);

    Game g = new Game();
    g.setStatus(GameStatus.CREATED);
    g.setCreatedAt(Instant.now());
    g.setWhiteMsLeft(startMs);
    g.setBlackMsLeft(startMs);

    // Pflichtfelder
    g.setTimeControl(tcRaw);
    g.setIncrementMs(incrementMs);

    switch (side) {
      case "white" -> g.setWhiteId(creatorUserId);
      case "black" -> g.setBlackId(creatorUserId);
      default -> { // random
        if ((System.nanoTime() & 1L) == 0L) g.setWhiteId(creatorUserId);
        else g.setBlackId(creatorUserId);
      }
    }
    return games.save(g);
  }

  /** Offene Partie beitreten. */
  @Transactional
  public Game joinMatch(Long gameId, Long userId) {
    if (gameId == null || userId == null) throw new IllegalArgumentException("bad_request");
    Game g = games.findById(gameId).orElseThrow(() -> new IllegalArgumentException("game_not_found"));
    users.findById(userId).orElseThrow(() -> new IllegalArgumentException("user_not_found"));

    if (g.getStatus() != GameStatus.CREATED && g.getStatus() != GameStatus.ONGOING) {
      throw new IllegalStateException("cannot_join_finished_game");
    }
    if (userId.equals(g.getWhiteId()) || userId.equals(g.getBlackId())) {
      return g; // bereits Teilnehmer
    }

    if (g.getWhiteId() == null) g.setWhiteId(userId);
    else if (g.getBlackId() == null) g.setBlackId(userId);
    else throw new IllegalStateException("game_full");

    if (g.getWhiteId() != null && g.getBlackId() != null) {
      g.setStatus(GameStatus.ONGOING);
      g.setLastMoveAt(Instant.now());
    }
    return games.save(g);
  }

  // Optionaler Convenience-View
  public Optional<GameDetailsView> detailsWithTimeoutApplied(long gameId) {
    return games.findById(gameId).map(g -> {
      applyTimeoutIfDue(g);
      GameDetailsView v = GameDetailsView.ofBasic(g);
      RemainingTime rt = snapshotRemaining(g);
      v.whiteMs = rt.whiteMs;
      v.blackMs = rt.blackMs;
      v.running = (g.getStatus() == GameStatus.ONGOING);
      v.nextToMove = v.running ? nextToMove(gameId) : null;
      v.status = g.getStatus(); // GameStatus
      return v;
    });
  }

  public static class RemainingTime {
    public final long whiteMs;
    public final long blackMs;
    public final boolean running;
    public RemainingTime(long w, long b, boolean running) {
      this.whiteMs = w; this.blackMs = b; this.running = running;
    }
  }
}
