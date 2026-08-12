package likelion.mcmshowcase.member.repository;

import likelion.mcmshowcase.member.entity.MemberPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPurchaseRepository extends JpaRepository<MemberPurchase, Long> {
}
