package likelion.mcmshowcase.ar.dto;

import java.util.List;

public record ProductSelectHistoryResponse(
        Long arSessionId,
        List<Product> products
) {
    public record Product(
            Long productId,
            String imageUrl
    ) {
    }
}
