package likelion.mcmshowcase.avatar.client;

import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FluxClientTest {

    private final FluxClient fluxClient = new FluxClient(
            RestClient.builder(),
            "https://api.bfl.ai",
            "test-key",
            "/v1/flux-2-pro-preview",
            Duration.ofSeconds(5),
            Duration.ofSeconds(30),
            Duration.ofMillis(500),
            Duration.ofMinutes(2)
    );

    @Test
    void requestBodyKeepsPromptImageNumbersAlignedWithInputImageFields() {
        List<AvatarReferenceProduct> products = List.of(
                product("https://images/top.png", "TOP", null),
                product("https://images/bottom.png", "BOTTOM", "Pants"),
                product("https://images/shoes.png", "SHOES", "Sneakers")
        );

        Map<String, Object> body = fluxClient.createRequestBody(
                "https://images/avatar.png", products);

        assertThat(body)
                .containsEntry("output_format", "png")
                .containsEntry("input_image", "https://images/avatar.png")
                .containsEntry("input_image_2", "https://images/top.png")
                .containsEntry("input_image_3", "https://images/bottom.png")
                .containsEntry("input_image_4", "https://images/shoes.png");
        assertThat((String) body.get("prompt"))
                .contains("- image 2: wear this exact product as the upper-body garment")
                .contains("- image 3: wear this exact product as the lower-body garment")
                .contains("- image 4: wear these exact shoes naturally on both feet");
    }

    @Test
    void bagInstructionsUseSubCategoryAndFallBackForUnknownValues() {
        String prompt = fluxClient.buildPrompt(List.of(
                product("backpack", "BAG", "BACKPACK"),
                product("crossbody", "BAG", "CROSSBODY_BAG"),
                product("shoulder", "BAG", "SHOULDER_BAG"),
                product("tote", "BAG", "TOTE_BAG"),
                product("clutch", "BAG", "CLUTCH"),
                product("unknown", "BAG", "HOBO")
        ));

        assertThat(prompt)
                .contains("- image 2: wear this exact backpack naturally on the back using both shoulder straps")
                .contains("- image 3: wear this exact crossbody bag diagonally across the torso")
                .contains("- image 4: carry this exact shoulder bag naturally on one shoulder")
                .contains("- image 5: carry this exact tote bag naturally in one hand or over one arm")
                .contains("- image 6: hold this exact clutch naturally in one hand")
                .contains("- image 7: carry this exact bag naturally in a way appropriate for its design");
    }

    @Test
    void accessoryInstructionsUseSubCategoryAndFallBackForUnknownValues() {
        String prompt = fluxClient.buildPrompt(List.of(
                product("cap", "ACCESSORIES", "CAP"),
                product("headwear", "ACCESSORIES", "Headwear"),
                product("belt", "ACCESSORIES", "Belt"),
                product("necklace", "ACCESSORIES", "NECKLACE"),
                product("sunglasses", "ACCESSORIES", "Sunglasses"),
                product("unknown", "ACCESSORIES", "SCARF")
        ));

        assertThat(prompt)
                .contains("- image 2: wear this exact accessory naturally on the head")
                .contains("- image 3: wear this exact accessory naturally on the head")
                .contains("- image 4: wear this exact belt naturally around the waist")
                .contains("- image 5: wear this exact accessory naturally around the neck")
                .contains("- image 6: wear this exact accessory naturally on the face")
                .contains("- image 7: wear this exact accessory in the most appropriate natural position for its design");
    }

    @Test
    void unknownCategoryUsesNaturalPositionFallback() {
        String prompt = fluxClient.buildPrompt(List.of(
                product("unknown", "OTHER", "UNKNOWN")
        ));

        assertThat(prompt).contains(
                "- image 2: incorporate this exact product naturally into the outfit "
                        + "in a position appropriate for its design");
    }

    @Test
    void promptSupportsDifferentProductCounts() {
        String oneProductPrompt = fluxClient.buildPrompt(List.of(
                product("top", "TOP", null)
        ));
        String threeProductPrompt = fluxClient.buildPrompt(List.of(
                product("top", "TOP", null),
                product("bottom", "BOTTOM", "Pants"),
                product("shoes", "SHOES", "Sneakers")
        ));

        assertThat(oneProductPrompt)
                .contains("- image 2:")
                .doesNotContain("- image 3:");
        assertThat(threeProductPrompt)
                .contains("- image 2:", "- image 3:", "- image 4:")
                .doesNotContain("- image 5:");
    }

    private AvatarReferenceProduct product(String imageUrl, String category, String subCategory) {
        return new AvatarReferenceProduct(imageUrl, category, subCategory, "Test product");
    }
}
