package likelion.mcmshowcase.recommendation.evaluation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecommendationEvaluationRequest(
        @NotEmpty List<@Valid Persona> personas
) {
    public record Persona(
            @NotBlank String personaId,
            @NotBlank String personaType,
            @NotNull List<@Valid ZoneInteraction> zoneInteractions,
            @NotNull List<@Valid ArInteraction> arInteractions,
            @NotNull List<@Valid MemberWishlist> memberWishlists,
            @NotNull @Valid GroundTruth groundTruth
    ) {
    }

    public record ZoneInteraction(
            @NotBlank String zone,
            @NotBlank String category,
            @Min(0) long dwellSeconds,
            @Min(1) int sequenceNo
    ) {
    }

    public record ArInteraction(
            @NotNull Long productId,
            @NotBlank String interactionType,
            @Min(1) int sequenceNo
    ) {
    }

    public record MemberWishlist(@NotNull Long productId) {
    }

    public record GroundTruth(
            @NotNull Long anchorProductId,
            @NotEmpty List<@Valid ExpectedRecommendation> recommendations
    ) {
    }

    public record ExpectedRecommendation(
            @NotNull Long productId,
            @Min(1) @Max(5) int relevance
    ) {
    }
}
