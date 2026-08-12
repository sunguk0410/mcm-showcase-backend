package likelion.mcmshowcase.ar.dto;

import jakarta.validation.constraints.NotNull;
import likelion.mcmshowcase.global.enums.Gender;

public record ArSessionCreateRequest(
        @NotNull Long customerSessionId,
        Gender gender
) {
}
