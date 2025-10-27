package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.MoveView;
import de.technikerarbeit.backend.game.dto.SubmitMoveRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
public class MoveController {

  private final MoveService moveService;
  private final MoveRepository moveRepository;
  private final GameRepository gameRepository;

  public MoveController(MoveService moveService,
                        MoveRepository moveRepository,
                        GameRepository gameRepository) {
    this.moveService = moveService;
    this.moveRepository = moveRepository;
    this.gameRepository = gameRepository;
  }

  /**
   * Züge lesen – read-only, für eingeloggte Nutzer erlaubt.
   * KEINE Spieler-Prüfung (damit Detailseite/Join robust funktionieren).
   */
  @GetMapping("/{id}/moves")
  public ResponseEntity<?> list(@PathVariable("id") Long gameId) {
    Game g = gameRepository.findById(gameId)
        .orElse(null);
    if (g == null) {
      return ResponseEntity.status(404).body("game_not_found");
    }

    List<MoveView> list = moveRepository
        .findByGameIdOrderByPlyAsc(gameId)
        .stream()
        .map(m -> {
          MoveView v = new MoveView();
          v.id = m.getId();
          v.ply = m.getPly();
          v.san = m.getSan();
          v.uci = m.getUci();
          v.fenAfter = m.getFenAfter();
          v.playedMs = m.getPlayedMs();
          return v;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(list);
  }

  /**
   * Zug abgeben – nur Teilnehmer, Validierung erfolgt im Service.
   */
  @PostMapping("/{id}/move")
  public ResponseEntity<?> submit(@PathVariable("id") Long gameId,
                                  Authentication auth,
                                  @RequestBody SubmitMoveRequest req) {
    try {
      Long userId = Long.parseLong(auth.getName());
      moveService.submitMove(gameId, userId, req);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      // game_not_found, user_not_found o.ä.
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (IllegalStateException e) {
      // wrong_turn, not_a_player_of_this_game, game_not_ongoing, illegal_move ...
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }
}
