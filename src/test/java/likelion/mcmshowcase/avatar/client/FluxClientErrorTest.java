package likelion.mcmshowcase.avatar.client;

import likelion.mcmshowcase.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluxClientErrorTest {
    @Test
    void lowCreditRateLimitReturnsDedicatedError() {
        var error = FluxClient.mapApiError(response(429,
                "{\"detail\":\"Rate limited: your credit balance is low. Top up to restore full throughput.\"}"),
                "FLUX API request failed");

        assertEquals(ErrorCode.FLUX_CREDIT_LOW, error.getErrorCode());
        assertEquals("AVATAR_016", error.getErrorCode().getCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.getErrorCode().getHttpStatus());
        assertEquals("FLUX API 크레딧 잔액이 부족하여 요청이 제한되었습니다.", error.getMessage());
    }

    @Test
    void ordinaryRateLimitKeepsExistingError() {
        assertEquals(ErrorCode.FLUX_SERVER_UNAVAILABLE,
                FluxClient.mapApiError(response(429, "{\"detail\":\"Too many requests\"}"),
                        "FLUX API request failed").getErrorCode());
    }

    @Test
    void otherStatusIsNotClassifiedAsLowCredit() {
        assertEquals(ErrorCode.FLUX_SERVER_UNAVAILABLE,
                FluxClient.mapApiError(response(500, "credit balance is low"),
                        "FLUX API request failed").getErrorCode());
    }

    private RestClientResponseException response(int status, String body) {
        return new RestClientResponseException("FLUX error", status, "Error", null,
                body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
