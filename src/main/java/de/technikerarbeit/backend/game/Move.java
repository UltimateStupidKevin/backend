package de.technikerarbeit.backend.game;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moves",
  uniqueConstraints = @UniqueConstraint(name="uq_moves_game_ply", columnNames={"game_id","ply"})
)
public class Move {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="game_id", nullable=false)
  private Long gameId;

  @Column(nullable=false)
  private int ply;

  @Column(nullable=false, length=16)
  private String san;

  @Column(nullable=false, length=8)
  private String uci;

  @Column(name="fen_after", nullable=false, length=128)
  private String fenAfter;

  @Column(name="played_ms", nullable=false)
  private int playedMs = 0;

  @Column(name="created_at", nullable=false)
  private Instant createdAt = Instant.now();

  // getters/setters
  public Long getId() { return id; }
  public Long getGameId() { return gameId; }
  public void setGameId(Long gameId) { this.gameId = gameId; }
  public int getPly() { return ply; }
  public void setPly(int ply) { this.ply = ply; }
  public String getSan() { return san; }
  public void setSan(String san) { this.san = san; }
  public String getUci() { return uci; }
  public void setUci(String uci) { this.uci = uci; }
  public String getFenAfter() { return fenAfter; }
  public void setFenAfter(String fenAfter) { this.fenAfter = fenAfter; }
  public int getPlayedMs() { return playedMs; }
  public void setPlayedMs(int playedMs) { this.playedMs = playedMs; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
