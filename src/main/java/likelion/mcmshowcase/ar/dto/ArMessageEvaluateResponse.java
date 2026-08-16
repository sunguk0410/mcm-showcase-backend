package likelion.mcmshowcase.ar.dto;

import likelion.mcmshowcase.ar.entity.InterestLevel;
import likelion.mcmshowcase.ar.entity.MessageTriggerType;

public record ArMessageEvaluateResponse(
        boolean triggered,
        MessageTriggerType triggerType,
        String zone,
        InterestLevel interestLevel,
        String targetCategory,
        String messageId,
        String message
) {
    public static ArMessageEvaluateResponse notTriggered() {
        return new ArMessageEvaluateResponse(false, null, null, null, null, null, null);
    }
}
