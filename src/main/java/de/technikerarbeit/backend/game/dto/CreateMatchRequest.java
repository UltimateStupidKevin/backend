package de.technikerarbeit.backend.game.dto;
public class CreateMatchRequest {
  public String timeControl;   // z.B. "5+0", "10+0", "15+10"
  public String side;          // "white" | "black" | null (random)  -> MVP: wir setzen Creator auf white
  public boolean rated;        // im MVP ignorierbar/false
}
