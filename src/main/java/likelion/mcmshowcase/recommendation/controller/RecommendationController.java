package likelion.mcmshowcase.recommendation.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.AvatarLookResponse;
import likelion.mcmshowcase.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/avatar-look/{arSessionId}")
    public ResponseEntity<ApiResponse<AvatarLookResponse>> createAvatarLook(@PathVariable Long arSessionId) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.createAvatarLook(arSessionId)));
    }

    @PostMapping("/ar-sessions/{arSessionId}")
    public ResponseEntity<ApiResponse<Void>> initializePreferences(@PathVariable Long arSessionId) {
        recommendationService.initializePreferences(arSessionId);
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }

    @GetMapping("/ar-sessions/{arSessionId}/categories/{categoryCode}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getInitialRecommendations(
            @PathVariable Long arSessionId,
            @PathVariable String categoryCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getInitialRecommendations(arSessionId, categoryCode)));
    }

    @PostMapping("/ar-sessions/{arSessionId}/categories/{categoryCode}/refresh")
    public ResponseEntity<ApiResponse<RecommendationResponse>> refreshRecommendations(
            @PathVariable Long arSessionId,
            @PathVariable String categoryCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.refreshRecommendations(arSessionId, categoryCode)));
    }
}
