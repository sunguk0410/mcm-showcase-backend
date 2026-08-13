package likelion.mcmshowcase.closet.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.product.entity.Product;

@Entity
@Table(name = "today_look_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayLookItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "today_look_id", nullable = false)
    private TodayLook todayLook;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public static TodayLookItem create(
            TodayLook todayLook,
            Product product,
            int displayOrder
    ) {
        TodayLookItem todayLookItem = new TodayLookItem();
        todayLookItem.todayLook = todayLook;
        todayLookItem.product = product;
        todayLookItem.displayOrder = displayOrder;
        return todayLookItem;
    }
}
