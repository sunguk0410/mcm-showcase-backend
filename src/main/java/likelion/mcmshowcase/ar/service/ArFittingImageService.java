package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.entity.ArInteractionType;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.product.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ArFittingImageService {

    private static final String BAG = "BAG";
    private static final String TOP = "TOP";
    private static final String BOTTOM = "BOTTOM";
    private static final String ACCESSORIES = "ACCESSORIES";
    private static final String SHOES = "SHOES";

    private final ArInteractionRepository arInteractionRepository;
    private final Path imageDirectory;

    public ArFittingImageService(
            ArInteractionRepository arInteractionRepository,
            @Value("${app.image-directory:/app/images}") String imageDirectory
    ) {
        this.arInteractionRepository = arInteractionRepository;
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    public String resolve(ArSession arSession) {
        return resolve(
                arSession,
                arInteractionRepository.findByArSessionOrderBySequenceNoAsc(arSession)
        );
    }

    public List<ArInteraction> filterInteractionsWithAvatarImage(
            ArSession arSession,
            List<ArInteraction> history
    ) {
        List<ArInteraction> result = new ArrayList<>();
        for (int index = 0; index < history.size(); index++) {
            ArInteraction interaction = history.get(index);
            if (!isSelectionChange(interaction.getInteractionType())) {
                continue;
            }
            if (resolve(arSession, history.subList(0, index + 1)) != null) {
                result.add(interaction);
            }
        }
        return List.copyOf(result);
    }

    private String resolve(ArSession arSession, List<ArInteraction> history) {
        Gender gender = arSession.getGender();
        if (gender == null) {
            return null;
        }
        Product bag = null;
        Product top = null;
        Product bottom = null;

        for (ArInteraction interaction : history) {
            Product product = interaction.getProduct();
            if (product == null || !isSelectionChange(interaction.getInteractionType())) {
                continue;
            }
            String category = categoryOf(product);
            boolean selected = interaction.getInteractionType() == ArInteractionType.PRODUCT_SELECT;
            if (BAG.equals(category)) {
                bag = updateSelection(bag, product, selected);
            } else if (TOP.equals(category)) {
                top = updateSelection(top, product, selected);
            } else if (BOTTOM.equals(category)) {
                bottom = updateSelection(bottom, product, selected);
            }
        }

        String genderDirectory = gender.name().toLowerCase(Locale.ROOT);
        ArInteraction lastSelectionChange = lastSelectionChange(history);
        if (lastSelectionChange != null
                && doesNotSupportFitting(categoryOf(lastSelectionChange.getProduct()))) {
            return null;
        }
        if (lastSelectionChange != null && BAG.equals(categoryOf(lastSelectionChange.getProduct()))) {
            return bag == null
                    ? baseAvatarPath(genderDirectory)
                    : fittingPath(genderDirectory, "bag-" + bag.getProductCode());
        }
        if (top != null && bottom != null) {
            return fittingPath(genderDirectory,
                    "top-" + top.getProductCode() + "-bottom-" + bottom.getProductCode());
        }
        if (top != null) {
            return fittingPath(genderDirectory, "top-" + top.getProductCode());
        }
        if (bottom != null) {
            return fittingPath(genderDirectory, "bottom-" + bottom.getProductCode());
        }
        return baseAvatarPath(genderDirectory);
    }

    private Product updateSelection(Product current, Product requested, boolean selected) {
        if (selected) {
            return requested;
        }
        return current != null && current.getId().equals(requested.getId()) ? null : current;
    }

    private ArInteraction lastSelectionChange(List<ArInteraction> history) {
        for (int index = history.size() - 1; index >= 0; index--) {
            ArInteraction interaction = history.get(index);
            if (interaction.getProduct() != null && isSelectionChange(interaction.getInteractionType())) {
                return interaction;
            }
        }
        return null;
    }

    private boolean isSelectionChange(ArInteractionType interactionType) {
        return interactionType == ArInteractionType.PRODUCT_SELECT
                || interactionType == ArInteractionType.PRODUCT_DESELECT;
    }

    private boolean doesNotSupportFitting(String category) {
        return ACCESSORIES.equals(category) || SHOES.equals(category);
    }

    private String categoryOf(Product product) {
        return product.getCategory().getCode().toUpperCase(Locale.ROOT);
    }

    private String fittingPath(String genderDirectory, String fileName) {
        String relativePath = "fittings/" + genderDirectory + "/" + fileName + ".png";
        Path imagePath = imageDirectory.resolve(relativePath).normalize();
        if (!imagePath.startsWith(imageDirectory) || !Files.isRegularFile(imagePath)) {
            return null;
        }
        return "/images/" + relativePath;
    }

    private String baseAvatarPath(String genderDirectory) {
        return "/images/fittings/" + genderDirectory + "/" + genderDirectory + ".png";
    }
}
