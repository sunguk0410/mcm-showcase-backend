package likelion.mcmshowcase.closet.service;

import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.closet.dto.*;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberRepository;
import likelion.mcmshowcase.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MyClosetService {

    private final MemberRepository memberRepository;
    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ArSessionRepository arSessionRepository;

    @Transactional
    public MyClosetMemberLinkResponse linkMember(
            Long styleProfileId,
            MyClosetMemberLinkRequest request
    ) {
        StyleProfile styleProfile = styleProfileRepository.findById(styleProfileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StyleProfile not found: " + styleProfileId));
        Member member = findMember(request.memberId());
        if (styleProfile.getArSession().getMember() != null
                && !styleProfile.getArSession().getMember().getId().equals(member.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ArSession is already mapped to another Member");
        }

        styleProfile.getArSession().mapMember(member);
        arSessionRepository.save(styleProfile.getArSession());
        return new MyClosetMemberLinkResponse(styleProfileId, member.getId());
    }

    @Transactional(readOnly = true)
    public MyClosetListResponse getMyCloset(Long memberId) {
        Member member = findMember(memberId);
        List<MyClosetListItemResponse> items = styleProfileRepository
                .findByArSessionMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(styleProfile -> new MyClosetListItemResponse(
                        styleProfile.getId(),
                        styleProfile.getStyleIdentityTitle(),
                        styleProfile.getCreatedAt()
                ))
                .toList();
        return new MyClosetListResponse(items);
    }

    @Transactional(readOnly = true)
    public MyClosetDetailResponse getMyClosetDetail(Long styleProfileId) {
        StyleProfile styleProfile = styleProfileRepository.findById(styleProfileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StyleProfile not found: " + styleProfileId));

        List<ArInteraction> arInteractions = arInteractionRepository
                .findByArSessionOrderBySequenceNoAsc(styleProfile.getArSession());
        Map<Long, Boolean> wishlistStatusByProductId = new LinkedHashMap<>();
        arInteractions.stream()
                .filter(interaction -> interaction.getProduct() != null)
                .filter(interaction -> interaction.getInteractionType()
                        == ArInteractionType.WISHLIST_ADD
                        || interaction.getInteractionType() == ArInteractionType.WISHLIST_REMOVE)
                .forEach(interaction -> wishlistStatusByProductId.put(
                        interaction.getProduct().getId(),
                        interaction.getInteractionType() == ArInteractionType.WISHLIST_ADD
                ));

        List<MyClosetProductResponse> todayLookProducts = todayLookRepository
                .findTopByStyleProfileOrderByCreatedAtDesc(styleProfile)
                .map(todayLook -> todayLookItemRepository
                        .findByTodayLookOrderByDisplayOrderAsc(todayLook)
                        .stream()
                        .map(item -> toProductResponse(
                                item.getProduct(), wishlistStatusByProductId))
                        .toList())
                .orElseGet(List::of);

        List<MyClosetProductResponse> fittingHistory = arInteractions.stream()
                .filter(interaction -> interaction.getInteractionType()
                        == ArInteractionType.PRODUCT_SELECT)
                .filter(interaction -> interaction.getProduct() != null)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                interaction -> interaction.getProduct().getId(),
                                ArInteraction::getProduct,
                                (first, ignored) -> first,
                                LinkedHashMap::new),
                        productsById -> productsById.values().stream()
                                .map(product -> toProductResponse(
                                        product, wishlistStatusByProductId))
                                .toList()));

        return new MyClosetDetailResponse(
                styleProfile.getId(),
                styleProfile.getStyleIdentityTitle(),
                styleProfile.getAvatarImageUrl(),
                new MyClosetTodayLookResponse(todayLookProducts),
                fittingHistory
        );
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Member not found: " + memberId));
    }

    private MyClosetProductResponse toProductResponse(
            Product product,
            Map<Long, Boolean> wishlistStatusByProductId
    ) {
        return new MyClosetProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductUrl(),
                wishlistStatusByProductId.getOrDefault(product.getId(), false)
        );
    }
}
