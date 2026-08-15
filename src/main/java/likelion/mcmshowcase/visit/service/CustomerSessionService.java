package likelion.mcmshowcase.visit.service;

import likelion.mcmshowcase.visit.dto.CustomerSessionCreateResponse;
import likelion.mcmshowcase.visit.dto.CustomerSessionEndResponse;
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
public class CustomerSessionService {

    private final CustomerSessionRepository customerSessionRepository;

    @Transactional
    public CustomerSessionCreateResponse createAnonymous() {
        LocalDateTime now = LocalDateTime.now();
        CustomerSession customerSession = CustomerSession.createAnonymous(now);
        CustomerSession savedCustomerSession = customerSessionRepository.save(customerSession);

        return new CustomerSessionCreateResponse(
                savedCustomerSession.getId(),
                savedCustomerSession.getStatus(),
                savedCustomerSession.getStartedAt()
        );
    }

    @Transactional
    public CustomerSessionEndResponse end(Long customerSessionId) {
        CustomerSession customerSession = customerSessionRepository.findById(customerSessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CustomerSession not found: " + customerSessionId));

        if (customerSession.getEndedAt() != null
                || customerSession.getStatus() == CustomerSessionStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "CustomerSession is already ended");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        if (endedAt.isBefore(customerSession.getStartedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "CustomerSession cannot end before its startedAt");
        }

        customerSession.end(endedAt);
        CustomerSession savedCustomerSession = customerSessionRepository.save(customerSession);

        return new CustomerSessionEndResponse(
                savedCustomerSession.getId(),
                savedCustomerSession.getStatus(),
                savedCustomerSession.getStartedAt(),
                savedCustomerSession.getEndedAt()
        );
    }
}
