package likelion.mcmshowcase.visit.controller;

import jakarta.validation.Valid;
import likelion.mcmshowcase.visit.dto.CustomerSessionCreateResponse;
import likelion.mcmshowcase.visit.dto.CustomerSessionEndResponse;
import likelion.mcmshowcase.visit.dto.CustomerSessionMemberMatchRequest;
import likelion.mcmshowcase.visit.dto.CustomerSessionMemberMatchResponse;
import likelion.mcmshowcase.visit.service.CustomerSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-sessions")
@RequiredArgsConstructor
public class CustomerSessionController {

    private final CustomerSessionService customerSessionService;

    @PostMapping
    public ResponseEntity<CustomerSessionCreateResponse> createAnonymous() {
        CustomerSessionCreateResponse response = customerSessionService.createAnonymous();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{customerSessionId}/member")
    public ResponseEntity<CustomerSessionMemberMatchResponse> matchMember(
            @PathVariable Long customerSessionId,
            @Valid @RequestBody CustomerSessionMemberMatchRequest request
    ) {
        CustomerSessionMemberMatchResponse response =
                customerSessionService.matchMember(customerSessionId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerSessionId}/end")
    public ResponseEntity<CustomerSessionEndResponse> end(
            @PathVariable Long customerSessionId
    ) {
        return ResponseEntity.ok(customerSessionService.end(customerSessionId));
    }
}
