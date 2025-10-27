package de.technikerarbeit.backend.game.dto;

import de.technikerarbeit.backend.game.Move;

public class MoveView {
  public Long id;
  public Long gameId;
  public int ply;
  public String san;
  public String uci;
  public String fenAfter;
  public int playedMs;

  public static MoveView of(Move m) {
    MoveView v = new MoveView();
    v.id = m.getId();
    v.gameId = m.getGameId();
    v.ply = m.getPly();
    v.san = m.getSan();
    v.uci = m.getUci();
    v.fenAfter = m.getFenAfter();
    v.playedMs = m.getPlayedMs();
    return v;
  }
}
