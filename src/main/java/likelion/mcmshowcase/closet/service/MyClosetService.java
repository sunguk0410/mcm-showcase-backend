package likelion.mcmshowcase.closet.service;

import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyClosetService {

    private final MemberRepository memberRepository;
    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;
    private final ArInteractionRepository arInteractionRepository;

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

        List<MyClosetProductResponse> todayLookProducts = todayLookRepository
                .findTopByStyleProfileOrderByCreatedAtDesc(styleProfile)
                .map(todayLook -> todayLookItemRepository
                        .findByTodayLookOrderByDisplayOrderAsc(todayLook)
                        .stream()
                        .map(item -> toProductResponse(item.getProduct()))
                        .toList())
                .orElseGet(List::of);

        List<MyClosetProductResponse> fittingHistory = arInteractionRepository
                .findByArSessionAndInteractionTypeOrderBySequenceNoAsc(
                        styleProfile.getArSession(), ArInteractionType.PRODUCT_SELECT)
                .stream()
                .filter(interaction -> interaction.getProduct() != null)
                .map(interaction -> interaction.getProduct())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Product::getId,
                                Function.identity(),
                                (first, ignored) -> first,
                                LinkedHashMap::new
                        ),
                        productsById -> productsById.values().stream()
                                .map(this::toProductResponse)
                                .toList()
                ));

        return new MyClosetDetailResponse(
                styleProfile.getId(),
                styleProfile.getStyleIdentityTitle(),
                new MyClosetTodayLookResponse(todayLookProducts),
                fittingHistory
        );
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Member not found: " + memberId));
    }

    private MyClosetProductResponse toProductResponse(Product product) {
        return new MyClosetProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductUrl()
        );
    }
}
