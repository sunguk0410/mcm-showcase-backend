package likelion.mcmshowcase.global.url;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageUrlResolverTest {

    private final ImageUrlResolver resolver = new ImageUrlResolver("https://example.com/");

    @Test
    void resolvesProductAndAvatarPathsWithTheSameBaseUrl() {
        assertEquals(
                "https://example.com/images/MWPGALR01BK001.jpg",
                resolver.toPublicUrl("/images/MWPGALR01BK001.jpg")
        );
        assertEquals(
                "https://example.com/images/avatars/female.png",
                resolver.toPublicUrl("/images/avatars/female.png")
        );
    }

    @Test
    void keepsAbsoluteUrlsUnchanged() {
        assertEquals(
                "https://cdn.example.com/product.jpg",
                resolver.toPublicUrl("https://cdn.example.com/product.jpg")
        );
        assertEquals(
                "http://cdn.example.com/avatar.png",
                resolver.toPublicUrl("http://cdn.example.com/avatar.png")
        );
    }

    @Test
    void requiresPublicBaseUrlOnlyWhenResolvingRelativePath() {
        ImageUrlResolver resolverWithoutBaseUrl = new ImageUrlResolver("");

        assertThrows(
                IllegalStateException.class,
                () -> resolverWithoutBaseUrl.toPublicUrl("/images/avatar.png")
        );
    }
}
