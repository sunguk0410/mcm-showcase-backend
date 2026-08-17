package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonAvatarLookRequest(
        Long arSessionId,
        List<PythonRecommendationInteraction> interactions
) {
}
