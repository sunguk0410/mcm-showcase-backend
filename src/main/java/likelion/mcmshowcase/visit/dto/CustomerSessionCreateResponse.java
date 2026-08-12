package likelion.mcmshowcase.visit.dto;

import likelion.mcmshowcase.visit.entity.CustomerSessionStatus;

import java.time.LocalDateTime;

public record CustomerSessionCreateResponse(
        Long customerSessionId,
        CustomerSessionStatus status,
        LocalDateTime startedAt
) {
}
