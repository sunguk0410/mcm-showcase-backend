package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.ar.service.ArFittingImageService;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLook;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.dto.PythonAvatarLookResponse;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationInteraction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AvatarLookPersistenceService {

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ArFittingImageService arFittingImageService;
    private final ProductRepository productRepository;
    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;

    @Transactional(readOnly = true)
    public AvatarLookContext loadContext(Long arSessionId) {
        ArSession arSession = findArSession(arSessionId);
        StyleProfile styleProfile = styleProfileRepository
                .findTopByArSessionOrderByCreatedAtDesc(arSession)
                .orElse(null);

        return new AvatarLookContext(
                arSessionId,
                styleProfile == null ? null : styleProfile.getId(),
                styleProfile == null ? null : styleProfile.getAvatarImageUrl(),
                styleProfile == null ? getArInteractions(arSession) : List.of()
        );
    }

    @Transactional
    public PreparedAvatarLook saveRecommendation(
            Long arSessionId,
            PythonAvatarLookResponse response
    ) {
        ArSession arSession = arSessionRepository.findByIdForUpdate(arSessionId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.AR_SESSION_NOT_FOUND, "ArSession not found: " + arSessionId));

        StyleProfile existing = styleProfileRepository
                .findTopByArSessionOrderByCreatedAtDesc(arSession)
                .orElse(null);
        if (existing != null) {
            return new PreparedAvatarLook(existing.getId(), existing.getAvatarImageUrl());
        }

        List<Long> productIds = response.products().stream()
                .map(PythonAvatarLookResponse.Product::productId)
                .toList();
        if (productIds.isEmpty()) {
            throw new CustomException(ErrorCode.RECOMMENDATION_EMPTY_AVATAR_LOOK);
        }

        Map<Long, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> orderedProducts = productIds.stream()
                .map(productId -> requireProduct(productsById, productId))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        StyleProfile styleProfile = styleProfileRepository.save(StyleProfile.create(
                arSession,
                response.styleIdentityTitle(),
                resolveAvatarImageUrl(arSession),
                now
        ));
        TodayLook todayLook = todayLookRepository.save(TodayLook.create(styleProfile, now));
        List<TodayLookItem> items = IntStream.range(0, orderedProducts.size())
                .mapToObj(index -> TodayLookItem.create(
                        todayLook, orderedProducts.get(index), index + 1))
                .toList();
        todayLookItemRepository.saveAll(items);
        todayLookItemRepository.flush();

        return new PreparedAvatarLook(styleProfile.getId(), styleProfile.getAvatarImageUrl());
    }

    private ArSession findArSession(Long arSessionId) {
        return arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.AR_SESSION_NOT_FOUND, "ArSession not found: " + arSessionId));
    }

    private List<PythonRecommendationInteraction> getArInteractions(ArSession arSession) {
        List<ArInteraction> history =
                arInteractionRepository.findByArSessionOrderBySequenceNoAsc(arSession);
        return arFittingImageService.filterInteractionsWithAvatarImage(arSession, history)
                .stream()
                .filter(interaction -> interaction.getInteractionType()
                        != ArInteractionType.PRODUCT_DESELECT)
                .map(interaction -> new PythonRecommendationInteraction(
                        interaction.getProduct().getId(),
                        interaction.getInteractionType().name()
                ))
                .toList();
    }

    private Product requireProduct(Map<Long, Product> productsById, Long productId) {
        Product product = productsById.get(productId);
        if (product == null) {
            throw new CustomException(
                    ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        }
        return product;
    }

    private String resolveAvatarImageUrl(ArSession arSession) {
        if (arSession.getGender() == null) {
            throw new CustomException(
                    ErrorCode.AR_SESSION_GENDER_NOT_SET,
                    "Gender is not set for ArSession: " + arSession.getId());
        }
        return switch (arSession.getGender()) {
            case MALE -> "/images/avatars/male.png";
            case FEMALE -> "/images/avatars/female.png";
        };
    }
}
