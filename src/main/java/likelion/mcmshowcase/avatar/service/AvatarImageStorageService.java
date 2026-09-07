package likelion.mcmshowcase.avatar.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.resilience.annotation.Retryable;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Service
public class AvatarImageStorageService {
    @Value("${flux.generated-image-directory:/app/images/generated}")
    private String generatedImageDirectory;

    @Retryable(includes = IOException.class,
            excludes = {AccessDeniedException.class, NoSuchFileException.class},
            maxRetries = 2, delay = 500, multiplier = 2)
    public String saveGeneratedImage(Long styleProfileId, byte[] image) {
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
            throw new CustomException(ErrorCode.AVATAR_IMAGE_SAVE_FAILED, exception.getMessage(), exception);
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
