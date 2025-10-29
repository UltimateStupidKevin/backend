package de.technikerarbeit.backend.tactics;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puzzles")
public class Puzzle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 120, nullable = false)
  private String fen;

  @Column(name = "side_to_move", length = 1, nullable = false)
  private String sideToMove; // "w" | "b"

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "puzzle", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("seq ASC")
  private List<PuzzleMove> moves = new ArrayList<>();

  // getters/setters
  public Long getId() { return id; }
  public String getFen() { return fen; }
  public void setFen(String fen) { this.fen = fen; }
  public String getSideToMove() { return sideToMove; }
  public void setSideToMove(String sideToMove) { this.sideToMove = sideToMove; }
  public Instant getCreatedAt() { return createdAt; }
  public List<PuzzleMove> getMoves() { return moves; }

  public void setMoves(List<PuzzleMove> moves) {
    this.moves.clear();
    if (moves != null) moves.forEach(this::addMove);
  }
  public void addMove(PuzzleMove move) {
    move.setPuzzle(this);
    this.moves.add(move);
  }
}
