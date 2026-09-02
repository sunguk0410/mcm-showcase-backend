package likelion.mcmshowcase.ar.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import jakarta.validation.Valid;
import likelion.mcmshowcase.ar.dto.ArInteractionCreateRequest;
import likelion.mcmshowcase.ar.dto.ArInteractionCreateResponse;
import likelion.mcmshowcase.ar.service.ArInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ar-interactions")
@RequiredArgsConstructor
public class ArInteractionController {

    private final ArInteractionService arInteractionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ArInteractionCreateResponse>> create(
            @Valid @RequestBody ArInteractionCreateRequest request
    ) {
        ArInteractionCreateResponse response = arInteractionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
