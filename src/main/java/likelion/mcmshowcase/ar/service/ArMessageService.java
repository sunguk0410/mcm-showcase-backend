package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.ar.dto.ArMessageEvaluateResponse;
import likelion.mcmshowcase.ar.entity.*;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArMessageHistoryRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArMessageService {
    private static final List<String> ZONES = List.of("NEW", "CLASSIC", "TRAVEL");
    private static final Set<String> CATEGORIES = Set.of("BAG", "TOP", "BOTTOM", "SHOES", "ACCESSORIES");
    private static final Map<String, List<String>> EXPANSION_PRIORITY = Map.of(
            "BAG", List.of("TOP", "SHOES", "ACCESSORIES", "BOTTOM"),
            "TOP", List.of("BOTTOM", "BAG", "ACCESSORIES", "SHOES"),
            "BOTTOM", List.of("TOP", "SHOES", "BAG", "ACCESSORIES"),
            "SHOES", List.of("BOTTOM", "BAG", "ACCESSORIES", "TOP"),
            "ACCESSORIES", List.of("BAG", "TOP", "SHOES", "BOTTOM")
    );

    private static final Map<String, List<Message>> KOREAN_MESSAGES = buildMessages();
    private static final Map<String, List<Message>> ENGLISH_MESSAGES = buildEnglishMessages();

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ArMessageHistoryRepository historyRepository;
    private final ArFittingImageService arFittingImageService;

    @Transactional
    public ArMessageEvaluateResponse evaluate(Long arSessionId) {
        return evaluate(arSessionId, Locale.KOREAN);
    }

    @Transactional
    public ArMessageEvaluateResponse evaluate(Long arSessionId, Locale locale) {
        ArSession session = arSessionRepository.findByIdForUpdate(arSessionId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.AR_SESSION_NOT_FOUND, "ArSession not found: " + arSessionId));
        List<ArInteraction> interactionHistory = arInteractionRepository
                .findByArSessionOrderBySequenceNoAsc(session);
        List<ArInteraction> fittings = arFittingImageService
                .filterInteractionsWithAvatarImage(session, interactionHistory)
                .stream()
                .filter(interaction -> interaction.getInteractionType()
                        == ArInteractionType.PRODUCT_SELECT)
                .toList();
        if (fittings.isEmpty()) return ArMessageEvaluateResponse.notTriggered();

        List<ArMessageHistory> histories = historyRepository
                .findByArSessionOrderByFittingSequenceNoAscIdAsc(session);
        Candidate candidate = evaluateFirstFitting(fittings, histories);
        if (candidate == null && !isCooldownSatisfied(fittings.size(), histories)) {
            return ArMessageEvaluateResponse.notTriggered();
        }
        if (candidate == null) candidate = evaluateZoneInterest(fittings, histories);
        if (candidate == null) candidate = evaluateCategoryExpansion(fittings, histories);
        if (candidate == null) candidate = evaluateCategorySwitch(fittings, histories);
        if (candidate == null) return ArMessageEvaluateResponse.notTriggered();

        Message selected = selectRandomMessage(candidate.messageKey(), locale);
        ArMessageHistory saved = historyRepository.save(ArMessageHistory.create(
                session, candidate.triggerType(), candidate.zone(), candidate.interestLevel(),
                candidate.targetCategory(), selected.id(), selected.text(), fittings.size(), LocalDateTime.now()));
        return new ArMessageEvaluateResponse(true, saved.getTriggerType(), saved.getZone(),
                saved.getInterestLevel(), saved.getTargetCategory(), saved.getMessageId(), saved.getMessage());
    }

    private Candidate evaluateFirstFitting(List<ArInteraction> fittings, List<ArMessageHistory> histories) {
        if (fittings.size() == 1 && histories.stream().noneMatch(h -> h.getTriggerType() == MessageTriggerType.FIRST_FITTING)) {
            return new Candidate(MessageTriggerType.FIRST_FITTING, null, null, null, "FIRST");
        }
        return null;
    }

    private Candidate evaluateZoneInterest(List<ArInteraction> fittings, List<ArMessageHistory> histories) {
        Map<String, Long> counts = fittings.stream().map(ArInteraction::getProduct)
                .filter(Objects::nonNull).collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a))
                .values().stream().map(Product::getZone).map(this::normalizeZone)
                .filter(ZONES::contains).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String currentZone = normalizeZone(last(fittings).getProduct().getZone());
        List<String> orderedZones = new ArrayList<>();
        if (ZONES.contains(currentZone)) orderedZones.add(currentZone);
        ZONES.stream().filter(z -> !z.equals(currentZone)).forEach(orderedZones::add);

        for (InterestLevel level : List.of(InterestLevel.STRONG, InterestLevel.MEANINGFUL)) {
            long threshold = level == InterestLevel.STRONG ? 3 : 2;
            for (String zone : orderedZones) {
                boolean exposed = histories.stream().anyMatch(h -> h.getTriggerType() == MessageTriggerType.ZONE_INTEREST
                        && zone.equals(h.getZone()) && level == h.getInterestLevel());
                if (counts.getOrDefault(zone, 0L) >= threshold && !exposed) {
                    return new Candidate(MessageTriggerType.ZONE_INTEREST, zone, level, null,
                            zone + (level == InterestLevel.STRONG ? "_S" : "_M"));
                }
            }
        }
        return null;
    }

    private Candidate evaluateCategoryExpansion(List<ArInteraction> fittings, List<ArMessageHistory> histories) {
        Map<Long, Product> uniqueProducts = fittings.stream().map(ArInteraction::getProduct)
                .filter(Objects::nonNull).collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        String current = category(last(fittings));
        if (!CATEGORIES.contains(current)) return null;
        Set<String> explored = uniqueProducts.values().stream().map(p -> p.getCategory().getCode())
                .filter(CATEGORIES::contains).collect(Collectors.toSet());
        long currentCount = uniqueProducts.values().stream()
                .filter(p -> current.equals(p.getCategory().getCode())).count();
        if (currentCount < 4 || explored.size() > 2 || explored.size() == CATEGORIES.size()) return null;
        String target = EXPANSION_PRIORITY.get(current).stream().filter(c -> !explored.contains(c)).findFirst().orElse(null);
        if (target == null) return null;
        boolean exposed = histories.stream().anyMatch(h -> h.getTriggerType() == MessageTriggerType.CATEGORY_EXPANSION
                && target.equals(h.getTargetCategory()));
        return exposed ? null : new Candidate(MessageTriggerType.CATEGORY_EXPANSION, null, null, target, "EXP_" + keyCategory(target));
    }

    private Candidate evaluateCategorySwitch(List<ArInteraction> fittings, List<ArMessageHistory> histories) {
        if (fittings.size() < 2) return null;
        String previous = category(fittings.get(fittings.size() - 2));
        String current = category(last(fittings));
        if (!CATEGORIES.contains(current) || current.equals(previous)) return null;
        Optional<ArMessageHistory> lastExpansion = histories.stream()
                .filter(h -> h.getTriggerType() == MessageTriggerType.CATEGORY_EXPANSION).reduce((a, b) -> b);
        if (lastExpansion.map(ArMessageHistory::getTargetCategory).filter(current::equals).isPresent()) return null;
        boolean exposed = histories.stream().anyMatch(h -> h.getTriggerType() == MessageTriggerType.CATEGORY_SWITCH
                && current.equals(h.getTargetCategory()));
        return exposed ? null : new Candidate(MessageTriggerType.CATEGORY_SWITCH, null, null, current,
                "SWITCH_" + keyCategory(current));
    }

    private boolean isCooldownSatisfied(int currentFittingSequenceNo, List<ArMessageHistory> histories) {
        return histories.isEmpty() || currentFittingSequenceNo - histories.get(histories.size() - 1).getFittingSequenceNo() >= 2;
    }

    private Message selectRandomMessage(String key, Locale locale) {
        Map<String, List<Message>> messagesByKey = locale != null && "en".equalsIgnoreCase(locale.getLanguage())
                ? ENGLISH_MESSAGES : KOREAN_MESSAGES;
        List<Message> messages = messagesByKey.get(key);
        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }

    private ArInteraction last(List<ArInteraction> fittings) { return fittings.get(fittings.size() - 1); }
    private String category(ArInteraction fitting) { return fitting.getProduct().getCategory().getCode(); }
    private String normalizeZone(String zone) { return "NEW_COLLECTION".equals(zone) ? "NEW" : zone; }
    private String keyCategory(String category) { return "ACCESSORIES".equals(category) ? "ACC" : category; }

    private record Message(String id, String text) {}
    private record Candidate(MessageTriggerType triggerType, String zone, InterestLevel interestLevel,
                             String targetCategory, String messageKey) {}

    private static Map<String, List<Message>> buildMessages() {
        Map<String, List<Message>> map = new HashMap<>();
        put(map, "FIRST", "FIRST", "오늘의 당신에게 어울리는 MCM 스타일을 만나보세요.|지금 마음이 향하는 스타일을 발견해보세요.|새로운 MCM 스타일로 오늘의 여정을 시작해보세요.|당신만의 스타일을 찾아 새로운 여정을 시작해보세요.|오늘의 무드에 어울리는 새로운 스타일을 만나보세요.|지금의 당신을 위한 새로운 스타일을 발견해보세요.|새로운 스타일 속에서 당신만의 MCM을 발견해보세요.|지금부터 당신만의 스타일 여정을 시작해보세요.|오늘의 당신을 표현할 새로운 스타일을 만나보세요.|어떤 스타일이 기다리고 있을지 지금 만나보세요.");
        put(map, "TRAVEL_M", "TRAVEL_M", "오늘은 Travel 컬렉션에 자연스럽게 시선이 머물고 있네요.|선택한 아이템을 보니 Travel 무드가 조금씩 이어지고 있어요.|지금의 선택에서 Travel 컬렉션에 대한 관심이 조금씩 보이고 있어요.|Travel 무드의 아이템이 오늘의 선택에 자주 보이기 시작했어요.|오늘의 선택이 Travel 컬렉션의 감각과 자연스럽게 이어지고 있어요.");
        put(map, "TRAVEL_S", "TRAVEL_S", "Travel 컬렉션이 오늘의 선택에서 계속 이어지고 있어요. 마음에 드는 스타일을 더 만나보세요.|지금 마음이 향하는 Travel 스타일을 조금 더 만나보세요.|Travel 아이템을 중심으로 오늘의 스타일이 조금씩 선명해지고 있어요.|오늘은 Travel 컬렉션의 무드에 유독 시선이 머물고 있네요. 이 감각을 더 이어가보세요.|반복되는 선택 속에서 Travel 무드가 눈에 띄고 있어요. 새로운 Travel 스타일도 발견해보세요.");
        put(map, "CLASSIC_M", "CLASSIC_M", "오늘은 MCM의 Classic 컬렉션에 자연스럽게 시선이 머물고 있네요.|선택한 아이템을 보니 Classic 무드가 조금씩 이어지고 있어요.|MCM의 아이코닉한 스타일이 오늘의 선택에 자주 보이기 시작했어요.|지금의 선택에는 MCM을 대표하는 Classic 무드가 자연스럽게 이어지고 있어요.|오늘의 선택이 MCM의 Classic한 감각과 조금씩 가까워지고 있어요.");
        put(map, "CLASSIC_S", "CLASSIC_S", "MCM의 Classic 컬렉션이 오늘의 선택에서 계속 이어지고 있어요.|지금 마음이 향하는 MCM Classic 스타일을 조금 더 탐색해보세요.|MCM의 시그니처 감각이 담긴 아이템에 계속 시선이 머물고 있네요.|오늘의 선택에서 MCM의 아이코닉한 무드가 점점 선명해지고 있어요.|반복되는 선택 속에서 Classic 무드가 눈에 띄고 있어요. MCM의 또 다른 시그니처 스타일도 만나보세요.");
        put(map, "NEW_M", "NEW_M", "오늘은 새로운 MCM 스타일에 자연스럽게 시선이 머물고 있네요.|New Collection의 아이템이 오늘의 선택에 자주 보이기 시작했어요.|지금의 선택에서 새로운 MCM 무드에 대한 관심이 조금씩 보이고 있어요.|선택한 아이템을 보니 New Collection의 무드가 조금씩 이어지고 있어요.|오늘의 선택이 MCM의 새로운 감각과 자연스럽게 이어지고 있어요.");
        put(map, "NEW_S", "NEW_S", "New Collection이 오늘의 선택에서 계속 이어지고 있어요. 새로운 스타일을 더 만나보세요.|지금 마음이 향하는 새로운 MCM 스타일을 조금 더 만나보세요.|New Collection을 중심으로 오늘의 스타일이 조금씩 선명해지고 있어요.|오늘은 MCM의 새로운 무드에 유독 시선이 머물고 있네요. 이 감각을 더 이어가보세요.|반복되는 선택 속에서 새로운 MCM 스타일이 눈에 띄고 있어요. 또 다른 New Collection도 발견해보세요.");
        put(map, "EXP_BAG", "EXP_BAG", "이번에는 아직 만나보지 않은 가방에서 새로운 무드를 발견해보세요.|지금의 탐색을 이어, 가방 카테고리도 함께 만나보세요.|새로운 가방으로 시선을 넓혀 또 다른 MCM 스타일을 발견해보세요.|아직 보지 않은 가방에서도 마음에 드는 스타일을 찾아보세요.|이번에는 가방에서 새로운 감각을 만나보세요.");
        put(map, "EXP_TOP", "EXP_TOP", "이번에는 아직 만나보지 않은 상의에서 새로운 무드를 발견해보세요.|지금의 탐색을 이어, 새로운 상의도 함께 만나보세요.|상의 카테고리로 시선을 넓혀 또 다른 스타일을 발견해보세요.|아직 보지 않은 상의에서도 마음에 드는 스타일을 찾아보세요.|이번에는 새로운 상의에서 오늘의 또 다른 무드를 만나보세요.");
        put(map, "EXP_BOTTOM", "EXP_BOTTOM", "이번에는 아직 만나보지 않은 하의에서 새로운 스타일을 발견해보세요.|지금의 탐색을 이어, 새로운 하의도 함께 만나보세요.|하의 카테고리까지 둘러보며 오늘의 스타일을 넓혀보세요.|아직 보지 않은 하의에서도 새로운 조합을 발견해보세요.|이번에는 새로운 하의에서 또 다른 MCM 스타일을 만나보세요.");
        put(map, "EXP_SHOES", "EXP_SHOES", "이번에는 아직 만나보지 않은 슈즈에서 새로운 감각을 발견해보세요.|지금의 탐색을 이어, 슈즈 카테고리도 함께 만나보세요.|새로운 슈즈로 시선을 넓혀 오늘의 또 다른 스타일을 발견해보세요.|아직 보지 않은 슈즈에서도 마음에 드는 스타일을 찾아보세요.|이번에는 슈즈에서 새로운 MCM 감각을 만나보세요.");
        put(map, "EXP_ACC", "EXP_ACC", "이번에는 아직 만나보지 않은 액세서리에서 새로운 디테일을 발견해보세요.|지금의 탐색을 이어, 새로운 액세서리도 함께 만나보세요.|액세서리 카테고리에서 오늘의 스타일에 새로운 디테일을 더해보세요.|아직 보지 않은 액세서리에서도 마음에 드는 디테일을 찾아보세요.|이번에는 액세서리로 시선을 넓혀 새로운 감각을 만나보세요.");
        put(map, "SWITCH_BAG", "SWITCH_BAG", "이번에는 가방에서 새로운 무드를 발견해보세요.|이제 가방으로 시선을 옮겨 새로운 스타일을 만나보세요.|이번에는 다양한 가방에서 또 다른 감각을 찾아보세요.|가방 카테고리에서 새로운 MCM 스타일을 발견해보세요.|다양한 가방에서 마음에 드는 스타일을 만나보세요.");
        put(map, "SWITCH_TOP", "SWITCH_TOP", "이번에는 상의에서 새로운 무드를 만나보세요.|이제 상의로 시선을 옮겨 또 다른 스타일을 발견해보세요.|새로운 상의에서 오늘의 또 다른 감각을 찾아보세요.|이번에는 상의 카테고리에서 새로운 MCM 스타일을 만나보세요.|상의로 탐색을 이어가며 마음에 드는 스타일을 발견해보세요.");
        put(map, "SWITCH_BOTTOM", "SWITCH_BOTTOM", "이번에는 하의에서 또 다른 스타일을 발견해보세요.|이제 하의로 시선을 옮겨 새로운 조합을 만나보세요.|새로운 하의에서 오늘의 또 다른 무드를 찾아보세요.|이번에는 하의 카테고리에서 새로운 스타일을 탐색해보세요.|하의로 탐색을 이어가며 또 다른 MCM 스타일을 만나보세요.");
        put(map, "SWITCH_SHOES", "SWITCH_SHOES", "이번에는 슈즈에서 새로운 감각을 만나보세요.|이제 슈즈로 시선을 옮겨 새로운 스타일을 발견해보세요.|새로운 슈즈에서 오늘의 또 다른 무드를 찾아보세요.|이번에는 슈즈 카테고리에서 새로운 MCM 스타일을 만나보세요.|슈즈로 탐색을 이어가며 마음에 드는 감각을 발견해보세요.");
        put(map, "SWITCH_ACC", "SWITCH_ACC", "이번에는 새로운 디테일에 시선을 옮겨보세요.|액세서리에서 오늘의 또 다른 감각을 발견해보세요.|이제 새로운 액세서리로 스타일의 디테일을 더해보세요.|이번에는 액세서리 카테고리에서 새로운 무드를 만나보세요.|작은 디테일에서도 새로운 MCM 스타일을 발견해보세요.");
        return Map.copyOf(map);
    }

    private static Map<String, List<Message>> buildEnglishMessages() {
        Map<String, List<Message>> map = new HashMap<>();
        put(map, "FIRST", "FIRST", "Discover the MCM style that feels made for you today.|Let's find the style that catches your eye right now.|Start today's journey with a fresh MCM look.|Find your signature style and begin a new journey.|Meet a new style that matches your mood today.|Discover a fresh look made for who you are right now.|Find your own MCM look among a world of new styles.|Your personal style journey starts here.|Express yourself today with a fresh new look.|See what kind of style is waiting for you today.");
        put(map, "TRAVEL_M", "TRAVEL_M", "You seem naturally drawn to the Travel collection today.|Your choices are beginning to reveal a Travel-inspired mood.|Your selections show a growing interest in the Travel collection.|The Travel mood is starting to stand out in your choices today.|Today's picks pair naturally with the spirit of the Travel collection.");
        put(map, "TRAVEL_S", "TRAVEL_S", "The Travel collection keeps appearing in your choices. Explore a few more styles you may love.|Take a closer look at more Travel styles that catch your eye.|Build today's look around the Travel pieces you keep coming back to.|The Travel mood clearly stands out in your choices today. Keep exploring that feeling.|Your repeated picks reveal a strong Travel mood. Discover another Travel style.");
        put(map, "CLASSIC_M", "CLASSIC_M", "You seem naturally drawn to MCM's Classic collection today.|Your choices are beginning to reveal a Classic mood.|MCM's iconic style is starting to stand out in your selections.|Your picks naturally reflect MCM's timeless Classic mood.|Today's choices are moving closer to MCM's signature Classic style.");
        put(map, "CLASSIC_S", "CLASSIC_S", "MCM's Classic collection keeps appearing in your choices.|Explore more MCM Classic styles that catch your eye.|MCM's signature character keeps standing out in the pieces you choose.|MCM's iconic mood is becoming clear in your selections today.|Your repeated picks reveal a Classic mood. Discover another MCM signature style.");
        put(map, "NEW_M", "NEW_M", "You seem naturally drawn to MCM's latest styles today.|New Collection pieces are beginning to stand out in your choices.|Your selections show a growing interest in MCM's newest mood.|Your choices are beginning to reveal the spirit of the New Collection.|Today's picks pair naturally with MCM's latest creative direction.");
        put(map, "NEW_S", "NEW_S", "The New Collection keeps appearing in your choices. Explore more of the latest styles.|Take a closer look at more new MCM styles that catch your eye.|Build today's look around the New Collection pieces you keep returning to.|MCM's newest mood clearly stands out in your choices today. Keep exploring.|Your repeated picks reveal a love for new MCM styles. Discover another New Collection piece.");
        put(map, "EXP_BAG", "EXP_BAG", "Discover a fresh mood among the bags you haven't explored yet.|Continue your journey with a new bag style.|Find a different MCM look by adding a new bag to your selection.|There may still be a bag style that is perfect for you.|Explore the bag collection and discover a fresh perspective.");
        put(map, "EXP_TOP", "EXP_TOP", "Discover a fresh mood among the tops you haven't explored yet.|Continue your journey with a new top.|Find a different style by exploring the tops collection.|There may still be a top that is perfect for you.|Meet a new mood for today in the tops collection.");
        put(map, "EXP_BOTTOM", "EXP_BOTTOM", "Discover a fresh style among the bottoms you haven't explored yet.|Continue your journey with a new bottom.|Complete today's look by exploring the bottoms collection.|Discover a fresh combination among the bottoms you haven't seen yet.|Meet a different MCM style in the bottoms collection.");
        put(map, "EXP_SHOES", "EXP_SHOES", "Discover a fresh perspective among the shoes you haven't explored yet.|Continue your journey with the shoes collection.|Step into a different look with a new pair of shoes.|There may still be a shoe style that is perfect for you.|Explore the shoes collection and discover a fresh MCM perspective.");
        put(map, "EXP_ACC", "EXP_ACC", "Discover a new detail among the accessories you haven't explored yet.|Continue your journey with a new accessory.|Add a fresh detail to today's look from the accessories collection.|There may still be an accessory that is perfect for you.|Complete your selection with a fresh touch from the accessories collection.");
        put(map, "SWITCH_BAG", "SWITCH_BAG", "Discover a fresh mood in the bags collection.|Now that you've moved to bags, explore a new style.|Find a different perspective among our range of bags.|Discover a fresh MCM style in the bags collection.|Explore the bags and find the style that catches your eye.");
        put(map, "SWITCH_TOP", "SWITCH_TOP", "Meet a fresh mood in the tops collection.|Now that you've moved to tops, discover a different style.|Find a new perspective for today among the latest tops.|Discover a fresh MCM look in the tops collection.|Explore the tops and find the style that catches your eye.");
        put(map, "SWITCH_BOTTOM", "SWITCH_BOTTOM", "Discover a different style in the bottoms collection.|Now that you've moved to bottoms, explore a fresh combination.|Find a new mood for today among the latest bottoms.|Explore a fresh style in the bottoms collection.|Continue your journey through a different side of MCM with the bottoms collection.");
        put(map, "SWITCH_SHOES", "SWITCH_SHOES", "Meet a fresh perspective in the shoes collection.|Now that you've moved to shoes, discover a new style.|Find a different mood for today among the latest shoes.|Discover a fresh MCM look in the shoes collection.|Explore the shoes and find the style that catches your eye.");
        put(map, "SWITCH_ACC", "SWITCH_ACC", "Add a fresh detail to your selection.|Discover a different perspective in the accessories collection.|Complete your style with a new accessory.|Meet a fresh mood in the accessories collection.|Even the smallest detail can reveal a new MCM style.");
        return Map.copyOf(map);
    }

    private static void put(Map<String, List<Message>> map, String key, String prefix, String values) {
        String[] texts = values.split("\\|");
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) messages.add(new Message(prefix + "_" + String.format("%02d", i + 1), texts[i]));
        map.put(key, List.copyOf(messages));
    }
}
