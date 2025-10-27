package de.technikerarbeit.backend.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

  List<Game> findByWhiteIdOrBlackIdOrderByCreatedAtDesc(Long whiteId, Long blackId);

  @Query("""
    select g from Game g
    where g.status = de.technikerarbeit.backend.game.GameStatus.CREATED
      and (g.whiteId is null or g.blackId is null)
      and (:tc is null or g.timeControl = :tc)
      and (:excludeCreator is null
           or ( (g.whiteId is null or g.whiteId <> :excludeCreator)
             and (g.blackId is null or g.blackId <> :excludeCreator) ))
    order by g.createdAt desc
  """)
  List<Game> findOpenGames(@Param("tc") String timeControl,
                           @Param("excludeCreator") Long excludeCreator);
}
