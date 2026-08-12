package likelion.mcmshowcase.ar.dto;

import jakarta.validation.constraints.NotNull;
import likelion.mcmshowcase.ar.entity.ArInteractionType;

public record ArInteractionCreateRequest(
        @NotNull Long arSessionId,
        @NotNull Long productId,
        @NotNull ArInteractionType interactionType
) {
}
