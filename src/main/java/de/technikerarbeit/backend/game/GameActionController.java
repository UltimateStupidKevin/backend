package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.user.User;
import de.technikerarbeit.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameActionController {

  private final GameActionService service;
  private final UserRepository users;

  public GameActionController(GameActionService service, UserRepository users) {
    this.service = service;
    this.users = users;
  }

  @PostMapping("/{id}/resign")
  public ResponseEntity<?> resign(@PathVariable("id") Long gameId, Authentication auth) {
    try {
      Long userId = resolveUserId(auth);
      service.resign(gameId, userId);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }

  @PostMapping("/{id}/draw/offer")
  public ResponseEntity<?> offerDraw(@PathVariable("id") Long gameId, Authentication auth) {
    try {
      Long userId = resolveUserId(auth);
      service.offerDraw(gameId, userId);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }

  @PostMapping("/{id}/draw/accept")
  public ResponseEntity<?> acceptDraw(@PathVariable("id") Long gameId, Authentication auth) {
    try {
      Long userId = resolveUserId(auth);
      service.acceptDraw(gameId, userId);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }

  @PostMapping("/{id}/draw/decline")
  public ResponseEntity<?> declineDraw(@PathVariable("id") Long gameId, Authentication auth) {
    try {
      Long userId = resolveUserId(auth);
      service.declineDraw(gameId, userId);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    }
  }

  private Long resolveUserId(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new IllegalStateException("unauthenticated");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof User u) {
      if (u.getId() == null) throw new IllegalStateException("user_without_id");
      return u.getId();
    }
    String name = authentication.getName(); // kann "3", Email oder Username sein
    try {
      Long id = Long.parseLong(name);
      return users.findById(id).map(User::getId)
          .orElseThrow(() -> new IllegalStateException("user_not_found: " + name));
    } catch (NumberFormatException ignore) {
      // kein Long
    }
    return users.findByEmail(name)
        .or(() -> users.findByUsername(name))
        .map(User::getId)
        .orElseThrow(() -> new IllegalStateException("user_not_found: " + name));
  }
}
