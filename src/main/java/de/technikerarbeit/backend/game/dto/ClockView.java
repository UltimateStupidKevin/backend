package de.technikerarbeit.backend.game.dto;

public class ClockView {
  public long whiteMs;   // aktuelle Restzeit Weiß
  public long blackMs;   // aktuelle Restzeit Schwarz
  public boolean running; // ob die Uhr läuft (ONGOING)
  public String status;  // GameStatus als String

  public ClockView(long whiteMs, long blackMs, boolean running, String status) {
    this.whiteMs = whiteMs;
    this.blackMs = blackMs;
    this.running = running;
    this.status = status;
  }
}
