package likelion.mcmshowcase.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String errorCode,
        String errorMessage,
        Map<String, String> validationErrors
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            Map<String, String> validationErrors
    ) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                validationErrors
        );
    }
}
