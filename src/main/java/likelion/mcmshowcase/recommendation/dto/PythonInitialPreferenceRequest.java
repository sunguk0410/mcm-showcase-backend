package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonInitialPreferenceRequest(
        Long arSessionId,
        List<ZoneInteraction> zoneInteractions
) {
    public record ZoneInteraction(
            String zone,
            String category,
            long dwellSeconds
    ) {
    }
}
