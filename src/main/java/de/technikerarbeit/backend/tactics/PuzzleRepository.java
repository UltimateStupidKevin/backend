package de.technikerarbeit.backend.tactics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {
  Page<Puzzle> findAllByOrderByIdDesc(Pageable pageable);
}
