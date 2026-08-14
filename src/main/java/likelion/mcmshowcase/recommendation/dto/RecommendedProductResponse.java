package likelion.mcmshowcase.recommendation.dto;

import java.math.BigDecimal;

public record RecommendedProductResponse(
        Long productId,
        String productCode,
        String name,
        String category,
        BigDecimal price,
        String imageUrl,
        String productUrl,
        Double score
) {
}
