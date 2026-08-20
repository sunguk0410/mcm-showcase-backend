package likelion.mcmshowcase.avatar.service;

import likelion.mcmshowcase.avatar.client.FluxClient;
import likelion.mcmshowcase.avatar.client.PythonImageClient;
import likelion.mcmshowcase.avatar.dto.AvatarReferenceProduct;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.global.url.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarGenerationService {

    private static final int MAX_REFERENCE_IMAGES = 8;

    private final StyleProfileRepository styleProfileRepository;
    private final TodayLookRepository todayLookRepository;
    private final TodayLookItemRepository todayLookItemRepository;
    private final ImageUrlResolver imageUrlResolver;
    private final FluxClient fluxClient;
    private final PythonImageClient pythonImageClient;

    @Value("${flux.generated-image-directory:/app/images/generated}")
    private String generatedImageDirectory;

    @Transactional
    public String generate(Long styleProfileId) {
        StyleProfile styleProfile = styleProfileRepository.findById(styleProfileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "StyleProfile not found: " + styleProfileId));
        validateBaseAvatar(styleProfile);

        List<TodayLookItem> items = todayLookRepository
                .findTopByStyleProfileOrderByCreatedAtDesc(styleProfile)
                .map(todayLookItemRepository::findByTodayLookOrderByDisplayOrderAsc)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "TodayLook not found for StyleProfile: " + styleProfileId));
        if (items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "TodayLook is empty for StyleProfile: " + styleProfileId
            );
        }
        if (items.size() + 1 > MAX_REFERENCE_IMAGES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "FLUX supports at most 7 TodayLook product images"
            );
        }

        String baseAvatarUrl = toPublicUrl(styleProfile.getAvatarImageUrl());
        List<AvatarReferenceProduct> referenceProducts = createReferenceProducts(items);
        String fluxImageUrl = generateAvatarWithTiming(
                styleProfileId, baseAvatarUrl, referenceProducts);
        String finalImageUrl = removeBackground(fluxImageUrl);
        byte[] generatedImage = fluxClient.downloadGeneratedImage(finalImageUrl);
        String relativeImageUrl = saveGeneratedImage(styleProfileId, generatedImage);

        styleProfile.updateAvatarImageUrl(relativeImageUrl);
        styleProfileRepository.save(styleProfile);
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

    private List<AvatarReferenceProduct> createReferenceProducts(List<TodayLookItem> items) {
        List<AvatarReferenceProduct> referenceProducts = new ArrayList<>();
        for (TodayLookItem item : items) {
            String productImageUrl = item.getProduct().getImageUrl();
            if (productImageUrl == null || productImageUrl.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product image is missing: " + item.getProduct().getId()
                );
            }
            referenceProducts.add(new AvatarReferenceProduct(
                    toPublicUrl(productImageUrl),
                    item.getProduct().getCategory().getCode(),
                    item.getProduct().getSubCategory(),
                    item.getProduct().getName()
            ));
        }
        return referenceProducts;
    }

    private String toPublicUrl(String imageUrl) {
        try {
            return imageUrlResolver.toPublicUrl(imageUrl);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private String saveGeneratedImage(Long styleProfileId, byte[] image) {
        Path directory = Path.of(generatedImageDirectory).toAbsolutePath().normalize();
        Path target = directory.resolve("avatar-" + styleProfileId + ".png").normalize();
        if (!target.getParent().equals(directory)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Invalid avatar image storage path");
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
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save generated avatar image");
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

    private void validateBaseAvatar(StyleProfile styleProfile) {
        if (styleProfile.getAvatarImageUrl() == null
                || styleProfile.getAvatarImageUrl().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Base avatar image is missing for StyleProfile: " + styleProfile.getId()
            );
        }
    }
}
