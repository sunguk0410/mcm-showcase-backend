package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
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

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ProductRepository productRepository;
    private final PythonRecommendationClient pythonRecommendationClient;

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(Long arSessionId) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));

        List<PythonRecommendationInteraction> interactions = arInteractionRepository
                .findByArSessionOrderBySequenceNoAsc(arSession)
                .stream()
                .map(interaction -> new PythonRecommendationInteraction(
                        interaction.getProduct() == null ? null : interaction.getProduct().getId(),
                        interaction.getInteractionType(),
                        interaction.getSequenceNo()
                ))
                .toList();

        PythonRecommendationResponse pythonResponse = pythonRecommendationClient.recommend(
                new PythonRecommendationRequest(arSession.getId(), arSession.getGender(), interactions)
        );

        List<Long> recommendedProductIds = pythonResponse.recommendedProductIds();
        Map<Long, Product> productsById = productRepository.findAllById(recommendedProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<RecommendedProductResponse> products = recommendedProductIds.stream()
                .map(productsById::get)
                .filter(product -> product != null)
                .map(this::toResponse)
                .toList();

        return new RecommendationResponse(arSession.getId(), products);
    }

    private RecommendedProductResponse toResponse(Product product) {
        return new RecommendedProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductUrl()
        );
    }
}
