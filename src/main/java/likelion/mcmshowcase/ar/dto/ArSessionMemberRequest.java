package likelion.mcmshowcase.ar.dto;

import jakarta.validation.constraints.NotNull;

public record ArSessionMemberRequest(
        @NotNull Long memberId
) {
}
