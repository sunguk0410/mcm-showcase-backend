package likelion.mcmshowcase.closet.dto;

import java.math.BigDecimal;

public record MyClosetProductResponse(
        Long productId,
        String name,
        String nameEn,
        BigDecimal price,
        String imageUrl,
        String productUrl,
        boolean isWishlisted
) {
}
