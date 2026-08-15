package likelion.mcmshowcase.closet.dto;

import jakarta.validation.constraints.NotNull;

public record MyClosetMemberLinkRequest(
        @NotNull Long memberId
) {
}
