package likelion.mcmshowcase.ar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.mcmshowcase.global.enums.Gender;

public record ArSessionMemberResponse(
        @Schema(example = "0") Long arSessionId,
        @Schema(example = "0") Long memberId,
        @Schema(example = "MALE") Gender gender
) {
}
