package likelion.mcmshowcase.recommendation.client;

import likelion.mcmshowcase.recommendation.dto.PythonInitialPreferenceRequest;
import likelion.mcmshowcase.recommendation.dto.PythonAvatarLookRequest;
import likelion.mcmshowcase.recommendation.dto.PythonAvatarLookResponse;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationRequest;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Component
public class PythonRecommendationClient {

    private final RestClient restClient;

    public PythonRecommendationClient(
            RestClient.Builder restClientBuilder,
            @Value("${recommendation.python.base-url}") String baseUrl,
            @Value("${recommendation.python.connect-timeout:2s}") Duration connectTimeout,
            @Value("${recommendation.python.read-timeout:5s}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public PythonRecommendationResponse recommend(PythonRecommendationRequest request) {
        try {
            PythonRecommendationResponse response = restClient.post()
                    .uri("/recommend")
                    .body(request)
                    .retrieve()
                    .body(PythonRecommendationResponse.class);

            if (response == null
                    || response.recommendations() == null
                    || response.recommendations().stream().anyMatch(recommendation ->
                    recommendation == null
                            || recommendation.productId() == null
                            || recommendation.score() == null)) {
                throw invalidResponse();
            }
            return response;
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Recommendation server unavailable"
            );
        } catch (RestClientResponseException exception) {
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    public void initializePreferences(PythonInitialPreferenceRequest request) {
        try {
            restClient.post()
                    .uri("/preferences/initialize")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw unavailable();
        } catch (RestClientResponseException exception) {
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    public PythonAvatarLookResponse createAvatarLook(PythonAvatarLookRequest request) {
        try {
            PythonAvatarLookResponse response = restClient.post()
                    .uri("/recommendations/avatar-look")
                    .body(request)
                    .retrieve()
                    .body(PythonAvatarLookResponse.class);

            if (response == null
                    || response.arSessionId() == null
                    || response.styleIdentityTitle() == null
                    || response.styleIdentityTitle().isBlank()
                    || response.products() == null
                    || response.products().stream().anyMatch(product ->
                    product == null || product.productId() == null)) {
                throw invalidResponse();
            }
            return response;
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw unavailable();
        } catch (RestClientResponseException exception) {
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Recommendation server unavailable"
        );
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Recommendation server unavailable"
        );
    }
}
