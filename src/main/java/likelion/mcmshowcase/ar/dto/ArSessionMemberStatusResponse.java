package likelion.mcmshowcase.ar.dto;

import likelion.mcmshowcase.global.enums.Gender;

public record ArSessionMemberStatusResponse(
        Long arSessionId,
        Long memberId,
        Gender gender
) {
}
