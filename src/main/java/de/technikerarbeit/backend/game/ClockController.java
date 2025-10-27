package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.ClockView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/games")
public class ClockController {

  private final GameRepository games;
  private final MoveRepository moves;
  private final GameService gameService;

  public ClockController(GameRepository games, MoveRepository moves, GameService gameService) {
    this.games = games;
    this.moves = moves;
    this.gameService = gameService;
  }

  @GetMapping("/{id}/clock")
  public ResponseEntity<?> getClock(@PathVariable("id") Long gameId) {
    Game g = games.findById(gameId).orElse(null);
    if (g == null) return ResponseEntity.status(404).body("game_not_found");

    // harte Timeout-Prüfung (setzt WHITE_WIN/BLACK_WIN falls nötig)
    gameService.applyTimeoutIfDue(g);

    long whiteMs = g.getWhiteMsLeft();
    long blackMs = g.getBlackMsLeft();
    boolean running = (g.getStatus() == GameStatus.ONGOING);

    if (running && g.getLastMoveAt() != null) {
      int nextPly = moves.findTopByGameIdOrderByPlyDesc(gameId)
          .map(m -> m.getPly() + 1)
          .orElse(1);
      boolean whiteToMove = (nextPly % 2 == 1);
      long elapsed = Math.max(0, Duration.between(g.getLastMoveAt(), Instant.now()).toMillis());
      if (whiteToMove) whiteMs = Math.max(0, whiteMs - elapsed);
      else blackMs = Math.max(0, blackMs - elapsed);
    }

    // ClockView hat bei dir keinen No-Args-Konstruktor -> nutze den mit allen Feldern
    ClockView v = new ClockView(
        whiteMs,
        blackMs,
        running,
        g.getStatus() != null ? g.getStatus().name() : "CREATED"
    );

    return ResponseEntity.ok(v);
  }
}
