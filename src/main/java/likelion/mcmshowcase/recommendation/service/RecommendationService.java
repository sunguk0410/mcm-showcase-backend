package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.global.dto.PythonMemberContext;
import likelion.mcmshowcase.global.dto.PythonProductMetadata;
import likelion.mcmshowcase.global.dto.PythonZoneInteraction;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberPurchaseRepository;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.client.PythonRecommendationClient;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationInteraction;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationRequest;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendedProductResponse;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final ZoneInteractionRepository zoneInteractionRepository;
    private final MemberPurchaseRepository memberPurchaseRepository;
    private final MemberWishlistRepository memberWishlistRepository;
    private final PythonRecommendationClient pythonRecommendationClient;

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(Long arSessionId) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));

        List<PythonRecommendationInteraction> interactions = arInteractionRepository
                .findByArSessionOrderBySequenceNoAsc(arSession)
                .stream()
                .map(interaction -> new PythonRecommendationInteraction(
                        interaction.getProduct() == null ? null : interaction.getProduct().getId(),
                        interaction.getInteractionType(),
                        interaction.getSequenceNo(),
                        toProductMetadata(interaction.getProduct())
                ))
                .toList();

        List<PythonZoneInteraction> zoneInteractions = zoneInteractionRepository
                .findByCustomerSessionOrderByEnteredAtAsc(arSession.getCustomerSession())
                .stream()
                .map(interaction -> new PythonZoneInteraction(
                        interaction.getZoneCategory().getZone().getFloorCode(),
                        interaction.getZoneCategory().getCategory().getCode(),
                        interaction.getDwellSeconds()
                ))
                .toList();

        PythonMemberContext memberContext = createMemberContext(
                arSession.getCustomerSession().getMember()
        );

        PythonRecommendationResponse pythonResponse = pythonRecommendationClient.recommend(
                new PythonRecommendationRequest(
                        arSession.getId(), arSession.getGender(), interactions,
                        zoneInteractions, memberContext
                )
        );

        List<Long> recommendedProductIds = pythonResponse.recommendedProductIds();
        Map<Long, Product> productsById = productRepository.findAllById(recommendedProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<RecommendedProductResponse> products = recommendedProductIds.stream()
                .map(productsById::get)
                .filter(product -> product != null)
                .map(this::toResponse)
                .toList();

        return new RecommendationResponse(arSession.getId(), products);
    }

    private RecommendedProductResponse toResponse(Product product) {
        return new RecommendedProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductUrl()
        );
    }

    private PythonProductMetadata toProductMetadata(Product product) {
        if (product == null) {
            return null;
        }
        return new PythonProductMetadata(
                product.getCategory().getCode(),
                product.getColor(),
                product.getMaterial(),
                product.getSilhouette(),
                product.getStyle()
        );
    }

    private PythonMemberContext createMemberContext(Member member) {
        if (member == null) {
            return null;
        }
        List<Long> purchaseProductIds = memberPurchaseRepository
                .findByMemberOrderByPurchasedAtAsc(member)
                .stream()
                .map(purchase -> purchase.getProduct().getId())
                .toList();
        List<Long> wishlistProductIds = memberWishlistRepository
                .findByMemberOrderByCreatedAtAsc(member)
                .stream()
                .map(wishlist -> wishlist.getProduct().getId())
                .toList();
        return new PythonMemberContext(purchaseProductIds, wishlistProductIds);
    }
}
