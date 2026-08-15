package likelion.mcmshowcase.avatar.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FluxClient {

    private static final String PROMPT = """
            Use the first image as the base avatar.
            Dress the avatar using the fashion products shown in the reference images.
            Preserve the avatar's face, body proportions, pose and overall appearance.
            Apply the clothing and accessories naturally.
            Generate a clean full-body fashion styling image.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String modelPath;
    private final Duration pollingInterval;
    private final Duration maxPollingDuration;

    public FluxClient(
            RestClient.Builder restClientBuilder,
            @Value("${flux.base-url:https://api.bfl.ai}") String baseUrl,
            @Value("${flux.api-key:}") String apiKey,
            @Value("${flux.model-path:/v1/flux-2-pro-preview}") String modelPath,
            @Value("${flux.connect-timeout:5s}") Duration connectTimeout,
            @Value("${flux.read-timeout:30s}") Duration readTimeout,
            @Value("${flux.polling-interval:500ms}") Duration pollingInterval,
            @Value("${flux.max-polling-duration:2m}") Duration maxPollingDuration
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.apiKey = apiKey;
        this.modelPath = modelPath;
        this.pollingInterval = pollingInterval;
        this.maxPollingDuration = maxPollingDuration;
    }

    public String generateAvatar(List<String> imageUrls) {
        validateConfiguration();
        log.info(
                "Submitting FLUX request. endpoint={}, imageCount={}, images={}",
                modelPath,
                imageUrls.size(),
                imageUrls
        );
        try {
            FluxSubmitResponse submitResponse = restClient.post()
                    .uri(modelPath)
                    .header("x-key", apiKey)
                    .body(createRequestBody(imageUrls))
                    .retrieve()
                    .body(FluxSubmitResponse.class);
            if (submitResponse == null
                    || submitResponse.id() == null
                    || submitResponse.pollingUrl() == null
                    || submitResponse.pollingUrl().isBlank()) {
                throw invalidResponse();
            }
            return pollUntilReady(submitResponse.pollingUrl());
        } catch (RestClientResponseException exception) {
            log.error(
                    "FLUX submit failed. status={}, body={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception
            );
            throw unavailable("FLUX API request failed");
        } catch (ResourceAccessException exception) {
            log.error("FLUX submit connection failed or timed out.", exception);
            throw unavailable("FLUX API connection failed or timed out");
        } catch (RestClientException exception) {
            log.error("FLUX submit failed.", exception);
            throw unavailable("FLUX API request failed");
        }
    }

    public byte[] downloadGeneratedImage(String imageUrl) {
        try {
            byte[] image = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);
            if (image == null || image.length == 0) {
                throw invalidResponse();
            }
            return image;
        } catch (RestClientResponseException exception) {
            log.error(
                    "FLUX result image download failed. status={}, body={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception
            );
            throw unavailable("FLUX image download failed");
        } catch (ResourceAccessException exception) {
            log.error("FLUX result image download failed or timed out. url={}", imageUrl, exception);
            throw unavailable("FLUX image download failed or timed out");
        } catch (RestClientException exception) {
            log.error("FLUX result image download failed. url={}", imageUrl, exception);
            throw unavailable("FLUX image download failed");
        }
    }

    private String pollUntilReady(String pollingUrl) {
        long deadline = System.nanoTime() + maxPollingDuration.toNanos();
        while (System.nanoTime() < deadline) {
            waitBeforePolling();
            FluxPollResponse response;
            try {
                response = restClient.get()
                        .uri(pollingUrl)
                        .header("x-key", apiKey)
                        .retrieve()
                        .body(FluxPollResponse.class);
            } catch (RestClientResponseException exception) {
                log.error(
                        "FLUX polling failed. status={}, body={}",
                        exception.getStatusCode(),
                        exception.getResponseBodyAsString(),
                        exception
                );
                throw unavailable("FLUX API polling failed");
            } catch (ResourceAccessException exception) {
                log.error("FLUX polling connection failed or timed out. url={}", pollingUrl, exception);
                throw unavailable("FLUX API polling failed or timed out");
            } catch (RestClientException exception) {
                log.error("FLUX polling failed. url={}", pollingUrl, exception);
                throw unavailable("FLUX API polling failed");
            }
            if (response == null || response.status() == null) {
                throw invalidResponse();
            }
            if ("Ready".equalsIgnoreCase(response.status())) {
                if (response.result() == null
                        || response.result().sample() == null
                        || response.result().sample().isBlank()) {
                    throw invalidResponse();
                }
                return response.result().sample();
            }
            if ("Error".equalsIgnoreCase(response.status())
                    || "Failed".equalsIgnoreCase(response.status())) {
                log.error("FLUX image generation failed during polling. status={}", response.status());
                throw unavailable("FLUX image generation failed");
            }
        }
        throw new ResponseStatusException(
                HttpStatus.GATEWAY_TIMEOUT, "FLUX image generation timed out");
    }

    private Map<String, Object> createRequestBody(List<String> imageUrls) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prompt", PROMPT);
        body.put("output_format", "png");
        for (int index = 0; index < imageUrls.size(); index++) {
            String fieldName = index == 0 ? "input_image" : "input_image_" + (index + 1);
            body.put(fieldName, imageUrls.get(index));
        }
        return body;
    }

    private void waitBeforePolling() {
        try {
            Thread.sleep(pollingInterval.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw unavailable("FLUX image generation was interrupted");
        }
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "BFL_API_KEY is not configured");
        }
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY, "FLUX API returned an invalid response");
    }

    private ResponseStatusException unavailable(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }

    private record FluxSubmitResponse(
            String id,
            @JsonProperty("polling_url") String pollingUrl
    ) {
    }

    private record FluxPollResponse(String status, FluxResult result) {
    }

    private record FluxResult(String sample) {
    }
}
