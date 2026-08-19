package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.product.entity.Category;
import likelion.mcmshowcase.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ArFittingImageServiceTest {

    @TempDir Path imageDirectory;

    private ArInteractionRepository repository;
    private ArFittingImageService service;
    private ArSession session;

    @BeforeEach
    void setUp() {
        repository = mock(ArInteractionRepository.class);
        service = new ArFittingImageService(repository, imageDirectory.toString());
        session = ArSession.create(LocalDateTime.now());
        session.setGender(Gender.FEMALE);
    }

    @Test
    void bagUsesAStandaloneProductImage() {
        Product bag = product(10L, "BAG-CODE", "BAG");
        stubHistory(interaction(bag, ArInteractionType.PRODUCT_SELECT));
        createFittingImage("female/bag-BAG-CODE.png");

        assertEquals("/images/fittings/female/bag-BAG-CODE.png", service.resolve(session));
    }

    @Test
    void topOnlyUsesTheTopImage() {
        Product top = product(20L, "TOP-CODE", "TOP");
        stubHistory(interaction(top, ArInteractionType.PRODUCT_SELECT));
        createFittingImage("female/top-TOP-CODE.png");

        assertEquals("/images/fittings/female/top-TOP-CODE.png", service.resolve(session));
    }

    @Test
    void bottomSelectionKeepsTheLatestTop() {
        Product top = product(20L, "TOP-CODE", "TOP");
        Product bottom = product(30L, "BOTTOM-CODE", "BOTTOM");
        stubHistory(
                interaction(top, ArInteractionType.PRODUCT_SELECT),
                interaction(bottom, ArInteractionType.PRODUCT_SELECT));
        createFittingImage("female/top-TOP-CODE-bottom-BOTTOM-CODE.png");

        assertEquals(
                "/images/fittings/female/top-TOP-CODE-bottom-BOTTOM-CODE.png",
                service.resolve(session));
    }

    @Test
    void latestProductInEachClothingCategoryWins() {
        Product oldTop = product(20L, "OLD-TOP-CODE", "TOP");
        Product latestTop = product(21L, "TOP-CODE", "TOP");
        Product bottom = product(30L, "BOTTOM-CODE", "BOTTOM");
        stubHistory(
                interaction(oldTop, ArInteractionType.PRODUCT_SELECT),
                interaction(latestTop, ArInteractionType.PRODUCT_SELECT),
                interaction(bottom, ArInteractionType.PRODUCT_SELECT));
        createFittingImage("female/top-TOP-CODE-bottom-BOTTOM-CODE.png");

        assertEquals(
                "/images/fittings/female/top-TOP-CODE-bottom-BOTTOM-CODE.png",
                service.resolve(session));
    }

    @Test
    void deselectingBottomReturnsToTheSelectedTop() {
        Product top = product(20L, "TOP-CODE", "TOP");
        Product bottom = product(30L, "BOTTOM-CODE", "BOTTOM");
        stubHistory(
                interaction(top, ArInteractionType.PRODUCT_SELECT),
                interaction(bottom, ArInteractionType.PRODUCT_SELECT),
                interaction(bottom, ArInteractionType.PRODUCT_DESELECT));
        createFittingImage("female/top-TOP-CODE.png");

        assertEquals("/images/fittings/female/top-TOP-CODE.png", service.resolve(session));
    }

    @Test
    void deselectingTheOnlyProductReturnsToTheBaseAvatar() {
        Product bag = product(10L, "BAG-CODE", "BAG");
        stubHistory(
                interaction(bag, ArInteractionType.PRODUCT_SELECT),
                interaction(bag, ArInteractionType.PRODUCT_DESELECT));

        assertEquals("/images/fittings/female/female.png", service.resolve(session));
    }

    @Test
    void genderIsRequiredToChooseAnAvatar() {
        ArSession genderlessSession = ArSession.create(LocalDateTime.now());

        assertNull(service.resolve(genderlessSession));
    }

    @Test
    void shoesDoNotReturnAnAvatarImage() {
        Product shoes = product(40L, "SHOES-CODE", "SHOES");
        stubHistory(interaction(shoes, ArInteractionType.PRODUCT_SELECT));

        assertNull(service.resolve(session));
    }

    @Test
    void accessoriesDoNotReturnAnAvatarImage() {
        Product accessories = product(50L, "ACCESSORIES-CODE", "ACCESSORIES");
        stubHistory(interaction(accessories, ArInteractionType.PRODUCT_SELECT));

        assertNull(service.resolve(session));
    }

    @Test
    void filtersInteractionsThatReturnedNoAvatarImage() {
        Product shoes = product(40L, "SHOES-CODE", "SHOES");
        Product missingTop = product(50L, "MISSING-TOP", "TOP");
        Product bag = product(60L, "BAG-CODE", "BAG");
        ArInteraction shoesSelection = interaction(shoes, ArInteractionType.PRODUCT_SELECT);
        ArInteraction missingTopSelection = interaction(missingTop, ArInteractionType.PRODUCT_SELECT);
        ArInteraction bagSelection = interaction(bag, ArInteractionType.PRODUCT_SELECT);
        List<ArInteraction> history = List.of(
                shoesSelection, missingTopSelection, bagSelection);
        createFittingImage("female/bag-BAG-CODE.png");

        assertEquals(
                List.of(bagSelection),
                service.filterInteractionsWithAvatarImage(session, history));
    }

    @Test
    void missingFittingImageReturnsNull() {
        Product top = product(20L, "TOP-CODE", "TOP");
        stubHistory(interaction(top, ArInteractionType.PRODUCT_SELECT));

        assertNull(service.resolve(session));
    }

    private Product product(long id, String productCode, String categoryCode) {
        Category category = mock(Category.class);
        when(category.getCode()).thenReturn(categoryCode);
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(id);
        when(product.getProductCode()).thenReturn(productCode);
        when(product.getCategory()).thenReturn(category);
        return product;
    }

    private ArInteraction interaction(Product product, ArInteractionType interactionType) {
        ArInteraction interaction = mock(ArInteraction.class);
        when(interaction.getProduct()).thenReturn(product);
        when(interaction.getInteractionType()).thenReturn(interactionType);
        return interaction;
    }

    private void stubHistory(ArInteraction... interactions) {
        when(repository.findByArSessionOrderBySequenceNoAsc(session))
                .thenReturn(List.of(interactions));
    }

    private void createFittingImage(String relativePath) {
        Path imagePath = imageDirectory.resolve("fittings").resolve(relativePath);
        try {
            Files.createDirectories(imagePath.getParent());
            Files.createFile(imagePath);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
