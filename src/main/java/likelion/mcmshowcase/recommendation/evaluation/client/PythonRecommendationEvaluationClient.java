package likelion.mcmshowcase.recommendation.evaluation.client;

import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationRequest;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

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
                throw unavailable();
            }
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid recommendation evaluation request"
                );
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Recommendation evaluation server unavailable"
        );
    }
}
