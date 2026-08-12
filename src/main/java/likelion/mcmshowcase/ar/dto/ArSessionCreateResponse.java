package likelion.mcmshowcase.ar.dto;

import likelion.mcmshowcase.global.enums.Gender;

import java.time.LocalDateTime;

public record ArSessionCreateResponse(
        Long arSessionId,
        Long customerSessionId,
        Gender gender,
        LocalDateTime startedAt
) {
}
