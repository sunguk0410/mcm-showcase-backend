package likelion.mcmshowcase.styleanalysis.dto;

public record StyleAnalysisResponse(
        Long arSessionId,
        Long styleProfileId,
        String styleIdentityTitle,
        TodayLookResponse todayLook
) {
}
