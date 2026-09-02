package likelion.mcmshowcase.testapi.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import likelion.mcmshowcase.testapi.dto.LatestAvatarImageResponse;
import likelion.mcmshowcase.testapi.service.TestAvatarImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/avatar-images")
@RequiredArgsConstructor
public class TestAvatarImageController {

    private final TestAvatarImageService testAvatarImageService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<LatestAvatarImageResponse>> getLatestAvatarImage() {
        return ResponseEntity.ok(ApiResponse.success(testAvatarImageService.getLatestAvatarImage()));
    }
}
