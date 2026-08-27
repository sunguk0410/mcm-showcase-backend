package likelion.mcmshowcase.member.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.global.entity.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_wishlist", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberWishlist extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    public static MemberWishlist create(Member member, Product product, LocalDateTime createdAt) {
        MemberWishlist memberWishlist = new MemberWishlist();
        memberWishlist.member = member;
        memberWishlist.product = product;
        memberWishlist.initializeAuditTimestamps(createdAt);
        return memberWishlist;
    }
}
