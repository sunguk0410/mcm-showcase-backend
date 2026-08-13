package likelion.mcmshowcase.styleanalysis.client;

import likelion.mcmshowcase.styleanalysis.dto.PythonStyleAnalysisRequest;
import likelion.mcmshowcase.styleanalysis.dto.PythonStyleAnalysisResponse;
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
public class PythonStyleAnalysisClient {

    private final RestClient restClient;

    public PythonStyleAnalysisClient(
            RestClient.Builder restClientBuilder,
            @Value("${style-analysis.python.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public PythonStyleAnalysisResponse analyze(PythonStyleAnalysisRequest request) {
        try {
            PythonStyleAnalysisResponse response = restClient.post()
                    .uri("/style-analysis")
                    .body(request)
                    .retrieve()
                    .body(PythonStyleAnalysisResponse.class);

            if (response == null
                    || response.styleIdentityTitle() == null
                    || response.styleIdentityTitle().isBlank()
                    || response.avatarPresetId() == null
                    || response.todayLook() == null
                    || response.todayLook().productIds() == null
                    || response.todayLook().productIds().stream().anyMatch(id -> id == null)) {
                throw invalidResponse();
            }
            return response;
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Style analysis server is unavailable."
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
                "Style analysis server returned an invalid response."
        );
    }
}
