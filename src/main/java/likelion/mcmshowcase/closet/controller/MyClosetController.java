package likelion.mcmshowcase.closet.controller;

import likelion.mcmshowcase.closet.dto.MyClosetDetailResponse;
import likelion.mcmshowcase.closet.dto.MyClosetListResponse;
import likelion.mcmshowcase.closet.service.MyClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my-closet")
@RequiredArgsConstructor
public class MyClosetController {

    private final MyClosetService myClosetService;

    @GetMapping
    public ResponseEntity<MyClosetListResponse> getMyCloset(@RequestParam Long memberId) {
        return ResponseEntity.ok(myClosetService.getMyCloset(memberId));
    }

    @GetMapping("/{styleProfileId}")
    public ResponseEntity<MyClosetDetailResponse> getMyClosetDetail(
            @PathVariable Long styleProfileId,
            @RequestParam Long memberId
    ) {
        return ResponseEntity.ok(myClosetService.getMyClosetDetail(styleProfileId, memberId));
    }
}
