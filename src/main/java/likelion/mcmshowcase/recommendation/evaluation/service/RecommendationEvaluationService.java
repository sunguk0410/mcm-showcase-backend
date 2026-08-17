package likelion.mcmshowcase.recommendation.evaluation.service;

import likelion.mcmshowcase.recommendation.evaluation.client.PythonRecommendationEvaluationClient;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationRequest;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationEvaluationService {

    private final RecommendationEvaluationValidator evaluationValidator;
    private final PythonRecommendationEvaluationClient evaluationClient;

    public RecommendationEvaluationResponse evaluate(RecommendationEvaluationRequest request) {
        evaluationValidator.validate(request);
        return evaluationClient.evaluate(request);
    }
}
