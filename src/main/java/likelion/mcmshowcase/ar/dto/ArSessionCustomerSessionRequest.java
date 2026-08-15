package likelion.mcmshowcase.ar.dto;

import jakarta.validation.constraints.NotNull;

public record ArSessionCustomerSessionRequest(
        @NotNull Long customerSessionId
) {
}
