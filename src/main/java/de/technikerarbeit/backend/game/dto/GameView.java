package de.technikerarbeit.backend.game.dto;

import de.technikerarbeit.backend.game.Game;
import de.technikerarbeit.backend.game.GameStatus;

public class GameView {
  public Long id;
  public Long whiteId;
  public Long blackId;
  public GameStatus status;
  public String initialFen;
  public String timeControl;
  public boolean rated;

  public static GameView of(Game g) {
    var v = new GameView();
    v.id = g.getId();
    v.whiteId = g.getWhiteId();
    v.blackId = g.getBlackId();
    v.status = g.getStatus();
    v.initialFen = g.getInitialFen();
    v.timeControl = g.getTimeControl();
    v.rated = g.isRated();
    return v;
  }
}
