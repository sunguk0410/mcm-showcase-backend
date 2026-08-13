package likelion.mcmshowcase.recommendation.client;

import likelion.mcmshowcase.recommendation.dto.PythonRecommendationRequest;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PythonRecommendationClient {

    private final RestClient restClient;

    public PythonRecommendationClient(
            RestClient.Builder restClientBuilder,
            @Value("${recommendation.python.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public PythonRecommendationResponse recommend(PythonRecommendationRequest request) {
        try {
            PythonRecommendationResponse response = restClient.post()
                    .uri("/recommendations")
                    .body(request)
                    .retrieve()
                    .body(PythonRecommendationResponse.class);

            if (response == null
                    || response.recommendedProductIds() == null
                    || response.recommendedProductIds().stream().anyMatch(id -> id == null)) {
                throw invalidResponse();
            }
            return response;
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Recommendation server is unavailable."
            );
        } catch (RestClientResponseException exception) {
            throw invalidResponse();
        } catch (RestClientException exception) {
            throw invalidResponse();
        }
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Recommendation server returned an invalid response."
        );
    }
}
