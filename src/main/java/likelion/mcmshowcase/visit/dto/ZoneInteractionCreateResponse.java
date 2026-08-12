package likelion.mcmshowcase.visit.dto;

import java.time.LocalDateTime;

public record ZoneInteractionCreateResponse(
        Long zoneInteractionId,
        Long customerSessionId,
        Long zoneCategoryId,
        String floorCode,
        String categoryCode,
        LocalDateTime enteredAt,
        LocalDateTime exitedAt,
        Integer dwellSeconds
) {
}
