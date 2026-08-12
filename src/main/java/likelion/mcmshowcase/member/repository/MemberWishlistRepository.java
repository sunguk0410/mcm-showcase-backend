package likelion.mcmshowcase.member.repository;

import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.entity.MemberWishlist;
import likelion.mcmshowcase.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberWishlistRepository extends JpaRepository<MemberWishlist, Long> {
    boolean existsByMemberAndProduct(Member member, Product product);

    Optional<MemberWishlist> findByMemberAndProduct(Member member, Product product);
}
