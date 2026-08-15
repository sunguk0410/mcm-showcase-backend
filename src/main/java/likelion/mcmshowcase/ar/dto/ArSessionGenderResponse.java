package likelion.mcmshowcase.ar.dto;

import likelion.mcmshowcase.global.enums.Gender;

public record ArSessionGenderResponse(
        Long arSessionId,
        Gender gender
) {
}
