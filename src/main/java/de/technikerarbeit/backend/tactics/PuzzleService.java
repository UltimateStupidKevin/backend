package de.technikerarbeit.backend.tactics;

import de.technikerarbeit.backend.tactics.dto.PuzzleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Random;

@Service
public class PuzzleService {

  private final PuzzleRepository repo;
  private final Random rnd = new Random();

  public PuzzleService(PuzzleRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public PuzzleDto create(PuzzleDto dto) {
    validate(dto);
    var saved = repo.save(PuzzleMapper.fromDto(dto));
    return PuzzleMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  public PuzzleDto get(Long id) {
    var p = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Puzzle not found"));
    return PuzzleMapper.toDto(p);
  }

  @Transactional(readOnly = true)
  public Page<PuzzleDto> list(int page, int size) {
    return repo.findAllByOrderByIdDesc(PageRequest.of(page, size)).map(PuzzleMapper::toDto);
  }

  @Transactional(readOnly = true)
  public PuzzleDto random() {
    long count = repo.count();
    if (count == 0) throw new NoSuchElementException("No puzzles available");
    int idx = rnd.nextInt((int)Math.min(count, Integer.MAX_VALUE));
    var page = repo.findAll(PageRequest.of(idx, 1));
    return page.getContent().isEmpty()
        ? repo.findAll(PageRequest.of(0, 1)).map(PuzzleMapper::toDto).getContent().get(0)
        : PuzzleMapper.toDto(page.getContent().get(0));
  }

  private void validate(PuzzleDto dto) {
    if (dto.fen == null || dto.fen.isBlank())
      throw new IllegalArgumentException("fen required");
    if (!"w".equals(dto.sideToMove) && !"b".equals(dto.sideToMove))
      throw new IllegalArgumentException("sideToMove must be 'w' or 'b'");
    if (dto.moves == null || dto.moves.isEmpty())
      throw new IllegalArgumentException("moves required (1..4 ply)");
    if (dto.moves.size() > 4)
      throw new IllegalArgumentException("max 4 half-moves (ply)");

    int seq = 1;
    for (var m : dto.moves) {
      if (m.seq != seq++) throw new IllegalArgumentException("moves.seq must be 1..n ascending");
      if (!"w".equals(m.color) && !"b".equals(m.color)) throw new IllegalArgumentException("move.color must be 'w' or 'b'");
      if (m.from == null || m.from.length()!=2 || m.to == null || m.to.length()!=2)
        throw new IllegalArgumentException("move from/to must be like e2,e4");
      if (m.promotion != null && !"qrbn".contains(m.promotion))
        throw new IllegalArgumentException("promotion must be q/r/b/n");
    }
    if (!dto.moves.isEmpty() && !dto.moves.get(0).color.equals(dto.sideToMove))
      throw new IllegalArgumentException("first move color must match sideToMove");
  }
}
