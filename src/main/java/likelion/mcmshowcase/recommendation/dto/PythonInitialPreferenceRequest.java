package likelion.mcmshowcase.recommendation.dto;

import java.util.List;

public record PythonInitialPreferenceRequest(
        List<ZoneInteraction> zoneInteractions
) {
    public record ZoneInteraction(
            String zone,
            String category,
            long dwellSeconds
    ) {
    }
}
