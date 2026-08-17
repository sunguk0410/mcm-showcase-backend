package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.dto.ArMessageEvaluateResponse;
import likelion.mcmshowcase.ar.entity.*;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArMessageHistoryRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.product.entity.Category;
import likelion.mcmshowcase.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArMessageServiceTest {
    @Mock ArSessionRepository sessionRepository;
    @Mock ArInteractionRepository interactionRepository;
    @Mock ArMessageHistoryRepository historyRepository;
    @Mock ArSession session;

    private ArMessageService service;
    private final List<ArInteraction> fittings = new ArrayList<>();
    private final List<ArMessageHistory> histories = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new ArMessageService(sessionRepository, interactionRepository, historyRepository);
        when(sessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(session));
        when(interactionRepository.findByArSessionAndInteractionTypeOrderBySequenceNoAsc(session, ArInteractionType.PRODUCT_SELECT))
                .thenAnswer(invocation -> List.copyOf(fittings));
        when(historyRepository.findByArSessionOrderByFittingSequenceNoAscIdAsc(session))
                .thenAnswer(invocation -> List.copyOf(histories));
        when(historyRepository.save(any())).thenAnswer(invocation -> {
            ArMessageHistory history = invocation.getArgument(0);
            histories.add(history);
            return history;
        });
    }

    @Test
    void firstFittingTriggersOnce() {
        add(1, "BAG", "TRAVEL");
        ArMessageEvaluateResponse first = service.evaluate(1L);
        ArMessageEvaluateResponse second = service.evaluate(1L);
        assertEquals(MessageTriggerType.FIRST_FITTING, first.triggerType());
        assertTrue(first.messageId().matches("FIRST_(0[1-9]|10)"));
        assertFalse(second.triggered());
    }

    @Test
    void englishLocaleReturnsEnglishMessage() {
        add(1, "BAG", "TRAVEL");

        ArMessageEvaluateResponse response = service.evaluate(1L, Locale.ENGLISH);

        assertEquals(MessageTriggerType.FIRST_FITTING, response.triggerType());
        assertTrue(response.message().matches("[\\x00-\\x7F]+"));
    }

    @Test
    void travelUniqueThresholdsAndDuplicateProduct() {
        add(1, "BAG", "TRAVEL");
        add(1, "BAG", "TRAVEL");
        assertFalse(service.evaluate(1L).triggered());
        add(2, "TOP", "TRAVEL");
        ArMessageEvaluateResponse meaningful = service.evaluate(1L);
        assertEquals(InterestLevel.MEANINGFUL, meaningful.interestLevel());
        add(3, "SHOES", "TRAVEL");
        assertFalse(service.evaluate(1L).triggered(), "cooldown must block the next fitting");
        add(3, "SHOES", "TRAVEL");
        ArMessageEvaluateResponse strong = service.evaluate(1L);
        assertEquals(InterestLevel.STRONG, strong.interestLevel());
        assertEquals("TRAVEL", strong.zone());
    }

    @Test
    void meaningfulAndStrongAreEachExposedAtMostOnce() {
        add(1, "BAG", "TRAVEL"); add(2, "TOP", "TRAVEL");
        assertEquals(InterestLevel.MEANINGFUL, service.evaluate(1L).interestLevel());
        add(3, "SHOES", "TRAVEL"); add(4, "BOTTOM", "TRAVEL");
        assertEquals(InterestLevel.STRONG, service.evaluate(1L).interestLevel());
        add(5, "ACCESSORIES", "TRAVEL"); add(6, "BAG", "TRAVEL");
        assertNull(service.evaluate(1L).interestLevel());
    }

    @Test
    void fourUniqueBagsExpandToTop() {
        add(1, "BAG", "OTHER"); add(2, "BAG", "OTHER");
        add(3, "BAG", "OTHER"); add(4, "BAG", "OTHER");
        ArMessageEvaluateResponse response = service.evaluate(1L);
        assertEquals(MessageTriggerType.CATEGORY_EXPANSION, response.triggerType());
        assertEquals("TOP", response.targetCategory());
        assertTrue(response.messageId().startsWith("EXP_TOP_"));
    }

    @Test
    void bagToShoesTriggersSwitch() {
        add(1, "BAG", "OTHER"); add(2, "SHOES", "OTHER");
        ArMessageEvaluateResponse response = service.evaluate(1L);
        assertEquals(MessageTriggerType.CATEGORY_SWITCH, response.triggerType());
        assertEquals("SHOES", response.targetCategory());
        assertTrue(response.messageId().startsWith("SWITCH_SHOES_"));
    }

    @Test
    void zoneHasPriorityOverSwitch() {
        add(1, "BAG", "TRAVEL"); add(2, "SHOES", "TRAVEL");
        assertEquals(MessageTriggerType.ZONE_INTEREST, service.evaluate(1L).triggerType());
    }

    @Test
    void switchToLatestExpansionTargetIsSkipped() {
        histories.add(history(MessageTriggerType.CATEGORY_EXPANSION, null, null, "TOP", 2));
        add(1, "BAG", "OTHER"); add(2, "TOP", "OTHER");
        assertFalse(service.evaluate(1L).triggered());
    }

    @Test
    void cooldownRequiresTwoFittingsAndStrongCandidatePersists() {
        histories.add(history(MessageTriggerType.ZONE_INTEREST, "TRAVEL", InterestLevel.MEANINGFUL, null, 2));
        add(1, "BAG", "TRAVEL"); add(2, "TOP", "TRAVEL"); add(3, "SHOES", "TRAVEL");
        assertFalse(service.evaluate(1L).triggered());
        add(3, "SHOES", "TRAVEL");
        ArMessageEvaluateResponse response = service.evaluate(1L);
        assertEquals(InterestLevel.STRONG, response.interestLevel());
    }

    private void add(long productId, String categoryCode, String zone) {
        Category category = mock(Category.class);
        when(category.getCode()).thenReturn(categoryCode);
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(productId);
        when(product.getCategory()).thenReturn(category);
        when(product.getZone()).thenReturn(zone);
        ArInteraction interaction = mock(ArInteraction.class);
        when(interaction.getProduct()).thenReturn(product);
        fittings.add(interaction);
    }

    private ArMessageHistory history(MessageTriggerType type, String zone, InterestLevel level,
                                     String target, int sequence) {
        ArMessageHistory history = mock(ArMessageHistory.class);
        when(history.getTriggerType()).thenReturn(type);
        when(history.getZone()).thenReturn(zone);
        when(history.getInterestLevel()).thenReturn(level);
        when(history.getTargetCategory()).thenReturn(target);
        when(history.getFittingSequenceNo()).thenReturn(sequence);
        return history;
    }
}
