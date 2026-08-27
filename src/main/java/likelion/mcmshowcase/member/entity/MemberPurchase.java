package likelion.mcmshowcase.member.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.global.entity.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_purchase")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPurchase extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;
}
