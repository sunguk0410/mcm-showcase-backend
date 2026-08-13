package likelion.mcmshowcase.closet.repository;

import likelion.mcmshowcase.closet.entity.TodayLook;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodayLookRepository extends JpaRepository<TodayLook, Long> {
    Optional<TodayLook> findTopByStyleProfileOrderByCreatedAtDesc(StyleProfile styleProfile);
}
