package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.OpenGameView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
public class GameBrowseController {

  private final GameRepository games;

  public GameBrowseController(GameRepository games) {
    this.games = games;
  }

  /**
   * Liste beitretbarer Spiele (Status CREATED, min. 1 freier Seat).
   * Query-Parameter:
   *  - tc:       optional, z.B. "5+0", "10+5"
   *  - excludeMine: true/false (default true) -> Spiele des aktuellen Users ausblenden
   *  - limit:    max Anzahl (default 50, max 200)
   */
  @GetMapping("/open")
  public ResponseEntity<?> openGames(
      @RequestParam(name = "tc", required = false) String tc,
      @RequestParam(name = "excludeMine", required = false, defaultValue = "true") String excludeMine,
      @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
      Authentication auth
  ) {
    Long currentUser = null;
    if (auth != null && auth.getName() != null) {
      try { currentUser = Long.parseLong(auth.getName()); } catch (NumberFormatException ignored) {}
    }

    boolean excl = !"false".equalsIgnoreCase(excludeMine);
    Long excludeCreator = excl ? currentUser : null;

    List<Game> list = games.findOpenGames(tc, excludeCreator);
    int cap = Math.max(1, Math.min(limit, 200));
    var result = list.stream()
        .limit(cap)
        .map(OpenGameView::of)
        .collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }
}
