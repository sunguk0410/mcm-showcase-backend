package likelion.mcmshowcase.styleanalysis.controller;

import likelion.mcmshowcase.styleanalysis.dto.StyleAnalysisResponse;
import likelion.mcmshowcase.styleanalysis.service.StyleAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/style-analysis")
@RequiredArgsConstructor
public class StyleAnalysisController {

    private final StyleAnalysisService styleAnalysisService;

    @PostMapping("/ar-sessions/{arSessionId}")
    public ResponseEntity<StyleAnalysisResponse> analyze(@PathVariable Long arSessionId) {
        return ResponseEntity.ok(styleAnalysisService.analyze(arSessionId));
    }
}
