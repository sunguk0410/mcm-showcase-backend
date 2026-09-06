package likelion.mcmshowcase.ar.dto;

import likelion.mcmshowcase.ar.entity.ArInteractionType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArInteractionCreateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesFittingAdd() throws Exception {
        ArInteractionCreateRequest request = objectMapper.readValue(
                """
                {
                  "arSessionId": 1,
                  "productId": 70,
                  "interactionType": "FITTING_ADD"
                }
                """,
                ArInteractionCreateRequest.class
        );

        assertThat(request.interactionType()).isEqualTo(ArInteractionType.FITTING_ADD);
    }

    @Test
    void deserializesFittingRemove() throws Exception {
        ArInteractionCreateRequest request = objectMapper.readValue(
                """
                {
                  "arSessionId": 1,
                  "productId": 70,
                  "interactionType": "FITTING_REMOVE"
                }
                """,
                ArInteractionCreateRequest.class
        );

        assertThat(request.interactionType()).isEqualTo(ArInteractionType.FITTING_REMOVE);
    }

    @Test
    void rejectsLegacyFittingValue() {
        assertThatThrownBy(() -> objectMapper.readValue(
                """
                {
                  "arSessionId": 1,
                  "productId": 70,
                  "interactionType": "FITTING"
                }
                """,
                ArInteractionCreateRequest.class
        )).isInstanceOf(Exception.class);
    }
}
