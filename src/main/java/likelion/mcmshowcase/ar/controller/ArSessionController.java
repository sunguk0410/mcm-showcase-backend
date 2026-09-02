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
import likelion.mcmshowcase.ar.dto.ArMessageEvaluateResponse;
import likelion.mcmshowcase.ar.dto.LatestActiveArSessionResponse;
import likelion.mcmshowcase.ar.service.ArMessageService;
import likelion.mcmshowcase.ar.service.ArSessionService;
import likelion.mcmshowcase.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Locale;

@RestController
@RequestMapping("/api/ar-sessions")
@RequiredArgsConstructor
public class ArSessionController {

    private final ArSessionService arSessionService;
    private final ArMessageService arMessageService;

    @GetMapping("/active/latest")
    public ResponseEntity<ApiResponse<LatestActiveArSessionResponse>> getLatestActiveUnlinkedSession() {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.getLatestActiveUnlinkedSession()));
    }

    @Operation(
            summary = "개인화 AR Message Trigger 평가",
            description = """
                    현재 AR Session의 FITTING 행동을 분석하여 개인화 AR Message Trigger를 평가합니다.

                    Priority:
                    FIRST_FITTING > ZONE_INTEREST > CATEGORY_EXPANSION > CATEGORY_SWITCH
                    """
    )
    @PostMapping("/{arSessionId}/messages/evaluate")
    public ResponseEntity<ApiResponse<ArMessageEvaluateResponse>> evaluateMessage(
            @PathVariable Long arSessionId,
            Locale locale
    ) {
        return ResponseEntity.ok(ApiResponse.success(arMessageService.evaluate(arSessionId, locale)));
    }

    @GetMapping("/{arSessionId}")
    public ResponseEntity<ApiResponse<ArSessionMemberStatusResponse>> getMemberStatus(
            @PathVariable Long arSessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.getMemberStatus(arSessionId)));
    }

    @GetMapping("/{arSessionId}/product-select-history")
    public ResponseEntity<ApiResponse<ProductSelectHistoryResponse>> getProductSelectHistory(
            @PathVariable Long arSessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.getProductSelectHistory(arSessionId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArSessionCreateResponse>> create() {
        ArSessionCreateResponse response = arSessionService.create();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PatchMapping("/{arSessionId}/end")
    public ResponseEntity<ApiResponse<ArSessionEndResponse>> end(@PathVariable Long arSessionId) {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.end(arSessionId)));
    }

    @PatchMapping("/{arSessionId}/customer-session")
    public ResponseEntity<ApiResponse<ArSessionCustomerSessionResponse>> mapCustomerSession(
            @PathVariable Long arSessionId,
            @Valid @RequestBody ArSessionCustomerSessionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.mapCustomerSession(arSessionId, request)));
    }

    @PatchMapping("/{arSessionId}/gender")
    public ResponseEntity<ApiResponse<ArSessionGenderResponse>> setGender(
            @PathVariable Long arSessionId,
            @Valid @RequestBody ArSessionGenderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.setGender(arSessionId, request)));
    }

    @PatchMapping("/{arSessionId}/member")
    public ResponseEntity<ApiResponse<ArSessionMemberResponse>> mapMember(
            @PathVariable Long arSessionId,
            @Valid @RequestBody ArSessionMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(arSessionService.mapMember(arSessionId, request)));
    }
}
