package likelion.mcmshowcase.product.dto;

import java.math.BigDecimal;

public record ProductDetailResponse(
        Long productId,
        String name,
        String nameEn,
        BigDecimal price,
        String color,
        String imageUrl
) {
}
