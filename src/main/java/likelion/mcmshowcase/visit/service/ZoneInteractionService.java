package likelion.mcmshowcase.visit.service;

import likelion.mcmshowcase.product.entity.Category;
import likelion.mcmshowcase.product.repository.CategoryRepository;
import likelion.mcmshowcase.visit.dto.ZoneInteractionCreateRequest;
import likelion.mcmshowcase.visit.dto.ZoneInteractionCreateResponse;
import likelion.mcmshowcase.visit.entity.CustomerSession;
import likelion.mcmshowcase.visit.entity.StoreZone;
import likelion.mcmshowcase.visit.entity.ZoneCategory;
import likelion.mcmshowcase.visit.entity.ZoneInteraction;
import likelion.mcmshowcase.visit.repository.CustomerSessionRepository;
import likelion.mcmshowcase.visit.repository.StoreZoneRepository;
import likelion.mcmshowcase.visit.repository.ZoneCategoryRepository;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ZoneInteractionService {

    private final CustomerSessionRepository customerSessionRepository;
    private final StoreZoneRepository storeZoneRepository;
    private final CategoryRepository categoryRepository;
    private final ZoneCategoryRepository zoneCategoryRepository;
    private final ZoneInteractionRepository zoneInteractionRepository;

    @Transactional
    public ZoneInteractionCreateResponse create(ZoneInteractionCreateRequest request) {
        if (request.exitedAt().isBefore(request.enteredAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "exitedAt must be equal to or later than enteredAt"
            );
        }

        CustomerSession customerSession = customerSessionRepository.findById(request.customerSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CustomerSession not found: " + request.customerSessionId()));

        StoreZone storeZone = storeZoneRepository.findByFloorCode(request.floorCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StoreZone not found for floorCode: " + request.floorCode()));

        Category category = categoryRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found for code: " + request.categoryCode()));

        ZoneCategory zoneCategory = zoneCategoryRepository.findByZoneAndCategory(storeZone, category)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ZoneCategory combination not found"));

        long dwellSecondsValue = Duration.between(request.enteredAt(), request.exitedAt()).getSeconds();
        if (dwellSecondsValue > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dwellSeconds exceeds the supported range");
        }
        int dwellSeconds = (int) dwellSecondsValue;

        ZoneInteraction interaction = ZoneInteraction.create(
                customerSession,
                zoneCategory,
                request.enteredAt(),
                request.exitedAt(),
                dwellSeconds
        );
        ZoneInteraction savedInteraction = zoneInteractionRepository.save(interaction);

        return new ZoneInteractionCreateResponse(
                savedInteraction.getId(),
                customerSession.getId(),
                zoneCategory.getId(),
                storeZone.getFloorCode(),
                category.getCode(),
                savedInteraction.getEnteredAt(),
                savedInteraction.getExitedAt(),
                savedInteraction.getDwellSeconds()
        );
    }
}
