package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.dto.ArSessionCreateRequest;
import likelion.mcmshowcase.ar.dto.ArSessionCreateResponse;
import likelion.mcmshowcase.ar.dto.ArSessionEndResponse;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.visit.entity.CustomerSession;
import likelion.mcmshowcase.visit.entity.CustomerSessionStatus;
import likelion.mcmshowcase.visit.repository.CustomerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArSessionService {

    private final CustomerSessionRepository customerSessionRepository;
    private final ArSessionRepository arSessionRepository;

    @Transactional
    public ArSessionCreateResponse create(ArSessionCreateRequest request) {
        CustomerSession customerSession = customerSessionRepository.findById(request.customerSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CustomerSession not found: " + request.customerSessionId()));

        if (customerSession.getStatus() != CustomerSessionStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ArSession can only be started for an active CustomerSession");
        }

        Gender gender = resolveGender(customerSession, request.gender());
        LocalDateTime now = LocalDateTime.now();
        ArSession arSession = ArSession.create(customerSession, gender, now);
        ArSession savedArSession = arSessionRepository.save(arSession);

        return new ArSessionCreateResponse(
                savedArSession.getId(),
                customerSession.getId(),
                savedArSession.getGender(),
                savedArSession.getStartedAt()
        );
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
                arSession.getCustomerSession().getId(),
                arSession.getStartedAt(),
                arSession.getEndedAt()
        );
    }

    private Gender resolveGender(CustomerSession customerSession, Gender requestedGender) {
        Member member = customerSession.getMember();
        if (member != null) {
            if (member.getGender() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "The identified member does not have a gender");
            }
            return member.getGender();
        }

        if (requestedGender == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "gender is required for an anonymous CustomerSession");
        }
        return requestedGender;
    }
}
