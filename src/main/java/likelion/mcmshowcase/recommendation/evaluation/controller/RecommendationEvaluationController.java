package likelion.mcmshowcase.recommendation.evaluation.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import jakarta.validation.Valid;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationRequest;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationResponse;
import likelion.mcmshowcase.recommendation.evaluation.service.RecommendationEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class RecommendationEvaluationController {

    private final RecommendationEvaluationService evaluationService;

    @PostMapping("/recommendations")
    public ResponseEntity<ApiResponse<RecommendationEvaluationResponse>> evaluate(
            @Valid @RequestBody RecommendationEvaluationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(evaluationService.evaluate(request)));
    }
}
