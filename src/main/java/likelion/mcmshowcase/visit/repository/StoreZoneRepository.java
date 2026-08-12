package likelion.mcmshowcase.visit.repository;

import likelion.mcmshowcase.visit.entity.StoreZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreZoneRepository extends JpaRepository<StoreZone, Long> {
    Optional<StoreZone> findByFloorCode(String floorCode);
}
