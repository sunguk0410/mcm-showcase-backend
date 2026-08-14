package likelion.mcmshowcase.styleanalysis.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLook;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.global.dto.PythonMemberContext;
import likelion.mcmshowcase.global.dto.PythonProductMetadata;
import likelion.mcmshowcase.global.dto.PythonZoneInteraction;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberPurchaseRepository;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.metaverse.entity.Avatar;
import likelion.mcmshowcase.metaverse.entity.AvatarPreset;
import likelion.mcmshowcase.metaverse.repository.AvatarPresetRepository;
import likelion.mcmshowcase.metaverse.repository.AvatarRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.styleanalysis.client.PythonStyleAnalysisClient;
import likelion.mcmshowcase.styleanalysis.dto.*;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StyleAnalysisService {

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;
    private final ZoneInteractionRepository zoneInteractionRepository;
    private final MemberPurchaseRepository memberPurchaseRepository;
    private final MemberWishlistRepository memberWishlistRepository;
    private final AvatarPresetRepository avatarPresetRepository;
    private final AvatarRepository avatarRepository;
    private final PythonStyleAnalysisClient pythonStyleAnalysisClient;

    @Transactional
    public StyleAnalysisResponse analyze(Long arSessionId) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));

        List<PythonStyleAnalysisInteraction> interactions = arInteractionRepository
                .findByArSessionOrderBySequenceNoAsc(arSession)
                .stream()
                .map(interaction -> new PythonStyleAnalysisInteraction(
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

        PythonStyleAnalysisResponse pythonResponse = pythonStyleAnalysisClient.analyze(
                new PythonStyleAnalysisRequest(
                        arSession.getId(), arSession.getGender(), interactions,
                        zoneInteractions, memberContext
                )
        );

        List<Long> productIds = pythonResponse.todayLook().productIds();
        Map<Long, Product> productsById = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> orderedProducts = productIds.stream()
                .map(productsById::get)
                .filter(product -> product != null)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        StyleProfile styleProfile = styleProfileRepository.save(
                StyleProfile.create(arSession, pythonResponse.styleIdentityTitle(), now)
        );
        TodayLook todayLook = todayLookRepository.save(TodayLook.create(styleProfile, now));

        List<TodayLookItem> items = java.util.stream.IntStream.range(0, orderedProducts.size())
                .mapToObj(index -> TodayLookItem.create(todayLook, orderedProducts.get(index), index + 1))
                .toList();
        todayLookItemRepository.saveAll(items);

        AvatarPreset avatarPreset = avatarPresetRepository.findById(pythonResponse.avatarPresetId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "AvatarPreset not found: " + pythonResponse.avatarPresetId()
                ));
        avatarRepository.save(Avatar.create(styleProfile, avatarPreset, now));

        List<StyleAnalysisProductResponse> products = orderedProducts.stream()
                .map(this::toResponse)
                .toList();
        return new StyleAnalysisResponse(
                arSession.getId(),
                styleProfile.getId(),
                styleProfile.getStyleIdentityTitle(),
                new TodayLookResponse(products)
        );
    }

    private StyleAnalysisProductResponse toResponse(Product product) {
        return new StyleAnalysisProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getName(),
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
                product.getSubCategory(),
                product.getZone(),
                product.getColor(),
                product.getGender()
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
