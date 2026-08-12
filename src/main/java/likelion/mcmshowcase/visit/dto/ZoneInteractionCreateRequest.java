package likelion.mcmshowcase.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ZoneInteractionCreateRequest(
        @NotNull Long customerSessionId,
        @NotBlank String floorCode,
        @NotBlank String categoryCode,
        @NotNull LocalDateTime enteredAt,
        @NotNull LocalDateTime exitedAt
) {
}
