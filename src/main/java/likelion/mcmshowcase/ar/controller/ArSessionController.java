package likelion.mcmshowcase.ar.controller;

import jakarta.validation.Valid;
import likelion.mcmshowcase.ar.dto.ArSessionCreateResponse;
import likelion.mcmshowcase.ar.dto.ArSessionCustomerSessionRequest;
import likelion.mcmshowcase.ar.dto.ArSessionCustomerSessionResponse;
import likelion.mcmshowcase.ar.dto.ArSessionEndResponse;
import likelion.mcmshowcase.ar.dto.ArSessionGenderRequest;
import likelion.mcmshowcase.ar.dto.ArSessionGenderResponse;
import likelion.mcmshowcase.ar.dto.ArSessionMemberRequest;
import likelion.mcmshowcase.ar.dto.ArSessionMemberResponse;
import likelion.mcmshowcase.ar.dto.ArSessionMemberStatusResponse;
import likelion.mcmshowcase.ar.dto.ProductSelectHistoryResponse;
import likelion.mcmshowcase.ar.service.ArSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ar-sessions")
@RequiredArgsConstructor
public class ArSessionController {

    private final ArSessionService arSessionService;

    @GetMapping("/{arSessionId}")
    public ResponseEntity<ArSessionMemberStatusResponse> getMemberStatus(
            @PathVariable Long arSessionId
    ) {
        return ResponseEntity.ok(arSessionService.getMemberStatus(arSessionId));
    }

    @GetMapping("/{arSessionId}/product-select-history")
    public ResponseEntity<ProductSelectHistoryResponse> getProductSelectHistory(
            @PathVariable Long arSessionId
    ) {
        return ResponseEntity.ok(arSessionService.getProductSelectHistory(arSessionId));
    }

    @PostMapping
    public ResponseEntity<ArSessionCreateResponse> create() {
        ArSessionCreateResponse response = arSessionService.create();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{arSessionId}/end")
    public ResponseEntity<ArSessionEndResponse> end(@PathVariable Long arSessionId) {
        return ResponseEntity.ok(arSessionService.end(arSessionId));
    }

    @PatchMapping("/{arSessionId}/customer-session")
    public ResponseEntity<ArSessionCustomerSessionResponse> mapCustomerSession(
            @PathVariable Long arSessionId,
            @Valid @RequestBody ArSessionCustomerSessionRequest request
    ) {
        return ResponseEntity.ok(arSessionService.mapCustomerSession(arSessionId, request));
    }

    @PatchMapping("/{arSessionId}/gender")
    public ResponseEntity<ArSessionGenderResponse> setGender(
            @PathVariable Long arSessionId,
            @Valid @RequestBody ArSessionGenderRequest request
    ) {
        return ResponseEntity.ok(arSessionService.setGender(arSessionId, request));
    }

    @PatchMapping("/{arSessionId}/member")
    public ResponseEntity<ArSessionMemberResponse> mapMember(
            @PathVariable Long arSessionId,
            @Valid @RequestBody ArSessionMemberRequest request
    ) {
        return ResponseEntity.ok(arSessionService.mapMember(arSessionId, request));
    }
}
