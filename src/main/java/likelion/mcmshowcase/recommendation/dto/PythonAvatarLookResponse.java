package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonAvatarLookResponse(
        Long arSessionId,
        String styleIdentityTitle,
        List<Product> products
) {
    public record Product(Long productId) {
    }
}
