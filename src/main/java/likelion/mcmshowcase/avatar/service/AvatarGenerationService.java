package likelion.mcmshowcase.avatar.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.avatar.client.FluxClient;
import likelion.mcmshowcase.avatar.client.PythonImageClient;
import likelion.mcmshowcase.avatar.dto.AvatarGenerationInput;
import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarGenerationService {

    private final AvatarGenerationPersistenceService persistenceService;
    private final FluxClient fluxClient;
    private final PythonImageClient pythonImageClient;

    @Value("${flux.generated-image-directory:/app/images/generated}")
    private String generatedImageDirectory;

    public String generate(Long styleProfileId) {
        AvatarGenerationInput input = persistenceService.loadInput(styleProfileId);
        String fluxImageUrl = generateAvatarWithTiming(
                styleProfileId, input.baseAvatarUrl(), input.referenceProducts());
        String finalImageUrl = removeBackground(fluxImageUrl);
        byte[] generatedImage = fluxClient.downloadGeneratedImage(finalImageUrl);
        String relativeImageUrl = saveGeneratedImage(styleProfileId, generatedImage);

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

    private String saveGeneratedImage(Long styleProfileId, byte[] image) {
        Path directory = Path.of(generatedImageDirectory).toAbsolutePath().normalize();
        Path target = directory.resolve("avatar-" + styleProfileId + ".png").normalize();
        if (!target.getParent().equals(directory)) {
            throw new CustomException(ErrorCode.INVALID_AVATAR_STORAGE_PATH);
        }

        try {
            Files.createDirectories(directory);
            Path temporaryFile = Files.createTempFile(directory, "avatar-" + styleProfileId, ".tmp");
            try {
                Files.write(
                        temporaryFile,
                        image,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                );
                moveReplacing(temporaryFile, target);
            } finally {
                Files.deleteIfExists(temporaryFile);
            }
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.AVATAR_IMAGE_SAVE_FAILED, exception.getMessage());
        }
        return "/images/generated/avatar-" + styleProfileId + ".png";
    }

    private void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
