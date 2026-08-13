package likelion.mcmshowcase.closet.repository;

import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StyleProfileRepository extends JpaRepository<StyleProfile, Long> {
    List<StyleProfile> findByArSessionCustomerSessionMemberOrderByCreatedAtDesc(Member member);

    Optional<StyleProfile> findByIdAndArSessionCustomerSessionMember(Long id, Member member);
}
