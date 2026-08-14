package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonRecommendationResponse(
        List<Recommendation> recommendations
) {
    public record Recommendation(Long productId, Double score) {
    }
}
