package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.GameDetailsView;
import de.technikerarbeit.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
public class GameQueryController {

  private final GameRepository games;
  private final MoveRepository moves;
  private final UserRepository users;
  private final GameService gameService;

  public GameQueryController(GameRepository games, MoveRepository moves, UserRepository users, GameService gameService) {
    this.games = games;
    this.moves = moves;
    this.users = users;
    this.gameService = gameService;
  }

  // --- GET /games/{id}/details ---
  @GetMapping("/{id}/details")
  public ResponseEntity<?> details(@PathVariable("id") Long gameId) {
    Game g = games.findById(gameId).orElse(null);
    if (g == null) return ResponseEntity.status(404).body("game_not_found");

    // Falls Zeit überschritten: Sieger setzen (WHITE_WIN/BLACK_WIN)
    gameService.applyTimeoutIfDue(g);

    var v = GameDetailsView.ofBasic(g);

    // Usernames (optional)
    if (v.whiteId != null) users.findById(v.whiteId).ifPresent(u -> v.whiteUsername = u.getUsername());
    if (v.blackId != null) users.findById(v.blackId).ifPresent(u -> v.blackUsername = u.getUsername());

    // Clock-Snapshot berechnen
    v.whiteMs = g.getWhiteMsLeft();
    v.blackMs = g.getBlackMsLeft();
    v.running = g.getStatus() == GameStatus.ONGOING;
    v.nextToMove = null;

    if (v.running && g.getLastMoveAt() != null) {
      int nextPly = moves.findTopByGameIdOrderByPlyDesc(gameId).map(m -> m.getPly() + 1).orElse(1);
      boolean whiteToMove = (nextPly % 2 == 1);
      long elapsed = Math.max(0, Duration.between(g.getLastMoveAt(), Instant.now()).toMillis());
      if (whiteToMove) {
        v.whiteMs = Math.max(0, v.whiteMs - elapsed);
        v.nextToMove = "WHITE";
      } else {
        v.blackMs = Math.max(0, v.blackMs - elapsed);
        v.nextToMove = "BLACK";
      }
    }

    v.status = g.getStatus();
    return ResponseEntity.ok(v);
  }

  // --- GET /games/my?status=ONGOING|ENDED|ALL&limit=50 ---
  @GetMapping("/my")
  public ResponseEntity<?> myGames(@RequestParam(name = "status", required = false, defaultValue = "ALL") String status,
                                   @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
                                   org.springframework.security.core.Authentication auth) {
    Long userId = Long.parseLong(auth.getName());
    List<Game> list = games.findByWhiteIdOrBlackIdOrderByCreatedAtDesc(userId, userId);

    status = status.toUpperCase(Locale.ROOT);
    final String finalStatus = status;
    list = list.stream()
        .filter(g -> switch (finalStatus) {
          case "ONGOING" -> g.getStatus() == GameStatus.ONGOING;
          case "ENDED" -> GameDetailsView.isEnded(g.getStatus());
          default -> true; // ALL
        })
        .limit(Math.max(1, Math.min(limit, 200)))
        .collect(Collectors.toList());

    var result = list.stream().map(GameDetailsView::ofBasic).collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }
}
