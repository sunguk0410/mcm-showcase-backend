package likelion.mcmshowcase.member.dto;

import java.math.BigDecimal;

public record MemberWishlistItemResponse(
        Long productId,
        String name,
        String nameEn,
        BigDecimal price,
        String imageUrl,
        String productUrl
) {
}
