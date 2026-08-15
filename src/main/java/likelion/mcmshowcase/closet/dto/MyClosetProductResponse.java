package likelion.mcmshowcase.closet.dto;

import java.math.BigDecimal;

public record MyClosetProductResponse(
        Long productId,
        String name,
        BigDecimal price,
        String imageUrl,
        String productUrl
) {
}
