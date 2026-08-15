package likelion.mcmshowcase.closet.dto;

import java.util.List;

public record MyClosetDetailResponse(
        Long styleProfileId,
        String styleIdentityTitle,
        String avatarImageUrl,
        MyClosetTodayLookResponse todayLook,
        List<MyClosetProductResponse> fittingHistory
) {
}
