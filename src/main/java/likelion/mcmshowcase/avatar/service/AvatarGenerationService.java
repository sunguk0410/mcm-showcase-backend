package likelion.mcmshowcase.avatar.service;

import likelion.mcmshowcase.avatar.client.FluxClient;
import likelion.mcmshowcase.avatar.client.PythonImageClient;
import likelion.mcmshowcase.avatar.dto.AvatarGenerationInput;
import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarGenerationService {

    private final AvatarGenerationPersistenceService persistenceService;
    private final FluxClient fluxClient;
    private final PythonImageClient pythonImageClient;

    private final AvatarImageStorageService imageStorageService;

    public String generate(Long styleProfileId) {
        AvatarGenerationInput input = persistenceService.loadInput(styleProfileId);
        String fluxImageUrl = generateAvatarWithTiming(
                styleProfileId, input.baseAvatarUrl(), input.referenceProducts());
        String finalImageUrl = removeBackground(fluxImageUrl);
        byte[] generatedImage = fluxClient.downloadGeneratedImage(finalImageUrl);
        String relativeImageUrl = imageStorageService.saveGeneratedImage(styleProfileId, generatedImage);

        persistenceService.updateAvatarImageUrl(styleProfileId, relativeImageUrl);
        return relativeImageUrl;
    }

    private String generateAvatarWithTiming(
            Long styleProfileId,
            String baseAvatarUrl,
            List<AvatarReferenceProduct> referenceProducts
    ) {
        long startedAt = System.nanoTime();
        try {
            String fluxImageUrl = fluxClient.generateAvatar(baseAvatarUrl, referenceProducts);
            log.info(
                    "FLUX avatar generation completed. styleProfileId={}, elapsedMs={}",
                    styleProfileId,
                    elapsedMillis(startedAt)
            );
            return fluxImageUrl;
        } catch (RuntimeException exception) {
            log.warn(
                    "FLUX avatar generation failed. styleProfileId={}, elapsedMs={}",
                    styleProfileId,
                    elapsedMillis(startedAt)
            );
            throw exception;
        }
    }

    private String removeBackground(String fluxImageUrl) {
        long startedAt = System.nanoTime();
        try {
            log.info("Avatar background removal started. imageUrl={}", fluxImageUrl);
            String transparentImageUrl = pythonImageClient.removeBackground(fluxImageUrl);
            log.info(
                    "Avatar background removal completed. imageUrl={}, elapsedMs={}",
                    transparentImageUrl,
                    elapsedMillis(startedAt)
            );
            return transparentImageUrl;
        } catch (RuntimeException exception) {
            log.error(
                    "Avatar background removal failed. imageUrl={}, elapsedMs={}",
                    fluxImageUrl,
                    elapsedMillis(startedAt),
                    exception
            );
            throw exception;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

}
