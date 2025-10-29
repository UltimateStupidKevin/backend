package de.technikerarbeit.backend.tactics;

import de.technikerarbeit.backend.tactics.dto.PuzzleDto;
import java.util.Comparator;

public final class PuzzleMapper {
  private PuzzleMapper(){}

  public static PuzzleDto toDto(Puzzle p) {
    var dto = new PuzzleDto();
    dto.id = p.getId();
    dto.fen = p.getFen();
    dto.sideToMove = p.getSideToMove();
    dto.moves = p.getMoves().stream()
        .sorted(Comparator.comparingInt(PuzzleMove::getSeq))
        .map(PuzzleMapper::toMoveDto)
        .toList();
    return dto;
  }

  private static PuzzleDto.MoveDto toMoveDto(PuzzleMove m) {
    var d = new PuzzleDto.MoveDto();
    d.seq = m.getSeq();
    d.color = m.getColor();
    d.from = m.getFromSq();
    d.to = m.getToSq();
    d.promotion = m.getPromotion();
    return d;
  }

  public static Puzzle fromDto(PuzzleDto dto) {
    var p = new Puzzle();
    p.setFen(dto.fen);
    p.setSideToMove(dto.sideToMove);
    if (dto.moves != null) {
      for (var m : dto.moves) {
        var pm = new PuzzleMove();
        pm.setSeq(m.seq);
        pm.setColor(m.color);
        pm.setFromSq(m.from);
        pm.setToSq(m.to);
        pm.setPromotion(m.promotion);
        p.addMove(pm);
      }
    }
    return p;
  }
}
