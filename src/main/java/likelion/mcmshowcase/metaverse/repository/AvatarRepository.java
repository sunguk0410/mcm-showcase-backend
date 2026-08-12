package likelion.mcmshowcase.metaverse.repository;

import likelion.mcmshowcase.metaverse.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
