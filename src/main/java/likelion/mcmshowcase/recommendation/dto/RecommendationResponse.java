package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        Long arSessionId,
        List<RecommendedProductResponse> products
) {
}
