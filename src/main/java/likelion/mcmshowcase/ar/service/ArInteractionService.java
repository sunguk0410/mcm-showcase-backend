package likelion.mcmshowcase.ar.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArInteractionService {

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final MemberWishlistRepository memberWishlistRepository;

    @Transactional
    public ArInteractionCreateResponse create(ArInteractionCreateRequest request) {
        ArSession arSession = arSessionRepository.findById(request.arSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + request.arSessionId()));

        if (arSession.getEndedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ArInteraction cannot be added to an ended ArSession");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + request.productId()));

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

        return new ArInteractionCreateResponse(
                savedArInteraction.getId(),
                arSession.getId(),
                product.getId(),
                savedArInteraction.getInteractionType(),
                savedArInteraction.getSequenceNo(),
                savedArInteraction.getCreatedAt()
        );
    }

    private void synchronizeWishlist(
            ArSession arSession,
            Product product,
            ArInteractionType interactionType,
            LocalDateTime createdAt
    ) {
        Member member = arSession.getCustomerSession().getMember();
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
