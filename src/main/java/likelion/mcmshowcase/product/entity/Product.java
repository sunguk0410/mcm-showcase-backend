package likelion.mcmshowcase.product.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.global.entity.BaseEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id
    private Long id;

    @Column(name = "product_code", length = 100, nullable = false, unique = true)
    private String productCode;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "gender", length = 20, nullable = false)
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "color", length = 100)
    private String color;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(name = "zone", length = 30, nullable = false)
    private String zone;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "product_url", length = 1000)
    private String productUrl;

}
