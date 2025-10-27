package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.CreateMatchRequest;
import de.technikerarbeit.backend.game.dto.GameView;
import de.technikerarbeit.backend.game.dto.JoinMatchRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class GameController {

  private final GameService service;
  private final GameRepository games;

  public GameController(GameService service, GameRepository games) {
    this.service = service;
    this.games = games;
  }

  /**
   * Partie erstellen.
   * Body: { "timeControl": "5+0", "side": "white|black|random", "rated": false }
   * Antwort: GameView
   */
  @PostMapping("/match/create")
  public ResponseEntity<?> create(@RequestBody CreateMatchRequest req, Authentication auth) {
    try {
      Long userId = Long.parseLong(auth.getName()); // JwtAuthFilter setzt subject=userId
      var g = service.createMatch(userId, req);
      return ResponseEntity.ok(GameView.of(g));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }

  /**
   * Offene Partie beitreten.
   * Body: { "gameId": 123 }
   * Antwort: GameView
   */
  @PostMapping("/match/join")
  public ResponseEntity<?> join(@RequestBody JoinMatchRequest req, Authentication auth) {
    try {
      if (req == null || req.gameId == null) {
        return ResponseEntity.status(400).body("game_id_required");
      }
      Long userId = Long.parseLong(auth.getName());
      var g = service.joinMatch(req.gameId, userId);
      return ResponseEntity.ok(GameView.of(g));
    } catch (IllegalArgumentException e) {
      // z. B. game_not_found, user_not_found
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (IllegalStateException e) {
      // z. B. cannot_join_finished_game, game_full
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }

  /**
   * Einzelnes Spiel abrufen (kompakt).
   */
  @GetMapping("/games/{id}")
  public ResponseEntity<?> get(@PathVariable("id") Long id) {
    return games.findById(id)
        .<ResponseEntity<?>>map(g -> ResponseEntity.ok(GameView.of(g)))
        .orElseGet(() -> ResponseEntity.status(404).body("game_not_found"));
  }
}
