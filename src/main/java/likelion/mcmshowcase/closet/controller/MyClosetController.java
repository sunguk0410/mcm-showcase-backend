package likelion.mcmshowcase.closet.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import jakarta.validation.Valid;
import likelion.mcmshowcase.closet.dto.MyClosetDetailResponse;
import likelion.mcmshowcase.closet.dto.MyClosetListResponse;
import likelion.mcmshowcase.closet.dto.MyClosetMemberLinkRequest;
import likelion.mcmshowcase.closet.dto.MyClosetMemberLinkResponse;
import likelion.mcmshowcase.closet.service.MyClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my-closet")
@RequiredArgsConstructor
public class MyClosetController {

    private final MyClosetService myClosetService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyClosetListResponse>> getMyCloset(@RequestParam Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(myClosetService.getMyCloset(memberId)));
    }

    @GetMapping("/{styleProfileId}")
    public ResponseEntity<ApiResponse<MyClosetDetailResponse>> getMyClosetDetail(
            @PathVariable Long styleProfileId
    ) {
        return ResponseEntity.ok(ApiResponse.success(myClosetService.getMyClosetDetail(styleProfileId)));
    }

    @PatchMapping("/{styleProfileId}/member")
    public ResponseEntity<ApiResponse<MyClosetMemberLinkResponse>> linkMember(
            @PathVariable Long styleProfileId,
            @Valid @RequestBody MyClosetMemberLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(myClosetService.linkMember(styleProfileId, request)));
    }
}
