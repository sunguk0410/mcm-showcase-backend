package likelion.mcmshowcase.member.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.product.entity.Product;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_wishlist", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberWishlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static MemberWishlist create(Member member, Product product, LocalDateTime createdAt) {
        MemberWishlist memberWishlist = new MemberWishlist();
        memberWishlist.member = member;
        memberWishlist.product = product;
        memberWishlist.createdAt = createdAt;
        return memberWishlist;
    }
}
