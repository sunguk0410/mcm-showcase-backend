package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonInitialPreferenceRequest(
        Long arSessionId,
        List<ZoneInteraction> zoneInteractions,
        List<MemberInteraction> memberInteractions
) {
    public record ZoneInteraction(
            String zone,
            String category,
            long dwellSeconds
    ) {
    }

    public record MemberInteraction(
            Long productId,
            String action
    ) {
    }
}
