package likelion.mcmshowcase.global.dto;

import java.util.List;

public record PythonMemberContext(
        List<Long> purchaseProductIds,
        List<Long> wishlistProductIds
) {
}
