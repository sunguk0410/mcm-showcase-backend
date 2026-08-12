package likelion.mcmshowcase.ar.dto;

import java.time.LocalDateTime;

public record ArSessionEndResponse(
        Long arSessionId,
        Long customerSessionId,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
