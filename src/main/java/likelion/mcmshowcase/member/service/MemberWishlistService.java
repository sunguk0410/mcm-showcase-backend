package likelion.mcmshowcase.member.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.member.dto.MemberWishlistItemResponse;
import likelion.mcmshowcase.member.dto.MemberWishlistListResponse;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.entity.MemberWishlist;
import likelion.mcmshowcase.member.repository.MemberRepository;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberWishlistService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final MemberWishlistRepository memberWishlistRepository;

    @Transactional
    public void add(Long memberId, Long productId) {
        Member member = findMember(memberId);
        Product product = findProduct(productId);
        if (!memberWishlistRepository.existsByMemberAndProduct(member, product)) {
            memberWishlistRepository.save(MemberWishlist.create(member, product, LocalDateTime.now()));
        }
    }

    @Transactional
    public void remove(Long memberId, Long productId) {
        Member member = findMember(memberId);
        Product product = findProduct(productId);
        memberWishlistRepository.findByMemberAndProduct(member, product)
                .ifPresent(memberWishlistRepository::delete);
    }

    @Transactional(readOnly = true)
    public MemberWishlistListResponse getWishlist(Long memberId) {
        Member member = findMember(memberId);
        return new MemberWishlistListResponse(memberWishlistRepository
                .findByMemberOrderByCreatedAtDescIdDesc(member).stream()
                .map(wishlist -> {
                    Product product = wishlist.getProduct();
                    return new MemberWishlistItemResponse(
                            product.getId(), product.getName(), product.getNameEn(),
                            product.getPrice(), product.getImageUrl(), product.getProductUrl());
                })
                .toList());
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.MEMBER_NOT_FOUND, "Member not found: " + memberId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId));
    }
}
