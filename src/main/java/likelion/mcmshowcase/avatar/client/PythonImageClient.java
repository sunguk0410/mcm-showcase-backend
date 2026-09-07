package likelion.mcmshowcase.avatar.client;

import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.resilience.annotation.Retryable;
import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.avatar.dto.BackgroundRemovalRequest;
import likelion.mcmshowcase.avatar.dto.BackgroundRemovalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.time.Duration;

@Component
public class PythonImageClient {

    private final RestClient restClient;
    private final URI baseUri;

    public PythonImageClient(
            RestClient.Builder restClientBuilder,
            @Value("${recommendation.python.base-url}") String baseUrl,
            @Value("${recommendation.python.image-connect-timeout:5s}") Duration connectTimeout,
            @Value("${recommendation.python.image-read-timeout:60s}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.baseUri = URI.create(baseUrl);
    }

    @Retryable(
            includes = {ResourceAccessException.class, HttpServerErrorException.BadGateway.class,
                    HttpServerErrorException.ServiceUnavailable.class, HttpServerErrorException.GatewayTimeout.class},
            maxRetries = 2, delay = 500, multiplier = 2)
    public String removeBackground(String imageUrl) {
        try {
            BackgroundRemovalResponse response = restClient.post()
                    .uri("/images/remove-background")
                    .body(new BackgroundRemovalRequest(imageUrl))
                    .retrieve()
                    .body(BackgroundRemovalResponse.class);
            if (response == null
                    || response.imageUrl() == null
                    || response.imageUrl().isBlank()) {
                throw new CustomException(ErrorCode.BACKGROUND_REMOVAL_INVALID_RESPONSE);
            }
            return toAbsoluteImageUrl(response.imageUrl());
        } catch (RestClientException exception) {
            throw new CustomException(
                    ErrorCode.BACKGROUND_REMOVAL_SERVER_UNAVAILABLE,
                    exception.getClass().getSimpleName() + ": " + exception.getMessage(), exception);
        }
    }

    private String toAbsoluteImageUrl(String imageUrl) {
        if (imageUrl.startsWith("/")) {
            return baseUri.resolve(imageUrl).toString();
        }
        return imageUrl;
    }
}
