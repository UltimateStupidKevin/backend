package de.technikerarbeit.backend.tactics.dto;

import java.util.List;

public class PuzzleDto {
  public Long id;
  public String fen;
  public String sideToMove;          // "w" | "b"
  public List<MoveDto> moves;        // max 4 Halbzüge

  public static class MoveDto {
    public int seq;                  // 1..4
    public String color;             // 'w' | 'b'
    public String from;              // "e2"
    public String to;                // "e4"
    public String promotion;         // optional "q","r","b","n"
  }
}
