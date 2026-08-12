package likelion.mcmshowcase.visit.dto;

import likelion.mcmshowcase.visit.entity.CustomerSessionStatus;

import java.time.LocalDateTime;

public record CustomerSessionMemberMatchResponse(
        Long customerSessionId,
        Long memberId,
        LocalDateTime identifiedAt,
        CustomerSessionStatus status
) {
}
