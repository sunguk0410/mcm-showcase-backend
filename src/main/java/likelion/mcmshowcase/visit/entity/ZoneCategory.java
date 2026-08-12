package likelion.mcmshowcase.visit.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.product.entity.Category;

@Entity
@Table(name = "zone_category", uniqueConstraints = @UniqueConstraint(columnNames = {"zone_id", "category_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZoneCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private StoreZone zone;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
