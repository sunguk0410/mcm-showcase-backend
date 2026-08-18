package likelion.mcmshowcase.recommendation.dto;

import likelion.mcmshowcase.global.enums.Gender;

import java.util.List;

public record PythonRecommendationRequest(
        Long arSessionId,
        List<PythonRecommendationInteraction> interactions,
        String category,
        int topK,
        Gender gender
) {
}
