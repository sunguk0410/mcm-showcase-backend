package likelion.mcmshowcase.recommendation.evaluation.service;

import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.evaluation.dto.RecommendationEvaluationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationEvaluationValidatorTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    RecommendationEvaluationValidator validator;

    @Test
    void acceptsGroundTruthProductsAcrossDifferentCategories() {
        RecommendationEvaluationRequest request = new RecommendationEvaluationRequest(List.of(
                new RecommendationEvaluationRequest.Persona(
                        "P3",
                        "EXPLORATORY",
                        List.of(new RecommendationEvaluationRequest.ZoneInteraction(
                                "CLASSIC", "BAG", 320, 1)),
                        List.of(new RecommendationEvaluationRequest.ArInteraction(
                                47L, "PRODUCT_SELECT", 1)),
                        List.of(),
                        new RecommendationEvaluationRequest.GroundTruth(
                                47L,
                                List.of(
                                        new RecommendationEvaluationRequest.ExpectedRecommendation(34L, 5),
                                        new RecommendationEvaluationRequest.ExpectedRecommendation(77L, 4),
                                        new RecommendationEvaluationRequest.ExpectedRecommendation(95L, 3),
                                        new RecommendationEvaluationRequest.ExpectedRecommendation(108L, 2)
                                )
                        )
                )
        ));
        List<Product> products = List.of(
                product(34L), product(47L), product(77L), product(95L), product(108L));
        when(productRepository.findAllById(any())).thenReturn(products);

        assertDoesNotThrow(() -> validator.validate(request));
    }

    private Product product(Long id) {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(id);
        return product;
    }
}
