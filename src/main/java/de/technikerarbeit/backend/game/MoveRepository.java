package de.technikerarbeit.backend.game;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<Move, Long> {
  List<Move> findByGameIdOrderByPlyAsc(Long gameId);
  Optional<Move> findTopByGameIdOrderByPlyDesc(Long gameId);
}
