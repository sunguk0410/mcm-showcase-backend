package likelion.mcmshowcase.recommendation.controller;

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
    public ResponseEntity<AvatarLookResponse> createAvatarLook(@PathVariable Long arSessionId) {
        return ResponseEntity.ok(recommendationService.createAvatarLook(arSessionId));
    }

    @PostMapping("/ar-sessions/{arSessionId}")
    public ResponseEntity<Void> initializePreferences(@PathVariable Long arSessionId) {
        recommendationService.initializePreferences(arSessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ar-sessions/{arSessionId}/categories/{categoryCode}")
    public ResponseEntity<RecommendationResponse> getInitialRecommendations(
            @PathVariable Long arSessionId,
            @PathVariable String categoryCode
    ) {
        return ResponseEntity.ok(
                recommendationService.getInitialRecommendations(arSessionId, categoryCode)
        );
    }

    @PostMapping("/ar-sessions/{arSessionId}/categories/{categoryCode}/refresh")
    public ResponseEntity<RecommendationResponse> refreshRecommendations(
            @PathVariable Long arSessionId,
            @PathVariable String categoryCode
    ) {
        return ResponseEntity.ok(
                recommendationService.refreshRecommendations(arSessionId, categoryCode)
        );
    }
}
