package likelion.mcmshowcase.styleanalysis.dto;

import java.util.List;

public record TodayLookResponse(
        List<StyleAnalysisProductResponse> products
) {
}
