package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.recommendation.dto.PythonRecommendationInteraction;

import java.util.List;

public record AvatarLookContext(
        Long arSessionId,
        Long styleProfileId,
        String avatarImageUrl,
        List<PythonRecommendationInteraction> interactions
) {
    public boolean hasStyleProfile() {
        return styleProfileId != null;
    }

    public boolean hasGeneratedAvatar() {
        return avatarImageUrl != null && avatarImageUrl.startsWith("/images/generated/");
    }
}
