package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record AvatarLookResponse(
        Long arSessionId,
        Long styleProfileId,
        String styleIdentityTitle,
        TodayLook todayLook
) {
    public record TodayLook(List<Product> products) {
    }

    public record Product(
            Long productId,
            String productCode,
            String name,
            String imageUrl,
            String productUrl
    ) {
    }
}
