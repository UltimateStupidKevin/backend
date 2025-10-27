package de.technikerarbeit.backend.game;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "games")
public class Game {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "white_id")
  private Long whiteId;

  @Column(name = "black_id")
  private Long blackId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GameStatus status = GameStatus.CREATED;

  @Column(name = "initial_fen", nullable = false, length = 128)
  private String initialFen = "startpos";

  @Column(name = "rated", nullable = false)
  private boolean rated = false;

  @Column(name = "time_control", nullable = false, length = 16)
  private String timeControl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "ended_at")
  private Instant endedAt;

  // --- Clock fields ---
  @Column(name = "white_ms_left", nullable = false)
  private int whiteMsLeft = 0;

  @Column(name = "black_ms_left", nullable = false)
  private int blackMsLeft = 0;

  @Column(name = "increment_ms", nullable = false)
  private int incrementMs = 0;

  @Column(name = "last_move_at")
  private Instant lastMoveAt;

  // --- Draw offer ---
  @Column(name = "draw_offer_by")
  private Long drawOfferBy;

  // getters & setters

  public Long getId() { return id; }

  public Long getWhiteId() { return whiteId; }
  public void setWhiteId(Long whiteId) { this.whiteId = whiteId; }

  public Long getBlackId() { return blackId; }
  public void setBlackId(Long blackId) { this.blackId = blackId; }

  public GameStatus getStatus() { return status; }
  public void setStatus(GameStatus status) { this.status = status; }

  public String getInitialFen() { return initialFen; }
  public void setInitialFen(String initialFen) { this.initialFen = initialFen; }

  public boolean isRated() { return rated; }
  public void setRated(boolean rated) { this.rated = rated; }

  public String getTimeControl() { return timeControl; }
  public void setTimeControl(String timeControl) { this.timeControl = timeControl; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getEndedAt() { return endedAt; }
  public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

  public int getWhiteMsLeft() { return whiteMsLeft; }
  public void setWhiteMsLeft(int whiteMsLeft) { this.whiteMsLeft = whiteMsLeft; }

  public int getBlackMsLeft() { return blackMsLeft; }
  public void setBlackMsLeft(int blackMsLeft) { this.blackMsLeft = blackMsLeft; }

  public int getIncrementMs() { return incrementMs; }
  public void setIncrementMs(int incrementMs) { this.incrementMs = incrementMs; }

  public Instant getLastMoveAt() { return lastMoveAt; }
  public void setLastMoveAt(Instant lastMoveAt) { this.lastMoveAt = lastMoveAt; }

  public Long getDrawOfferBy() { return drawOfferBy; }
  public void setDrawOfferBy(Long drawOfferBy) { this.drawOfferBy = drawOfferBy; }
}
