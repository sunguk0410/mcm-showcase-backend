package likelion.mcmshowcase.recommendation.evaluation.client;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationRequest;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Component
public class PythonRecommendationEvaluationClient {

    private final RestClient restClient;

    public PythonRecommendationEvaluationClient(
            RestClient.Builder builder,
            @Value("${recommendation.python.base-url}") String baseUrl,
            @Value("${recommendation.python.connect-timeout:2s}") Duration connectTimeout,
            @Value("${recommendation.python.evaluation-read-timeout:60s}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public RecommendationEvaluationResponse evaluate(RecommendationEvaluationRequest request) {
        try {
            RecommendationEvaluationResponse response = restClient.post()
                    .uri("/evaluations/recommendations")
                    .body(request)
                    .retrieve()
                    .body(RecommendationEvaluationResponse.class);
            if (response == null || response.summary() == null || response.personas() == null) {
                throw new CustomException(ErrorCode.RECOMMENDATION_EVALUATION_SERVER_UNAVAILABLE);
            }
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new CustomException(
                        ErrorCode.INVALID_RECOMMENDATION_EVALUATION_REQUEST,
                        exception.getMessage());
            }
            throw unavailable(exception);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private CustomException unavailable(Exception cause) {
        return new CustomException(
                ErrorCode.RECOMMENDATION_EVALUATION_SERVER_UNAVAILABLE,
                cause.getClass().getSimpleName() + ": " + cause.getMessage());
    }
}
