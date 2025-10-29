package de.technikerarbeit.backend.tactics;

import de.technikerarbeit.backend.tactics.dto.PuzzleDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tactics")
public class PuzzleController {

  private final PuzzleService service;

  public PuzzleController(PuzzleService service) {
    this.service = service;
  }

  @PostMapping("/puzzles")
  public ResponseEntity<PuzzleDto> create(@RequestBody PuzzleDto dto) {
    return ResponseEntity.ok(service.create(dto));
  }

  @GetMapping("/puzzles/{id}")
  public ResponseEntity<PuzzleDto> get(@PathVariable Long id) {
    return ResponseEntity.ok(service.get(id));
  }

  @GetMapping("/puzzles")
  public Page<PuzzleDto> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
    return service.list(page, size);
  }

  @GetMapping("/random")
  public ResponseEntity<PuzzleDto> random() {
    return ResponseEntity.ok(service.random());
  }
}
