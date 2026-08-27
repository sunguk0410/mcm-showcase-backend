package likelion.mcmshowcase.avatar.service;

import likelion.mcmshowcase.avatar.dto.AvatarGenerationInput;
import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.global.url.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StyleProfile not found: " + styleProfileId));
        validateBaseAvatar(styleProfile);

        List<TodayLookItem> items = todayLookRepository
                .findTopByStyleProfileOrderByCreatedAtDesc(styleProfile)
                .map(todayLookItemRepository::findByTodayLookOrderByDisplayOrderAsc)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StyleProfile not found: " + styleProfileId));
        styleProfile.updateAvatarImageUrl(imageUrl);
    }

    private void validateBaseAvatar(StyleProfile styleProfile) {
        if (styleProfile.getAvatarImageUrl() == null
                || styleProfile.getAvatarImageUrl().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Base avatar image is missing for StyleProfile: " + styleProfile.getId()
            );
        }
    }

    private void validateItems(List<TodayLookItem> items) {
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TodayLook is empty");
        }
        if (items.size() + 1 > MAX_REFERENCE_IMAGES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "FLUX supports at most 7 TodayLook product images"
            );
        }
    }

    private List<AvatarReferenceProduct> createReferenceProducts(List<TodayLookItem> items) {
        List<AvatarReferenceProduct> products = new ArrayList<>();
        for (TodayLookItem item : items) {
            String productImageUrl = item.getProduct().getImageUrl();
            if (productImageUrl == null || productImageUrl.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product image is missing: " + item.getProduct().getId()
                );
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
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
