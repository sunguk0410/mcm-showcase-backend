package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.dto.ArMessageEvaluateResponse;
import likelion.mcmshowcase.ar.entity.*;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArMessageHistoryRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    private static final Map<String, List<Message>> MESSAGES = buildMessages();

    private final ArSessionRepository arSessionRepository;
    private final ArInteractionRepository arInteractionRepository;
    private final ArMessageHistoryRepository historyRepository;

    @Transactional
    public ArMessageEvaluateResponse evaluate(Long arSessionId) {
        ArSession session = arSessionRepository.findByIdForUpdate(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ArSession not found: " + arSessionId));
        List<ArInteraction> fittings = arInteractionRepository
                .findByArSessionAndInteractionTypeOrderBySequenceNoAsc(session, ArInteractionType.FITTING);
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

        Message selected = selectRandomMessage(candidate.messageKey());
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

    private Message selectRandomMessage(String key) {
        List<Message> messages = MESSAGES.get(key);
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

    private static void put(Map<String, List<Message>> map, String key, String prefix, String values) {
        String[] texts = values.split("\\|");
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) messages.add(new Message(prefix + "_" + String.format("%02d", i + 1), texts[i]));
        map.put(key, List.copyOf(messages));
    }
}
