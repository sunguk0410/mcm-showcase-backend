package likelion.mcmshowcase.metaverse.repository;

import likelion.mcmshowcase.metaverse.entity.Avatar;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    Optional<Avatar> findTopByStyleProfileOrderByCreatedAtDesc(StyleProfile styleProfile);
}
