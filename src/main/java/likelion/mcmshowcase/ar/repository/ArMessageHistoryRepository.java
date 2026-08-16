package likelion.mcmshowcase.ar.repository;

import likelion.mcmshowcase.ar.entity.ArMessageHistory;
import likelion.mcmshowcase.ar.entity.ArSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArMessageHistoryRepository extends JpaRepository<ArMessageHistory, Long> {
    List<ArMessageHistory> findByArSessionOrderByFittingSequenceNoAscIdAsc(ArSession arSession);
}
