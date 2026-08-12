package likelion.mcmshowcase.visit.dto;

import jakarta.validation.constraints.NotNull;

public record CustomerSessionMemberMatchRequest(
        @NotNull Long memberId
) {
}
