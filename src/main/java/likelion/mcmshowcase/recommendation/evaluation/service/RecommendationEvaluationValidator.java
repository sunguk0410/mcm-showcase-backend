package likelion.mcmshowcase.recommendation.evaluation.service;

import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendationEvaluationValidator {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public void validate(RecommendationEvaluationRequest request) {
        Set<String> personaIds = new HashSet<>();
        request.personas().forEach(persona -> {
            if (!personaIds.add(persona.personaId())) {
                badRequest("Duplicate personaId: " + persona.personaId());
            }
            if (!Set.of("CONFIDENT", "EXPLORATORY")
                    .contains(persona.personaType().toUpperCase(Locale.ROOT))) {
                badRequest("Unsupported personaType: " + persona.personaType());
            }
            validateSequences(persona);
            validateInteractionTypes(persona);
        });

        Set<Long> productIds = request.personas().stream()
                .flatMap(persona -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.concat(
                                persona.arInteractions().stream()
                                        .map(RecommendationEvaluationRequest.ArInteraction::productId),
                                persona.memberWishlists().stream()
                                        .map(RecommendationEvaluationRequest.MemberWishlist::productId)),
                        java.util.stream.Stream.concat(
                                java.util.stream.Stream.of(persona.groundTruth().anchorProductId()),
                                persona.groundTruth().recommendations().stream()
                                        .map(RecommendationEvaluationRequest.ExpectedRecommendation::productId))))
                .collect(Collectors.toSet());

        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (products.size() != productIds.size()) {
            Set<Long> missing = new HashSet<>(productIds);
            missing.removeAll(products.keySet());
            badRequest("Products not found: " + missing);
        }

        request.personas().forEach(persona -> validateGroundTruth(persona, products));
    }

    private void validateSequences(RecommendationEvaluationRequest.Persona persona) {
        ensureUniquePositiveSequences(
                persona.personaId(),
                "zoneInteractions",
                persona.zoneInteractions().stream()
                        .map(RecommendationEvaluationRequest.ZoneInteraction::sequenceNo).toList());
        ensureUniquePositiveSequences(
                persona.personaId(),
                "arInteractions",
                persona.arInteractions().stream()
                        .map(RecommendationEvaluationRequest.ArInteraction::sequenceNo).toList());
    }

    private void ensureUniquePositiveSequences(
            String personaId,
            String field,
            List<Integer> sequences
    ) {
        if (sequences.stream().anyMatch(sequence -> sequence < 1)
                || new HashSet<>(sequences).size() != sequences.size()) {
            badRequest(personaId + " has invalid or duplicate " + field + " sequenceNo");
        }
    }

    private void validateInteractionTypes(RecommendationEvaluationRequest.Persona persona) {
        persona.arInteractions().forEach(interaction -> {
            try {
                ArInteractionType.valueOf(interaction.interactionType().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                badRequest("Unsupported interactionType: " + interaction.interactionType());
            }
        });
    }

    private void validateGroundTruth(
            RecommendationEvaluationRequest.Persona persona,
            Map<Long, Product> products
    ) {
        String category = persona.groundTruth().category();
        Set<Long> arProductIds = persona.arInteractions().stream()
                .map(RecommendationEvaluationRequest.ArInteraction::productId)
                .collect(Collectors.toSet());
        Set<Long> signalProductIds = new HashSet<>(arProductIds);
        persona.memberWishlists().stream()
                .map(RecommendationEvaluationRequest.MemberWishlist::productId)
                .forEach(signalProductIds::add);
        if (!signalProductIds.contains(persona.groundTruth().anchorProductId())) {
            badRequest(persona.personaId()
                    + " anchor must appear in AR interactions or member wishlists");
        }
        Product anchor = products.get(persona.groundTruth().anchorProductId());
        if (!anchor.getCategory().getCode().equalsIgnoreCase(category)) {
            badRequest(persona.personaId() + " anchor is not in Ground Truth category "
                    + category);
        }
        boolean categoryAppearsInAr = arProductIds.stream()
                .map(products::get)
                .anyMatch(product -> product.getCategory().getCode().equalsIgnoreCase(category));
        if (!categoryAppearsInAr) {
            badRequest(persona.personaId() + " Ground Truth category " + category
                    + " does not appear in AR interactions");
        }
        Set<Long> recommendationIds = new HashSet<>();
        persona.groundTruth().recommendations().forEach(recommendation -> {
            if (!recommendationIds.add(recommendation.productId())) {
                badRequest(persona.personaId() + " has duplicate Ground Truth products");
            }
            if (recommendation.productId().equals(persona.groundTruth().anchorProductId())) {
                badRequest(persona.personaId() + " Ground Truth recommendations contain the anchor");
            }
            Product product = products.get(recommendation.productId());
            if (!product.getCategory().getCode().equalsIgnoreCase(category)) {
                badRequest(persona.personaId() + " Ground Truth product "
                        + recommendation.productId() + " is not in category " + category);
            }
        });
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
