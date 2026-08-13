package likelion.mcmshowcase.styleanalysis.dto;

import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.global.dto.PythonMemberContext;
import likelion.mcmshowcase.global.dto.PythonZoneInteraction;

import java.util.List;

public record PythonStyleAnalysisRequest(
        Long arSessionId,
        Gender gender,
        List<PythonStyleAnalysisInteraction> interactions,
        List<PythonZoneInteraction> zoneInteractions,
        PythonMemberContext memberContext
) {
}
