package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLook;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
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
public class RecommendationService {

    private static final int DEFAULT_TOP_K = 6;

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final ZoneInteractionRepository zoneInteractionRepository;
    private final PythonRecommendationClient pythonRecommendationClient;
    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;

    @Transactional
    public AvatarLookResponse createAvatarLook(Long arSessionId) {
        ArSession arSession = findArSession(arSessionId);
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

        LocalDateTime now = LocalDateTime.now();
        StyleProfile styleProfile = styleProfileRepository.save(
                StyleProfile.create(arSession, pythonResponse.styleIdentityTitle(), now)
        );
        TodayLook todayLook = todayLookRepository.save(TodayLook.create(styleProfile, now));
        List<TodayLookItem> items = java.util.stream.IntStream.range(0, orderedProducts.size())
                .mapToObj(index -> TodayLookItem.create(
                        todayLook, orderedProducts.get(index), index + 1))
                .toList();
        todayLookItemRepository.saveAll(items);

        List<AvatarLookResponse.Product> products = orderedProducts.stream()
                .map(product -> new AvatarLookResponse.Product(
                        product.getId(),
                        product.getProductCode(),
                        product.getName(),
                        product.getImageUrl(),
                        product.getProductUrl()
                ))
                .toList();
        return new AvatarLookResponse(
                arSessionId,
                styleProfile.getId(),
                styleProfile.getStyleIdentityTitle(),
                new AvatarLookResponse.TodayLook(products)
        );
    }

    @Transactional(readOnly = true)
    public void initializePreferences(Long arSessionId) {
        ArSession arSession = findArSession(arSessionId);
        List<PythonInitialPreferenceRequest.ZoneInteraction> zoneInteractions =
                getZoneInteractions(arSession);
        if (zoneInteractions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ZoneInteraction not found for ArSession: " + arSessionId
            );
        }

        pythonRecommendationClient.initializePreferences(
                new PythonInitialPreferenceRequest(arSessionId, zoneInteractions)
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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CustomerSession is not mapped to ArSession: " + arSession.getId()
            );
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
