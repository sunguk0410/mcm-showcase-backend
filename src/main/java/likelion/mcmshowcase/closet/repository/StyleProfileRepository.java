package likelion.mcmshowcase.closet.repository;

import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StyleProfileRepository extends JpaRepository<StyleProfile, Long> {
    List<StyleProfile> findByArSessionMemberOrderByCreatedAtDesc(Member member);

    Optional<StyleProfile> findTopByArSessionOrderByCreatedAtDesc(ArSession arSession);

    @Query("""
            SELECT styleProfile
            FROM StyleProfile styleProfile
            WHERE styleProfile.avatarImageUrl IS NOT NULL
              AND TRIM(styleProfile.avatarImageUrl) <> ''
            ORDER BY styleProfile.createdAt DESC, styleProfile.id DESC
            """)
    List<StyleProfile> findLatestWithAvatarImage(Pageable pageable);
}
