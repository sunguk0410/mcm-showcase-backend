package likelion.mcmshowcase.ar.dto;

import likelion.mcmshowcase.ar.entity.ArInteractionType;

import java.time.LocalDateTime;

public record ArInteractionCreateResponse(
        Long arInteractionId,
        Long arSessionId,
        Long productId,
        ArInteractionType interactionType,
        Integer sequenceNo,
        LocalDateTime createdAt
) {
}
