package likelion.mcmshowcase.closet.repository;

import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StyleProfileRepository extends JpaRepository<StyleProfile, Long> {
    List<StyleProfile> findByArSessionMemberOrderByCreatedAtDesc(Member member);
}
