package likelion.mcmshowcase.global.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeTest {

    @Test
    void errorCodesAreUniqueAndFollowDomainFormat() {
        Set<String> uniqueCodes = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .collect(Collectors.toSet());

        assertEquals(ErrorCode.values().length, uniqueCodes.size());
        assertTrue(uniqueCodes.stream().allMatch(code ->
                code.matches("[A-Z]+(?:_[A-Z]+)*_\\d{3}")));
    }

    @Test
    void everyErrorCodeHasMessageAndHttpStatus() {
        Arrays.stream(ErrorCode.values()).forEach(errorCode -> {
            assertFalse(errorCode.getMessage().isBlank());
            assertTrue(errorCode.getHttpStatus().isError());
        });
    }
}
