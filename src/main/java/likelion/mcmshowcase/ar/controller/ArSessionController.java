package likelion.mcmshowcase.ar.controller;

import jakarta.validation.Valid;
import likelion.mcmshowcase.ar.dto.ArSessionCreateRequest;
import likelion.mcmshowcase.ar.dto.ArSessionCreateResponse;
import likelion.mcmshowcase.ar.dto.ArSessionCustomerSessionRequest;
import likelion.mcmshowcase.ar.dto.ArSessionCustomerSessionResponse;
import likelion.mcmshowcase.ar.dto.ArSessionEndResponse;
import likelion.mcmshowcase.ar.service.ArSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<ArSessionCreateResponse> create(
            @Valid @RequestBody ArSessionCreateRequest request
    ) {
        ArSessionCreateResponse response = arSessionService.create(request);
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
}
