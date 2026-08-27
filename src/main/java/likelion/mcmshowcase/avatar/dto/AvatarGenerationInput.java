package likelion.mcmshowcase.avatar.dto;

import java.util.List;

public record AvatarGenerationInput(
        Long styleProfileId,
        String baseAvatarUrl,
        List<AvatarReferenceProduct> referenceProducts
) {
}
