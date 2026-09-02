package likelion.mcmshowcase.global.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void customExceptionUsesErrorCodeStatusAndPublicMessage() {
        CustomException exception = new CustomException(
                ErrorCode.PRODUCT_NOT_FOUND,
                "Product not found: 99"
        );

        var response = handler.handleCustomException(exception);

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus(), response.getStatusCode());
        assertEquals(
                ErrorResponse.from(ErrorCode.PRODUCT_NOT_FOUND),
                response.getBody()
        );
    }

    @Test
    void unexpectedExceptionDoesNotExposeInternalMessage() {
        var response = handler.handleUnexpectedException(
                new IllegalStateException("database password leaked")
        );

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(), response.getStatusCode());
        assertEquals(
                ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR),
                response.getBody()
        );
    }
}
