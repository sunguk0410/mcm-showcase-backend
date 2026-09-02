package likelion.mcmshowcase.visit.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import jakarta.validation.Valid;
import likelion.mcmshowcase.visit.dto.ZoneInteractionCreateRequest;
import likelion.mcmshowcase.visit.dto.ZoneInteractionCreateResponse;
import likelion.mcmshowcase.visit.service.ZoneInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zone-interactions")
@RequiredArgsConstructor
public class ZoneInteractionController {

    private final ZoneInteractionService zoneInteractionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ZoneInteractionCreateResponse>> create(
            @Valid @RequestBody ZoneInteractionCreateRequest request
    ) {
        ZoneInteractionCreateResponse response = zoneInteractionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
