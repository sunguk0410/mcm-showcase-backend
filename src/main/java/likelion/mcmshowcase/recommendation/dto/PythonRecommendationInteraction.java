package likelion.mcmshowcase.recommendation.dto;

import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.global.dto.PythonProductMetadata;

public record PythonRecommendationInteraction(
        Long productId,
        ArInteractionType interactionType,
        Integer sequenceNo,
        PythonProductMetadata product
) {
}
