package likelion.mcmshowcase.retry;

import likelion.mcmshowcase.avatar.client.FluxClient;
import likelion.mcmshowcase.avatar.client.PythonImageClient;
import likelion.mcmshowcase.avatar.service.AvatarImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.recommendation.client.PythonRecommendationClient;
import likelion.mcmshowcase.recommendation.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.aop.support.AopUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.mockito.Mockito.*;

class SpringRetryIntegrationTest {
    @Configuration(proxyBeanMethods = false)
    @EnableResilientMethods
    static class RetryTestConfiguration {
    }

    AnnotationConfigApplicationContext context;
    MockRestServiceServer server;
    PythonImageClient imageClient;
    PythonRecommendationClient recommendationClient;
    FluxClient fluxClient;
    AvatarImageStorageService storage;
    @TempDir Path directory;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://python.test");
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient http = builder.build();
        Duration timeout = Duration.ofSeconds(1);
        var images = new PythonImageClient(RestClient.builder(), "https://python.test", timeout, timeout);
        var recommendations = new PythonRecommendationClient(RestClient.builder(), "https://python.test", timeout, timeout, timeout);
        var flux = new FluxClient(RestClient.builder(), "https://flux.test", "test-key", "/generate", timeout, timeout, timeout, timeout);
        var files = new AvatarImageStorageService();
        ReflectionTestUtils.setField(images, "restClient", http);
        ReflectionTestUtils.setField(recommendations, "restClient", http);
        ReflectionTestUtils.setField(recommendations, "avatarRestClient", http);
        ReflectionTestUtils.setField(flux, "restClient", http);
        ReflectionTestUtils.setField(flux, "imageDownloadClient", http);
        ReflectionTestUtils.setField(files, "generatedImageDirectory", directory.toString());
        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource("test", java.util.Map.of("flux.generated-image-directory", directory.toString())));
        context.register(RetryTestConfiguration.class);
        context.registerBean(PythonImageClient.class, () -> images);
        context.registerBean(PythonRecommendationClient.class, () -> recommendations);
        context.registerBean(FluxClient.class, () -> flux);
        context.registerBean(AvatarImageStorageService.class, () -> files);
        context.refresh();
        imageClient = context.getBean(PythonImageClient.class);
        recommendationClient = context.getBean(PythonRecommendationClient.class);
        fluxClient = context.getBean(FluxClient.class);
        storage = context.getBean(AvatarImageStorageService.class);
        assertTrue(AopUtils.isAopProxy(imageClient));
        assertTrue(AopUtils.isAopProxy(storage));
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void backgroundRemovalRetriesSameImage() {
        String endpoint = "https://python.test/images/remove-background";
        server.expect(requestTo(endpoint)).andExpect(content().json("{\"imageUrl\":\"https://image.test/original.png\"}"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo(endpoint)).andRespond(withSuccess("{\"imageUrl\":\"/transparent.png\"}", MediaType.APPLICATION_JSON));
        assertEquals("https://python.test/transparent.png", imageClient.removeBackground("https://image.test/original.png"));
        server.verify();
    }

    @Test
    void exhaustionKeepsExistingServiceError() {
        server.expect(times(3), requestTo("https://python.test/images/remove-background"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        var error = assertThrows(CustomException.class, () -> imageClient.removeBackground("https://image.test/input.png"));
        assertEquals(ErrorCode.BACKGROUND_REMOVAL_SERVER_UNAVAILABLE, error.getErrorCode());
        server.verify();
    }

    @Test
    void authenticationFailureIsNotRetried() {
        server.expect(requestTo("https://python.test/images/remove-background"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThrows(CustomException.class, () -> imageClient.removeBackground("https://image.test/input.png"));
        server.verify();
    }

    @Test
    void invalidResponseIsNotRetried() {
        server.expect(requestTo("https://python.test/images/remove-background"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        var error = assertThrows(CustomException.class, () -> imageClient.removeBackground("https://image.test/input.png"));
        assertEquals(ErrorCode.BACKGROUND_REMOVAL_INVALID_RESPONSE, error.getErrorCode());
        server.verify();
    }

    @Test
    void recommendationRetriesCommunicationFailure() {
        server.expect(requestTo("https://python.test/recommend")).andRespond(withException(new IOException("timeout")));
        server.expect(requestTo("https://python.test/recommend"))
                .andRespond(withSuccess("{\"recommendations\":[]}", MediaType.APPLICATION_JSON));
        assertTrue(recommendationClient.recommend(new PythonRecommendationRequest(1L, List.of(), "BAG", 6, null)).recommendations().isEmpty());
        server.verify();
    }

    @Test
    void avatarRecommendationRetries() {
        server.expect(requestTo("https://python.test/recommendations/avatar-look")).andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        server.expect(requestTo("https://python.test/recommendations/avatar-look"))
                .andRespond(withSuccess("{\"arSessionId\":1,\"styleIdentityTitle\":\"Look\",\"products\":[]}", MediaType.APPLICATION_JSON));
        assertEquals(1L, recommendationClient.createAvatarLook(new PythonAvatarLookRequest(1L, List.of())).arSessionId());
        server.verify();
    }

    @Test
    void preferenceInitializationRetries() {
        server.expect(requestTo("https://python.test/preferences/initialize")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo("https://python.test/preferences/initialize")).andRespond(withSuccess());
        recommendationClient.initializePreferences(new PythonInitialPreferenceRequest(1L, List.of(), List.of()));
        server.verify();
    }

    @Test
    void downloadRetriesSameUrl() {
        server.expect(requestTo("https://image.test/result.png")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        server.expect(requestTo("https://image.test/result.png")).andRespond(withSuccess(new byte[]{1, 2}, MediaType.IMAGE_PNG));
        assertArrayEquals(new byte[]{1, 2}, fluxClient.downloadGeneratedImage("https://image.test/result.png"));
        server.verify();
    }

    @Test
    void fluxSubmissionIsNeverRetried() {
        server.expect(requestTo("https://python.test/generate")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThrows(CustomException.class, () -> fluxClient.generateAvatar("https://image.test/base.png", List.of()));
        server.verify();
    }

    @Test
    void backgroundRetryDoesNotRegenerateAvatar() {
        var persistence = mock(likelion.mcmshowcase.avatar.service.AvatarGenerationPersistenceService.class);
        var generation = mock(FluxClient.class);
        when(persistence.loadInput(1L)).thenReturn(new likelion.mcmshowcase.avatar.dto.AvatarGenerationInput(
                1L, "https://image.test/base.png", List.of()));
        when(generation.generateAvatar("https://image.test/base.png", List.of()))
                .thenReturn("https://image.test/generated.png");
        when(generation.downloadGeneratedImage("https://python.test/transparent.png"))
                .thenReturn(new byte[]{1, 2});
        server.expect(times(2), requestTo("https://python.test/images/remove-background"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo("https://python.test/images/remove-background"))
                .andRespond(withSuccess("{\"imageUrl\":\"/transparent.png\"}", MediaType.APPLICATION_JSON));
        var service = new likelion.mcmshowcase.avatar.service.AvatarGenerationService(
                persistence, generation, imageClient, storage);
        assertEquals("/images/generated/avatar-1.png", service.generate(1L));
        verify(generation, org.mockito.Mockito.times(1)).generateAvatar("https://image.test/base.png", List.of());
        verify(persistence).updateAvatarImageUrl(1L, "/images/generated/avatar-1.png");
        server.verify();
    }

    @Test
    void storageRetriesWriteAndKeepsBytes() throws IOException {
        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.createDirectories(directory)).thenThrow(new IOException("temporary failure")).thenCallRealMethod();
            files.clearInvocations();
            assertEquals("/images/generated/avatar-1.png", storage.saveGeneratedImage(1L, new byte[]{3, 4}));
            assertArrayEquals(new byte[]{3, 4}, Files.readAllBytes(directory.resolve("avatar-1.png")));
            files.verify(() -> Files.createDirectories(directory), org.mockito.Mockito.times(2));
        }
    }

    @Test
    void storagePermissionErrorIsNotRetried() {
        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.createDirectories(directory)).thenThrow(new AccessDeniedException(directory.toString()));
            files.clearInvocations();
            var error = assertThrows(CustomException.class, () -> storage.saveGeneratedImage(1L, new byte[]{3, 4}));
            assertEquals(ErrorCode.AVATAR_IMAGE_SAVE_FAILED, error.getErrorCode());
            files.verify(() -> Files.createDirectories(directory), org.mockito.Mockito.times(1));
        }
    }
}
