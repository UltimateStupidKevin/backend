package de.technikerarbeit.backend.game.dto;

import de.technikerarbeit.backend.game.Game;
import de.technikerarbeit.backend.game.GameStatus;

import java.time.Instant;

public class GameDetailsView {
  public Long id;
  public Long whiteId;
  public Long blackId;
  public String whiteUsername;
  public String blackUsername;
  public GameStatus status;
  public String nextToMove; // "WHITE" | "BLACK" | null
  public long whiteMs;
  public long blackMs;
  public boolean running;
  public String timeControl;
  public Integer incrementMs;
  public Instant createdAt;
  public Instant lastMoveAt;
  public Instant endedAt;
  public Long drawOfferBy;

  public static GameDetailsView ofBasic(Game g) {
    GameDetailsView v = new GameDetailsView();
    v.id = g.getId();
    v.whiteId = g.getWhiteId();
    v.blackId = g.getBlackId();
    v.status = g.getStatus();
    v.timeControl = g.getTimeControl();
    v.incrementMs = g.getIncrementMs();
    v.createdAt = g.getCreatedAt();
    v.lastMoveAt = g.getLastMoveAt();
    v.endedAt = g.getEndedAt();
    v.whiteMs = g.getWhiteMsLeft();
    v.blackMs = g.getBlackMsLeft();
    v.running = (g.getStatus() == GameStatus.ONGOING);
    v.drawOfferBy = g.getDrawOfferBy();
    return v;
  }

  /** Hilfsfunktion für Filter (beendete Spiele). */
  public static boolean isEnded(GameStatus s) {
    if (s == null) return false;
    return switch (s) {
      case WHITE_WIN, BLACK_WIN, DRAW, RESIGN -> true;
      case CREATED, ONGOING -> false;
    };
  }
}
