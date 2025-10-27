package de.technikerarbeit.backend.game.dto;

import de.technikerarbeit.backend.game.Game;

import java.time.Instant;

public class OpenGameView {
  public Long id;
  public Long whiteId;     // kann null sein
  public Long blackId;     // kann null sein
  public String timeControl;
  public Instant createdAt;
  public String freeSeat;  // "WHITE" | "BLACK" | "EITHER"

  public static OpenGameView of(Game g) {
    OpenGameView v = new OpenGameView();
    v.id = g.getId();
    v.whiteId = g.getWhiteId();
    v.blackId = g.getBlackId();
    v.timeControl = g.getTimeControl();
    v.createdAt = g.getCreatedAt();
    if (g.getWhiteId() == null && g.getBlackId() == null) v.freeSeat = "EITHER";
    else if (g.getWhiteId() == null) v.freeSeat = "WHITE";
    else if (g.getBlackId() == null) v.freeSeat = "BLACK";
    else v.freeSeat = null;
    return v;
  }
}
