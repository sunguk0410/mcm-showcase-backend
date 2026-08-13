package likelion.mcmshowcase.recommendation.dto;

import likelion.mcmshowcase.global.enums.Gender;

import java.util.List;

public record PythonRecommendationRequest(
        Long arSessionId,
        Gender gender,
        List<PythonRecommendationInteraction> interactions
) {
}
