package de.technikerarbeit.backend.mastergames;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MasterGameRepository extends JpaRepository<MasterGame, Long>, JpaSpecificationExecutor<MasterGame> {
}
