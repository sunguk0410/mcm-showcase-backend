package likelion.mcmshowcase.styleanalysis.dto;

public record PythonStyleAnalysisResponse(
        String styleIdentityTitle,
        Long avatarPresetId,
        PythonTodayLookResponse todayLook
) {
}
