package de.technikerarbeit.backend.game.dto;

import java.time.Instant;
import java.util.Map;

public class GameEvent {
  public GameEventType type;
  public Long gameId;
  public long ts;
  public Map<String, Object> payload;

  public static GameEvent of(GameEventType type, Long gameId, Map<String, Object> payload) {
    GameEvent e = new GameEvent();
    e.type = type;
    e.gameId = gameId;
    e.ts = Instant.now().toEpochMilli();
    e.payload = payload;
    return e;
  }
}
