package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.client.PythonRecommendationClient;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationInteraction;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationRequest;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendationResponse;
import likelion.mcmshowcase.recommendation.dto.RecommendedProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int DEFAULT_TOP_K = 6;

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final PythonRecommendationClient pythonRecommendationClient;

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(Long arSessionId) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));

        List<ArInteraction> arInteractions = arInteractionRepository
                .findByArSessionOrderBySequenceNoAsc(arSession);

        if (arInteractions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ArSession has no interactions: " + arSessionId);
        }

        List<PythonRecommendationInteraction> interactions = arInteractions.stream()
                .map(interaction -> new PythonRecommendationInteraction(
                        interaction.getProduct().getId(),
                        interaction.getInteractionType().name()
                ))
                .toList();

        Product currentProduct = arInteractions.get(arInteractions.size() - 1).getProduct();

        PythonRecommendationResponse pythonResponse = pythonRecommendationClient.recommend(
                new PythonRecommendationRequest(
                        interactions,
                        currentProduct.getCategory().getCode(),
                        DEFAULT_TOP_K
                )
        );

        List<Long> recommendedProductIds = pythonResponse.recommendations().stream()
                .map(PythonRecommendationResponse.Recommendation::productId)
                .toList();
        Map<Long, Product> productsById = productRepository.findAllById(recommendedProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<RecommendedProductResponse> products = pythonResponse.recommendations().stream()
                .filter(recommendation -> productsById.containsKey(recommendation.productId()))
                .map(recommendation -> toResponse(
                        productsById.get(recommendation.productId()), recommendation.score()))
                .toList();

        return new RecommendationResponse(arSession.getId(), products);
    }

    private RecommendedProductResponse toResponse(Product product, Double score) {
        return new RecommendedProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getCategory().getCode(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductUrl(),
                score
        );
    }
}
