package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.ar.service.ArFittingImageService;
import likelion.mcmshowcase.avatar.service.AvatarGenerationService;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.product.entity.Category;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import likelion.mcmshowcase.recommendation.client.PythonRecommendationClient;
import likelion.mcmshowcase.recommendation.dto.RecommendedProductResponse;
import likelion.mcmshowcase.recommendation.dto.PythonRecommendationInteraction;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationProductMappingTest {

    @Mock ArSessionRepository arSessionRepository;
    @Mock ArInteractionRepository arInteractionRepository;
    @Mock ArFittingImageService arFittingImageService;
    @Mock ProductRepository productRepository;
    @Mock ZoneInteractionRepository zoneInteractionRepository;
    @Mock MemberWishlistRepository memberWishlistRepository;
    @Mock PythonRecommendationClient pythonRecommendationClient;
    @Mock StyleProfileRepository styleProfileRepository;
    @Mock TodayLookRepository todayLookRepository;
    @Mock TodayLookItemRepository todayLookItemRepository;
    @Mock AvatarGenerationService avatarGenerationService;
    @InjectMocks RecommendationService recommendationService;

    @Test
    void recommendationResponseIncludesKoreanAndEnglishNames() {
        Product product = mock(Product.class);
        Category category = mock(Category.class);
        when(product.getId()).thenReturn(2L);
        when(product.getProductCode()).thenReturn("MWHGAXT03CO001");
        when(product.getName()).thenReturn("Tracy 비세토스 호보");
        when(product.getNameEn()).thenReturn("Tracy Hobo in Visetos");
        when(product.getCategory()).thenReturn(category);
        when(category.getCode()).thenReturn("BAG");
        when(product.getPrice()).thenReturn(BigDecimal.valueOf(1_690_000));
        when(product.getImageUrl()).thenReturn("/images/products/2.png");
        when(product.getProductUrl()).thenReturn("https://example.com/products/2");

        RecommendedProductResponse response = ReflectionTestUtils.invokeMethod(
                recommendationService, "toResponse", product, 0.91);

        assertEquals("Tracy 비세토스 호보", response.name());
        assertEquals("Tracy Hobo in Visetos", response.nameEn());
    }

    @Test
    void pythonInteractionsExcludeMissingAvatarImagesAndProductDeselect() {
        ArSession arSession = mock(ArSession.class);
        Product selectedProduct = mock(Product.class);
        Product deselectedProduct = mock(Product.class);
        Product wishlistProduct = mock(Product.class);
        Product fittingProduct = mock(Product.class);
        ArInteraction select = interaction(selectedProduct, ArInteractionType.PRODUCT_SELECT);
        ArInteraction deselect = interaction(deselectedProduct, ArInteractionType.PRODUCT_DESELECT);
        ArInteraction wishlist = interaction(wishlistProduct, ArInteractionType.WISHLIST_ADD);
        ArInteraction fitting = interaction(fittingProduct, ArInteractionType.FITTING);
        when(selectedProduct.getId()).thenReturn(13L);
        when(arInteractionRepository.findByArSessionOrderBySequenceNoAsc(arSession))
                .thenReturn(List.of(select, deselect, wishlist, fitting));
        when(arFittingImageService.filterInteractionsWithAvatarImage(
                arSession, List.of(select, deselect, wishlist, fitting)))
                .thenReturn(List.of(select, deselect));

        List<PythonRecommendationInteraction> result = ReflectionTestUtils.invokeMethod(
                recommendationService, "getArInteractions", arSession);

        assertEquals(List.of(
                new PythonRecommendationInteraction(13L, "PRODUCT_SELECT")
        ), result);
    }

    private ArInteraction interaction(Product product, ArInteractionType type) {
        ArInteraction interaction = mock(ArInteraction.class);
        org.mockito.Mockito.lenient().when(interaction.getInteractionType()).thenReturn(type);
        if (type == ArInteractionType.PRODUCT_SELECT) {
            when(interaction.getProduct()).thenReturn(product);
        }
        return interaction;
    }
}
