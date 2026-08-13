package likelion.mcmshowcase.recommendation.controller;

import likelion.mcmshowcase.recommendation.dto.RecommendationResponse;
import likelion.mcmshowcase.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/ar-sessions/{arSessionId}")
    public ResponseEntity<RecommendationResponse> recommend(@PathVariable Long arSessionId) {
        return ResponseEntity.ok(recommendationService.recommend(arSessionId));
    }
}
