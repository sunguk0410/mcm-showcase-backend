package likelion.mcmshowcase.avatar.controller;

import likelion.mcmshowcase.avatar.dto.AvatarGenerateResponse;
import likelion.mcmshowcase.avatar.service.AvatarGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarGenerationService avatarGenerationService;

    @PostMapping("/generate/{styleProfileId}")
    public ResponseEntity<AvatarGenerateResponse> generate(
            @PathVariable Long styleProfileId
    ) {
        return ResponseEntity.ok(avatarGenerationService.generate(styleProfileId));
    }
}
