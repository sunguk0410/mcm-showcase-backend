package likelion.mcmshowcase.recommendation.dto;

import likelion.mcmshowcase.ar.entity.ArInteractionType;

public record PythonRecommendationInteraction(
        Long productId,
        ArInteractionType interactionType,
        Integer sequenceNo
) {
}
