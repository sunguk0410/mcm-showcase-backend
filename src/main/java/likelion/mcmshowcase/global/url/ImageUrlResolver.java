package likelion.mcmshowcase.global.url;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ImageUrlResolver {

    private final String publicBaseUrl;

    public ImageUrlResolver(@Value("${app.public-base-url:}") String publicBaseUrl) {
        this.publicBaseUrl = removeTrailingSlash(publicBaseUrl.trim());
    }

    public String toPublicUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }

        String lowerCaseUrl = imageUrl.toLowerCase(Locale.ROOT);
        if (lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://")) {
            return imageUrl;
        }
        if (publicBaseUrl.isBlank()) {
            throw new IllegalStateException("PUBLIC_BASE_URL is not configured");
        }

        String path = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return publicBaseUrl + path;
    }

    private static String removeTrailingSlash(String value) {
        int endIndex = value.length();
        while (endIndex > 0 && value.charAt(endIndex - 1) == '/') {
            endIndex--;
        }
        return value.substring(0, endIndex);
    }
}
