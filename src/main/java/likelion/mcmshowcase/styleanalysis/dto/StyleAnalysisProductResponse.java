package likelion.mcmshowcase.styleanalysis.dto;

public record StyleAnalysisProductResponse(
        Long productId,
        String productCode,
        String name,
        String imageUrl,
        String productUrl
) {
}
