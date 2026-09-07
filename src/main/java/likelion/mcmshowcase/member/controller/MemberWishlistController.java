package likelion.mcmshowcase.member.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import likelion.mcmshowcase.member.dto.MemberWishlistListResponse;
import likelion.mcmshowcase.member.service.MemberWishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/{memberId}/wishlist")
@RequiredArgsConstructor
public class MemberWishlistController {

    private final MemberWishlistService memberWishlistService;

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> add(
            @PathVariable Long memberId, @PathVariable Long productId
    ) {
        memberWishlistService.add(memberId, productId);
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable Long memberId, @PathVariable Long productId
    ) {
        memberWishlistService.remove(memberId, productId);
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MemberWishlistListResponse>> getWishlist(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberWishlistService.getWishlist(memberId)));
    }
}
