package likelion.mcmshowcase.closet.dto;

public record MyClosetProductResponse(
        Long productId,
        String productCode,
        String name,
        String imageUrl,
        String productUrl
) {
}
