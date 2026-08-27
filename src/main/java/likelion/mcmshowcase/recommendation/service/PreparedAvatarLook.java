package likelion.mcmshowcase.recommendation.service;

public record PreparedAvatarLook(
        Long styleProfileId,
        String avatarImageUrl
) {
    public boolean hasGeneratedAvatar() {
        return avatarImageUrl != null && avatarImageUrl.startsWith("/images/generated/");
    }
}
