package de.technikerarbeit.backend.game.dto;

public class SubmitMoveRequest {
  public String san;       // z.B. "e4"
  public String uci;       // z.B. "e2e4"
  public String fenAfter;  // FEN nach dem Zug (Client liefert fürs MVP)
  public Integer playedMs; // Dauer des Zugs in Millisekunden (optional)

  public String getSan() { return san; }
  public void setSan(String san) { this.san = san; }
  public String getUci() { return uci; }
  public void setUci(String uci) { this.uci = uci; }
  public String getFenAfter() { return fenAfter; }
  public void setFenAfter(String fenAfter) { this.fenAfter = fenAfter; }
  public Integer getPlayedMs() { return playedMs; }
  public void setPlayedMs(Integer playedMs) { this.playedMs = playedMs; }
}
