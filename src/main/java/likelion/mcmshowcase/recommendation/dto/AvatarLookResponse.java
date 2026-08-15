package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record AvatarLookResponse(
        Long arSessionId,
        Long styleProfileId,
        String styleIdentityTitle,
        List<Product> products
) {
    public record Product(
            Long productId
    ) {
    }
}
