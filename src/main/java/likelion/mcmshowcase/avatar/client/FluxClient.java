package likelion.mcmshowcase.avatar.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
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

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class FluxClient {

    private static final String PROMPT_PREFIX = """
            Use image 1 as the base avatar.

            Keep the avatar's face, hairstyle, body shape, pose, proportions, and overall identity unchanged.
            Create a clean, stylish, realistic full-body fashion look using the referenced products.

            Apply the referenced products exactly as follows:
            """;

    private static final String PROMPT_SUFFIX = """

            Important instructions:
            - Use each referenced product exactly once.
            - Preserve the original color, material, silhouette, logo, pattern, proportions, and design details of each referenced product.
            - Do not invent additional clothing, bags, shoes, or accessories.
            - Do not duplicate any referenced product.
            - Do not replace a referenced product with a different design.
            - Keep the avatar centered and fully visible from head to toe.
            - Do not crop the head, hands, legs, or feet.
            - Keep the original avatar pose and body proportions.
            - Do not change the avatar's face or hairstyle.
            - Make every referenced item look naturally worn or carried.
            - Ensure every referenced product remains clearly visible.
            - Avoid unnecessary overlap that hides important product details.
            - Keep the background simple and clean.
            """;

    private final RestClient restClient;
    private final RestClient imageDownloadClient;
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
        SimpleClientHttpRequestFactory requestFactory = createRequestFactory(
                connectTimeout, readTimeout);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.imageDownloadClient = RestClient.builder()
                .requestFactory(createRequestFactory(connectTimeout, readTimeout))
                .build();
        this.apiKey = apiKey;
        this.modelPath = modelPath;
        this.pollingInterval = pollingInterval;
        this.maxPollingDuration = maxPollingDuration;
    }

    public String generateAvatar(
            String baseAvatarUrl,
            List<AvatarReferenceProduct> referenceProducts
    ) {
        validateConfiguration();
        log.info(
                "Submitting FLUX request. endpoint={}, imageCount={}, images={}",
                modelPath,
                referenceProducts.size() + 1,
                referenceProducts.stream().map(AvatarReferenceProduct::imageUrl).toList()
        );
        try {
            FluxSubmitResponse submitResponse = restClient.post()
                    .uri(modelPath)
                    .header("x-key", apiKey)
                    .body(createRequestBody(baseAvatarUrl, referenceProducts))
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
            byte[] image = imageDownloadClient.get()
                    .uri(URI.create(imageUrl))
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
                        .uri(URI.create(pollingUrl))
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

    Map<String, Object> createRequestBody(
            String baseAvatarUrl,
            List<AvatarReferenceProduct> referenceProducts
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prompt", buildPrompt(referenceProducts));
        body.put("output_format", "png");
        body.put("input_image", baseAvatarUrl);
        for (int index = 0; index < referenceProducts.size(); index++) {
            int imageNumber = index + 2;
            body.put("input_image_" + imageNumber, referenceProducts.get(index).imageUrl());
        }
        return body;
    }

    String buildPrompt(List<AvatarReferenceProduct> referenceProducts) {
        StringBuilder prompt = new StringBuilder(PROMPT_PREFIX);
        for (int index = 0; index < referenceProducts.size(); index++) {
            prompt.append(buildProductInstruction(index + 2, referenceProducts.get(index)))
                    .append('\n');
        }
        return prompt.append(PROMPT_SUFFIX).toString();
    }

    private String buildProductInstruction(int imageNumber, AvatarReferenceProduct product) {
        String prefix = "- image " + imageNumber + ": ";
        String category = normalize(product.category());
        String subCategory = normalize(product.subCategory());

        return prefix + switch (category) {
            case "TOP" -> "wear this exact product as the upper-body garment";
            case "BOTTOM" -> "wear this exact product as the lower-body garment";
            case "SHOES" -> "wear these exact shoes naturally on both feet";
            case "BAG" -> buildBagInstruction(subCategory);
            case "ACCESSORIES" -> buildAccessoryInstruction(subCategory);
            default -> "incorporate this exact product naturally into the outfit "
                    + "in a position appropriate for its design";
        };
    }

    private String buildBagInstruction(String subCategory) {
        if (subCategory.contains("BACKPACK")) {
            return "wear this exact backpack naturally on the back using both shoulder straps";
        }
        if (subCategory.contains("CROSSBODY")) {
            return "wear this exact crossbody bag diagonally across the torso";
        }
        if (subCategory.contains("SHOULDER")) {
            return "carry this exact shoulder bag naturally on one shoulder";
        }
        if (subCategory.contains("TOTE")) {
            return "carry this exact tote bag naturally in one hand or over one arm";
        }
        if (subCategory.contains("CLUTCH")) {
            return "hold this exact clutch naturally in one hand";
        }
        return "carry this exact bag naturally in a way appropriate for its design";
    }

    private String buildAccessoryInstruction(String subCategory) {
        if (subCategory.contains("HAT")
                || subCategory.contains("CAP")
                || subCategory.contains("HEADWEAR")) {
            return "wear this exact accessory naturally on the head";
        }
        if (subCategory.contains("BELT")) {
            return "wear this exact belt naturally around the waist";
        }
        if (subCategory.contains("NECKLACE")) {
            return "wear this exact accessory naturally around the neck";
        }
        if (subCategory.contains("SUNGLASSES") || subCategory.contains("EYEWEAR")) {
            return "wear this exact accessory naturally on the face";
        }
        return "wear this exact accessory in the most appropriate natural position for its design";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static SimpleClientHttpRequestFactory createRequestFactory(
            Duration connectTimeout,
            Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
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
