package likelion.mcmshowcase.member.repository;

import likelion.mcmshowcase.member.entity.MemberPurchase;
import likelion.mcmshowcase.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberPurchaseRepository extends JpaRepository<MemberPurchase, Long> {
    List<MemberPurchase> findByMemberOrderByPurchasedAtAsc(Member member);
}
