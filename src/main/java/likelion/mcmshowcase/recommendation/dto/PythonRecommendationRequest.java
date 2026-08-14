package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonRecommendationRequest(
        List<PythonRecommendationInteraction> interactions,
        String category,
        int topK
) {
}
