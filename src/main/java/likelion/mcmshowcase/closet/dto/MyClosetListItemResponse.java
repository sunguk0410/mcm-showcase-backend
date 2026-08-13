package likelion.mcmshowcase.closet.dto;

import java.time.LocalDateTime;

public record MyClosetListItemResponse(
        Long styleProfileId,
        String styleIdentityTitle,
        LocalDateTime createdAt,
        String avatarPreviewImageUrl
) {
}
