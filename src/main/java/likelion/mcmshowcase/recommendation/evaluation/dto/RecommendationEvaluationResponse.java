package likelion.mcmshowcase.recommendation.evaluation.dto;

import java.util.List;

public record RecommendationEvaluationResponse(
        Summary summary,
        List<PersonaResult> personas
) {
    public record Summary(
            int personaCount,
            double meanRecallAt5,
            double meanNdcgAt5,
            Double confidentGroupAverageGap,
            Double exploratoryGroupAverageGap,
            Double confidentGroupAverageEntropy,
            Double exploratoryGroupAverageEntropy
    ) {
    }

    public record PersonaResult(
            String personaId,
            String personaType,
            RankingEvaluation rankingEvaluation,
            ConfidenceEvaluation confidenceEvaluation
    ) {
    }

    public record RankingEvaluation(
            double recallAt5,
            double ndcgAt5,
            List<RankedProduct> top5,
            List<GroundTruthResult> groundTruthResults
    ) {
    }

    public record ConfidenceEvaluation(
            double top1Top2Gap,
            double top5StandardDeviation,
            double normalizedEntropy
    ) {
    }

    public record RankedProduct(
            int rank,
            Long productId,
            String category,
            double score,
            int relevance
    ) {
    }

    public record GroundTruthResult(
            Long productId,
            int relevance,
            Integer overallRank,
            Double score,
            boolean includedInTop5
    ) {
    }
}
