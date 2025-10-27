package de.technikerarbeit.backend.game;

import de.technikerarbeit.backend.game.dto.GameEvent;
import de.technikerarbeit.backend.game.dto.GameEventType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class GameActionService {

  private final GameRepository games;
  private final SimpMessagingTemplate messaging;

  public GameActionService(GameRepository games, SimpMessagingTemplate messaging) {
    this.games = games;
    this.messaging = messaging;
  }

  @Transactional
  public void resign(Long gameId, Long userId) {
    Game g = games.findById(gameId).orElseThrow(() -> new IllegalArgumentException("game_not_found"));
    if (g.getStatus() != GameStatus.ONGOING)
      throw new IllegalStateException("game_not_ongoing");
    if (!isPlayer(g, userId))
      throw new IllegalStateException("not_a_player_of_this_game");

    g.setStatus(GameStatus.RESIGN);
    g.setEndedAt(Instant.now());
    games.save(g);

    messaging.convertAndSend("/topic/game/" + gameId + "/events",
        GameEvent.of(GameEventType.GAME_ENDED, gameId, Map.of(
            "reason", "RESIGN",
            "resignedBy", String.valueOf(userId)
        )));
  }

  @Transactional
  public void offerDraw(Long gameId, Long userId) {
    Game g = games.findById(gameId).orElseThrow(() -> new IllegalArgumentException("game_not_found"));
    if (g.getStatus() != GameStatus.ONGOING)
      throw new IllegalStateException("game_not_ongoing");
    if (!isPlayer(g, userId))
      throw new IllegalStateException("not_a_player_of_this_game");
    if (g.getDrawOfferBy() != null)
      throw new IllegalStateException("draw_already_offered");

    g.setDrawOfferBy(userId);
    games.save(g);

    messaging.convertAndSend("/topic/game/" + gameId + "/events",
        GameEvent.of(GameEventType.DRAW_OFFERED, gameId, Map.of(
            "offeredBy", String.valueOf(userId)
        )));
  }

  @Transactional
  public void acceptDraw(Long gameId, Long userId) {
    Game g = games.findById(gameId).orElseThrow(() -> new IllegalArgumentException("game_not_found"));
    if (g.getStatus() != GameStatus.ONGOING)
      throw new IllegalStateException("game_not_ongoing");
    if (g.getDrawOfferBy() == null)
      throw new IllegalStateException("no_draw_offer");
    if (!isPlayer(g, userId))
      throw new IllegalStateException("not_a_player_of_this_game");
    // Nur der Gegner des Anbieters darf akzeptieren
    if (g.getDrawOfferBy().equals(userId))
      throw new IllegalStateException("not_offer_opponent");

    g.setStatus(GameStatus.DRAW);
    g.setEndedAt(Instant.now());
    g.setDrawOfferBy(null);
    games.save(g);

    messaging.convertAndSend("/topic/game/" + gameId + "/events",
        GameEvent.of(GameEventType.GAME_ENDED, gameId, Map.of(
            "reason", "AGREED_DRAW",
            "result", "1/2-1/2",
            "acceptedBy", String.valueOf(userId)
        )));
  }

  @Transactional
  public void declineDraw(Long gameId, Long userId) {
    Game g = games.findById(gameId).orElseThrow(() -> new IllegalArgumentException("game_not_found"));
    if (g.getStatus() != GameStatus.ONGOING)
      throw new IllegalStateException("game_not_ongoing");
    if (g.getDrawOfferBy() == null)
      throw new IllegalStateException("no_draw_offer");
    if (!isPlayer(g, userId))
      throw new IllegalStateException("not_a_player_of_this_game");
    // Nur der Gegner des Anbieters darf ablehnen
    if (g.getDrawOfferBy().equals(userId))
      throw new IllegalStateException("not_offer_opponent");

    g.setDrawOfferBy(null);
    games.save(g);

    messaging.convertAndSend("/topic/game/" + gameId + "/events",
        GameEvent.of(GameEventType.DRAW_DECLINED, gameId, Map.of(
            "declinedBy", String.valueOf(userId)
        )));
  }

  private boolean isPlayer(Game g, Long userId) {
    return (g.getWhiteId() != null && g.getWhiteId().equals(userId))
        || (g.getBlackId() != null && g.getBlackId().equals(userId));
  }
}
