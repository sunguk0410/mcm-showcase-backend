package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.ar.dto.ArInteractionCreateRequest;
import likelion.mcmshowcase.ar.dto.ArInteractionCreateResponse;
import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.entity.MemberWishlist;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArInteractionService {

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final MemberWishlistRepository memberWishlistRepository;
    private final ArFittingImageService arFittingImageService;

    @Transactional
    public ArInteractionCreateResponse create(ArInteractionCreateRequest request) {
        ArSession arSession = arSessionRepository.findById(request.arSessionId())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.AR_SESSION_NOT_FOUND, "ArSession not found: " + request.arSessionId()));

        if (arSession.getEndedAt() != null) {
            throw new CustomException(ErrorCode.INTERACTION_ON_ENDED_AR_SESSION);
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + request.productId()));

        int sequenceNo = arInteractionRepository.findTopByArSessionOrderBySequenceNoDesc(arSession)
                .map(interaction -> interaction.getSequenceNo() + 1)
                .orElse(1);
        LocalDateTime now = LocalDateTime.now();

        ArInteraction arInteraction = ArInteraction.create(
                arSession,
                product,
                request.interactionType(),
                sequenceNo,
                now
        );
        ArInteraction savedArInteraction = arInteractionRepository.save(arInteraction);
        synchronizeWishlist(arSession, product, request.interactionType(), now);
        String avatarImageUrl = isProductSelectionChange(request.interactionType())
                ? arFittingImageService.resolve(arSession)
                : null;

        return new ArInteractionCreateResponse(
                savedArInteraction.getId(),
                arSession.getId(),
                product.getId(),
                savedArInteraction.getInteractionType(),
                avatarImageUrl,
                savedArInteraction.getSequenceNo(),
                savedArInteraction.getCreatedAt()
        );
    }

    private boolean isProductSelectionChange(ArInteractionType interactionType) {
        return interactionType == ArInteractionType.PRODUCT_SELECT
                || interactionType == ArInteractionType.PRODUCT_DESELECT;
    }

    private void synchronizeWishlist(
            ArSession arSession,
            Product product,
            ArInteractionType interactionType,
            LocalDateTime createdAt
    ) {
        Member member = arSession.getMember();
        if (member == null) {
            return;
        }

        if (interactionType == ArInteractionType.WISHLIST_ADD) {
            if (!memberWishlistRepository.existsByMemberAndProduct(member, product)) {
                memberWishlistRepository.save(MemberWishlist.create(member, product, createdAt));
            }
            return;
        }

        if (interactionType == ArInteractionType.WISHLIST_REMOVE) {
            memberWishlistRepository.findByMemberAndProduct(member, product)
                    .ifPresent(memberWishlistRepository::delete);
        }
    }
}
