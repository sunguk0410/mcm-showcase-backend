package likelion.mcmshowcase.product.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_code", length = 100, nullable = false, unique = true)
    private String productCode;
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;
    @Column(name = "color", length = 100)
    private String color;
    @Column(name = "material", length = 100)
    private String material;
    @Column(name = "silhouette", length = 100)
    private String silhouette;
    @Column(name = "style", length = 100)
    private String style;
    @Column(name = "image_url", length = 1000)
    private String imageUrl;
    @Column(name = "product_url", length = 1000)
    private String productUrl;
    @Column(name = "ar_asset_url", length = 1000)
    private String arAssetUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
