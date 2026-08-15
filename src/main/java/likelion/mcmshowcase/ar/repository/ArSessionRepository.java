package likelion.mcmshowcase.ar.repository;

import likelion.mcmshowcase.ar.entity.ArSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ArSessionRepository extends JpaRepository<ArSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select arSession from ArSession arSession where arSession.id = :id")
    Optional<ArSession> findByIdForUpdate(@Param("id") Long id);
}
