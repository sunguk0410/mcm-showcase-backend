package likelion.mcmshowcase.avatar.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.avatar.dto.AvatarGenerationInput;
import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.global.url.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvatarGenerationPersistenceService {

    private static final int MAX_REFERENCE_IMAGES = 8;

    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;
    private final ImageUrlResolver imageUrlResolver;

    @Transactional(readOnly = true)
    public AvatarGenerationInput loadInput(Long styleProfileId) {
        StyleProfile styleProfile = styleProfileRepository.findById(styleProfileId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.STYLE_PROFILE_NOT_FOUND, "StyleProfile not found: " + styleProfileId));
        validateBaseAvatar(styleProfile);

        List<TodayLookItem> items = todayLookRepository
                .findTopByStyleProfileOrderByCreatedAtDesc(styleProfile)
                .map(todayLookItemRepository::findByTodayLookOrderByDisplayOrderAsc)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.TODAY_LOOK_NOT_FOUND,
                        "TodayLook not found for StyleProfile: " + styleProfileId));
        validateItems(items);

        return new AvatarGenerationInput(
                styleProfileId,
                toPublicUrl(styleProfile.getAvatarImageUrl()),
                createReferenceProducts(items)
        );
    }

    @Transactional
    public void updateAvatarImageUrl(Long styleProfileId, String imageUrl) {
        StyleProfile styleProfile = styleProfileRepository.findById(styleProfileId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.STYLE_PROFILE_NOT_FOUND, "StyleProfile not found: " + styleProfileId));
        styleProfile.updateAvatarImageUrl(imageUrl);
    }

    private void validateBaseAvatar(StyleProfile styleProfile) {
        if (styleProfile.getAvatarImageUrl() == null
                || styleProfile.getAvatarImageUrl().isBlank()) {
            throw new CustomException(
                    ErrorCode.BASE_AVATAR_IMAGE_MISSING,
                    "Base avatar image is missing for StyleProfile: " + styleProfile.getId());
        }
    }

    private void validateItems(List<TodayLookItem> items) {
        if (items.isEmpty()) {
            throw new CustomException(ErrorCode.TODAY_LOOK_EMPTY);
        }
        if (items.size() + 1 > MAX_REFERENCE_IMAGES) {
            throw new CustomException(ErrorCode.TOO_MANY_AVATAR_REFERENCE_IMAGES);
        }
    }

    private List<AvatarReferenceProduct> createReferenceProducts(List<TodayLookItem> items) {
        List<AvatarReferenceProduct> products = new ArrayList<>();
        for (TodayLookItem item : items) {
            String productImageUrl = item.getProduct().getImageUrl();
            if (productImageUrl == null || productImageUrl.isBlank()) {
                throw new CustomException(
                        ErrorCode.PRODUCT_IMAGE_MISSING,
                        "Product image is missing: " + item.getProduct().getId());
            }
            products.add(new AvatarReferenceProduct(
                    toPublicUrl(productImageUrl),
                    item.getProduct().getCategory().getCode(),
                    item.getProduct().getSubCategory(),
                    item.getProduct().getName()
            ));
        }
        return List.copyOf(products);
    }

    private String toPublicUrl(String imageUrl) {
        try {
            return imageUrlResolver.toPublicUrl(imageUrl);
        } catch (IllegalStateException exception) {
            throw new CustomException(ErrorCode.INVALID_PUBLIC_IMAGE_URL, exception.getMessage());
        }
    }
}
