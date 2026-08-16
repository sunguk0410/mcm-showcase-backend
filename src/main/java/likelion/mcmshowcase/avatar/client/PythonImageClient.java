package likelion.mcmshowcase.avatar.client;

import likelion.mcmshowcase.avatar.dto.BackgroundRemovalRequest;
import likelion.mcmshowcase.avatar.dto.BackgroundRemovalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Component
public class PythonImageClient {

    private final RestClient restClient;

    public PythonImageClient(
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
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Background removal server returned an invalid response"
                );
            }
            return response.imageUrl();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Background removal server unavailable",
                    exception
            );
        }
    }
}
