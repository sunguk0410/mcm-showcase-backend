package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.dto.ArSessionCreateResponse;
import likelion.mcmshowcase.ar.dto.ArSessionCustomerSessionRequest;
import likelion.mcmshowcase.ar.dto.ArSessionCustomerSessionResponse;
import likelion.mcmshowcase.ar.dto.ArSessionEndResponse;
import likelion.mcmshowcase.ar.dto.ArSessionGenderRequest;
import likelion.mcmshowcase.ar.dto.ArSessionGenderResponse;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.recommendation.service.RecommendationService;
import likelion.mcmshowcase.visit.entity.CustomerSession;
import likelion.mcmshowcase.visit.entity.CustomerSessionStatus;
import likelion.mcmshowcase.visit.repository.CustomerSessionRepository;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArSessionService {

    private final CustomerSessionRepository customerSessionRepository;
    private final ArSessionRepository arSessionRepository;
    private final ZoneInteractionRepository zoneInteractionRepository;
    private final RecommendationService recommendationService;

    @Transactional
    public ArSessionCreateResponse create() {
        LocalDateTime now = LocalDateTime.now();
        ArSession arSession = ArSession.create(now);
        ArSession savedArSession = arSessionRepository.save(arSession);

        return new ArSessionCreateResponse(savedArSession.getId());
    }

    @Transactional
    public ArSessionEndResponse end(Long arSessionId) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));

        if (arSession.getEndedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ArSession is already ended");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        if (endedAt.isBefore(arSession.getStartedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ArSession cannot end before its startedAt");
        }

        arSession.end(endedAt);

        return new ArSessionEndResponse(
                arSession.getId(),
                arSession.getCustomerSession() == null
                        ? null
                        : arSession.getCustomerSession().getId(),
                arSession.getStartedAt(),
                arSession.getEndedAt()
        );
    }

    @Transactional
    public ArSessionGenderResponse setGender(Long arSessionId, ArSessionGenderRequest request) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));

        arSession.setGender(request.gender());

        return new ArSessionGenderResponse(arSession.getId(), arSession.getGender());
    }

    @Transactional
    public ArSessionCustomerSessionResponse mapCustomerSession(
            Long arSessionId,
            ArSessionCustomerSessionRequest request
    ) {
        ArSession arSession = arSessionRepository.findById(arSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ArSession not found: " + arSessionId));
        CustomerSession customerSession = customerSessionRepository
                .findById(request.customerSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CustomerSession not found: " + request.customerSessionId()));

        if (customerSession.getStatus() != CustomerSessionStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only an active CustomerSession can be mapped to an ArSession"
            );
        }
        if (arSession.getCustomerSession() != null
                && !arSession.getCustomerSession().getId().equals(customerSession.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "ArSession is already mapped to another CustomerSession"
            );
        }

        arSession.mapCustomerSession(customerSession);
        ArSession savedArSession = arSessionRepository.save(arSession);

        if (zoneInteractionRepository.existsByCustomerSession(customerSession)) {
            initializePreferencesAfterCommit(savedArSession.getId());
        }

        return new ArSessionCustomerSessionResponse(
                savedArSession.getId(),
                customerSession.getId()
        );
    }

    private void initializePreferencesAfterCommit(Long arSessionId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    recommendationService.initializePreferences(arSessionId);
                } catch (RuntimeException exception) {
                    log.warn(
                            "Failed to initialize recommendation preferences for ArSession: {}",
                            arSessionId,
                            exception
                    );
                }
            }
        });
    }

}
