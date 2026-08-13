package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonRecommendationResponse(
        List<Long> recommendedProductIds
) {
}
