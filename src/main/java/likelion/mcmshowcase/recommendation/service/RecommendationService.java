package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.avatar.service.AvatarGenerationService;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLook;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.client.PythonRecommendationClient;
import likelion.mcmshowcase.recommendation.dto.PythonInitialPreferenceRequest;
import likelion.mcmshowcase.recommendation.dto.AvatarLookResponse;
import likelion.mcmshowcase.recommendation.dto.PythonAvatarLookRequest;
import likelion.mcmshowcase.recommendation.dto.PythonAvatarLookResponse;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationInteraction;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationRequest;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendedProductResponse;
import likelion.mcmshowcase.visit.entity.ZoneInteraction;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private static final int DEFAULT_TOP_K = 6;

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final ZoneInteractionRepository zoneInteractionRepository;
    private final MemberWishlistRepository memberWishlistRepository;
    private final PythonRecommendationClient pythonRecommendationClient;
    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;
    private final AvatarGenerationService avatarGenerationService;

    @Transactional
    public AvatarLookResponse createAvatarLook(Long arSessionId) {
        String stage = "request-start";
        Long styleProfileId = null;
        log.info("Avatar look started. arSessionId={}", arSessionId);
        try {
            stage = "ar-session-lookup";
            ArSession arSession = arSessionRepository.findByIdForUpdate(arSessionId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));
            log.info("ArSession lookup completed. arSessionId={}", arSessionId);

            stage = "style-profile-lookup";
            StyleProfile existingStyleProfile = styleProfileRepository
                    .findTopByArSessionOrderByCreatedAtDesc(arSession)
                    .orElse(null);
            if (existingStyleProfile != null) {
                styleProfileId = existingStyleProfile.getId();
                log.info(
                        "StyleProfile ready. arSessionId={}, styleProfileId={}",
                        arSessionId,
                        styleProfileId
                );
                String avatarImageUrl;
                if (isGeneratedAvatar(existingStyleProfile.getAvatarImageUrl())) {
                    avatarImageUrl = existingStyleProfile.getAvatarImageUrl();
                    log.info(
                            "FLUX avatar generation skipped; generated avatar already exists. "
                                    + "styleProfileId={}",
                            styleProfileId
                    );
                } else {
                    stage = "flux-avatar-generation";
                    log.info("FLUX avatar generation started. styleProfileId={}", styleProfileId);
                    avatarImageUrl = avatarGenerationService.generate(styleProfileId);
                    log.info(
                            "FLUX avatar generation completed. styleProfileId={}, avatarImageUrl={}",
                            styleProfileId,
                            avatarImageUrl
                    );
                }
                log.info(
                        "Avatar look completed. arSessionId={}, styleProfileId={}",
                        arSessionId,
                        styleProfileId
                );
                return new AvatarLookResponse(arSessionId, styleProfileId, avatarImageUrl);
            }

            stage = "python-avatar-look-request";
            log.info("Python avatar look request started. arSessionId={}", arSessionId);
            PythonAvatarLookResponse pythonResponse = pythonRecommendationClient.createAvatarLook(
                    new PythonAvatarLookRequest(arSessionId)
            );
            if (!arSessionId.equals(pythonResponse.arSessionId())) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Recommendation server returned an invalid response"
                );
            }

            List<Long> productIds = pythonResponse.products().stream()
                    .map(PythonAvatarLookResponse.Product::productId)
                    .toList();
            log.info(
                    "Python avatar look response received. arSessionId={}, productCount={}",
                    arSessionId,
                    productIds.size()
            );
            if (productIds.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Recommendation server returned an empty Avatar Look"
                );
            }

            stage = "product-lookup";
            Map<Long, Product> productsById = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));
            List<Product> orderedProducts = productIds.stream()
                    .map(productId -> {
                        Product product = productsById.get(productId);
                        if (product == null) {
                            throw new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Product not found: " + productId);
                        }
                        return product;
                    })
                    .toList();
            log.info(
                    "Product lookup completed. arSessionId={}, productCount={}",
                    arSessionId,
                    orderedProducts.size()
            );

            stage = "style-profile-create";
            LocalDateTime now = LocalDateTime.now();
            String avatarImageUrl = resolveAvatarImageUrl(arSession);
            StyleProfile styleProfile = styleProfileRepository.save(
                    StyleProfile.create(
                            arSession, pythonResponse.styleIdentityTitle(), avatarImageUrl, now)
            );
            styleProfileId = styleProfile.getId();
            log.info(
                    "StyleProfile ready. arSessionId={}, styleProfileId={}",
                    arSessionId,
                    styleProfileId
            );

            stage = "today-look-save";
            TodayLook todayLook = todayLookRepository.save(TodayLook.create(styleProfile, now));
            List<TodayLookItem> items = java.util.stream.IntStream
                    .range(0, orderedProducts.size())
                    .mapToObj(index -> TodayLookItem.create(
                            todayLook, orderedProducts.get(index), index + 1))
                    .toList();
            todayLookItemRepository.saveAll(items);
            todayLookItemRepository.flush();
            log.info(
                    "TodayLook saved. styleProfileId={}, itemCount={}",
                    styleProfileId,
                    items.size()
            );

            stage = "flux-avatar-generation";
            log.info("FLUX avatar generation started. styleProfileId={}", styleProfileId);
            String generatedAvatarImageUrl = avatarGenerationService.generate(styleProfileId);
            log.info(
                    "FLUX avatar generation completed. styleProfileId={}, avatarImageUrl={}",
                    styleProfileId,
                    generatedAvatarImageUrl
            );
            log.info(
                    "Avatar look completed. arSessionId={}, styleProfileId={}",
                    arSessionId,
                    styleProfileId
            );
            return new AvatarLookResponse(
                    arSessionId, styleProfileId, generatedAvatarImageUrl);
        } catch (RuntimeException exception) {
            log.error(
                    "Avatar look failed. arSessionId={}, styleProfileId={}, stage={}",
                    arSessionId,
                    styleProfileId,
                    stage,
                    exception
            );
            throw exception;
        }
    }

    private boolean isGeneratedAvatar(String avatarImageUrl) {
        return avatarImageUrl != null
                && avatarImageUrl.startsWith("/images/generated/");
    }

    @Transactional(readOnly = true)
    public void initializePreferences(Long arSessionId) {
        ArSession arSession = findArSession(arSessionId);
        List<PythonInitialPreferenceRequest.ZoneInteraction> zoneInteractions =
                getZoneInteractions(arSession);
        List<PythonInitialPreferenceRequest.MemberInteraction> memberInteractions =
                getMemberInteractions(arSession);
        if (zoneInteractions.isEmpty() && memberInteractions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recommendation preference data not found for ArSession: " + arSessionId
            );
        }

        pythonRecommendationClient.initializePreferences(
                new PythonInitialPreferenceRequest(
                        arSessionId, zoneInteractions, memberInteractions)
        );
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getInitialRecommendations(Long arSessionId, String categoryCode) {
        return recommend(arSessionId, categoryCode, false);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse refreshRecommendations(Long arSessionId, String categoryCode) {
        return recommend(arSessionId, categoryCode, true);
    }

    private RecommendationResponse recommend(
            Long arSessionId,
            String categoryCode,
            boolean includeArInteractions
    ) {
        ArSession arSession = findArSession(arSessionId);

        List<PythonRecommendationInteraction> interactions = includeArInteractions
                ? getArInteractions(arSession)
                : List.of();

        PythonRecommendationResponse pythonResponse = pythonRecommendationClient.recommend(
                new PythonRecommendationRequest(
                        arSessionId,
                        interactions,
                        categoryCode,
                        DEFAULT_TOP_K
                )
        );

        List<Long> recommendedProductIds = pythonResponse.recommendations().stream()
                .map(PythonRecommendationResponse.Recommendation::productId)
                .toList();
        Map<Long, Product> productsById = productRepository.findAllById(recommendedProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<RecommendedProductResponse> products = pythonResponse.recommendations().stream()
                .filter(recommendation -> productsById.containsKey(recommendation.productId()))
                .filter(recommendation -> productsById.get(recommendation.productId())
                        .getCategory().getCode().equalsIgnoreCase(categoryCode))
                .limit(DEFAULT_TOP_K)
                .map(recommendation -> toResponse(
                        productsById.get(recommendation.productId()), recommendation.score()))
                .toList();

        return new RecommendationResponse(arSession.getId(), products);
    }

    private ArSession findArSession(Long arSessionId) {
        return arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));
    }

    private List<PythonRecommendationInteraction> getArInteractions(ArSession arSession) {
        return arInteractionRepository.findByArSessionOrderBySequenceNoAsc(arSession)
                .stream()
                .map(interaction -> new PythonRecommendationInteraction(
                        interaction.getProduct().getId(),
                        interaction.getInteractionType().name()
                ))
                .toList();
    }

    private List<PythonInitialPreferenceRequest.ZoneInteraction> getZoneInteractions(
            ArSession arSession
    ) {
        if (arSession.getCustomerSession() == null) {
            return List.of();
        }

        Map<ZoneCategoryKey, Long> dwellSecondsByZoneCategory = new LinkedHashMap<>();
        zoneInteractionRepository
                .findByCustomerSessionOrderByEnteredAtAsc(arSession.getCustomerSession())
                .stream()
                .filter(interaction -> interaction.getExitedAt() != null)
                .forEach(interaction -> {
                    ZoneCategoryKey key = new ZoneCategoryKey(
                            toZoneCode(interaction),
                            interaction.getZoneCategory().getCategory().getCode()
                    );
                    long dwellSeconds = Duration.between(
                            interaction.getEnteredAt(), interaction.getExitedAt()).getSeconds();
                    dwellSecondsByZoneCategory.merge(key, dwellSeconds, Long::sum);
                });

        return dwellSecondsByZoneCategory.entrySet().stream()
                .map(entry -> new PythonInitialPreferenceRequest.ZoneInteraction(
                        entry.getKey().zone(),
                        entry.getKey().category(),
                        entry.getValue()
                ))
                .toList();
    }

    private List<PythonInitialPreferenceRequest.MemberInteraction> getMemberInteractions(
            ArSession arSession
    ) {
        Member member = arSession.getMember();
        if (member == null) {
            return List.of();
        }

        return memberWishlistRepository.findByMemberOrderByCreatedAtAsc(member).stream()
                .map(wishlist -> new PythonInitialPreferenceRequest.MemberInteraction(
                        wishlist.getProduct().getId(), "WISHLIST"))
                .toList();
    }

    private RecommendedProductResponse toResponse(Product product, Double score) {
        return new RecommendedProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getCategory().getCode(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductUrl(),
                score
        );
    }

    private String resolveAvatarImageUrl(ArSession arSession) {
        if (arSession.getGender() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gender is not set for ArSession: " + arSession.getId()
            );
        }
        return switch (arSession.getGender()) {
            case MALE -> "/images/avatars/male.png";
            case FEMALE -> "/images/avatars/female.png";
        };
    }

    private String toZoneCode(ZoneInteraction interaction) {
        String floorCode = interaction.getZoneCategory().getZone().getFloorCode();
        return switch (floorCode.toUpperCase(Locale.ROOT)) {
            case "1F", "NEW", "NEW_COLLECTION" -> "NEW_COLLECTION";
            case "2F", "CLASSIC" -> "CLASSIC";
            case "3F", "TRAVEL" -> "TRAVEL";
            default -> floorCode;
        };
    }

    private record ZoneCategoryKey(String zone, String category) {
    }
}
