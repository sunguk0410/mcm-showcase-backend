package likelion.mcmshowcase.styleanalysis.dto;

import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.global.dto.PythonProductMetadata;

public record PythonStyleAnalysisInteraction(
        Long productId,
        ArInteractionType interactionType,
        Integer sequenceNo,
        PythonProductMetadata product
) {
}
