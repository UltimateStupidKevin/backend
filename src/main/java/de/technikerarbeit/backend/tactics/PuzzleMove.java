package de.technikerarbeit.backend.tactics;

import jakarta.persistence.*;

@Entity
@Table(name = "puzzle_moves",
       uniqueConstraints = @UniqueConstraint(columnNames = {"puzzle_id","seq"}))
public class PuzzleMove {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "puzzle_id", nullable = false)
  private Puzzle puzzle;

  @Column(nullable = false)
  private int seq;                 // 1..4 (Halbzug-Reihenfolge)

  @Column(length = 1, nullable = false)
  private String color;            // 'w' | 'b'

  @Column(name = "from_sq", length = 2, nullable = false)
  private String fromSq;           // z. B. "e2"

  @Column(name = "to_sq", length = 2, nullable = false)
  private String toSq;             // z. B. "e4"

  @Column(length = 1)
  private String promotion;        // optional: q/r/b/n

  // getters/setters
  public Long getId() { return id; }
  public Puzzle getPuzzle() { return puzzle; }
  public void setPuzzle(Puzzle puzzle) { this.puzzle = puzzle; }
  public int getSeq() { return seq; }
  public void setSeq(int seq) { this.seq = seq; }
  public String getColor() { return color; }
  public void setColor(String color) { this.color = color; }
  public String getFromSq() { return fromSq; }
  public void setFromSq(String fromSq) { this.fromSq = fromSq; }
  public String getToSq() { return toSq; }
  public void setToSq(String toSq) { this.toSq = toSq; }
  public String getPromotion() { return promotion; }
  public void setPromotion(String promotion) { this.promotion = promotion; }
}
