package likelion.mcmshowcase.recommendation.dto;

import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.global.dto.PythonMemberContext;
import likelion.mcmshowcase.global.dto.PythonZoneInteraction;

import java.util.List;

public record PythonRecommendationRequest(
        Long arSessionId,
        Gender gender,
        List<PythonRecommendationInteraction> interactions,
        List<PythonZoneInteraction> zoneInteractions,
        PythonMemberContext memberContext
) {
}
