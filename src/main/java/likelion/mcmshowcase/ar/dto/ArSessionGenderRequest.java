package likelion.mcmshowcase.ar.dto;

import jakarta.validation.constraints.NotNull;
import likelion.mcmshowcase.global.enums.Gender;

public record ArSessionGenderRequest(
        @NotNull Gender gender
) {
}
