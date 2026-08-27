package likelion.mcmshowcase.recommendation.service;

import likelion.mcmshowcase.avatar.service.AvatarGenerationService;
import likelion.mcmshowcase.recommendation.client.PythonRecommendationClient;
import likelion.mcmshowcase.recommendation.dto.AvatarLookResponse;
import likelion.mcmshowcase.recommendation.dto.PythonAvatarLookResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationAvatarLookServiceTest {

    @Mock AvatarLookPersistenceService persistenceService;
    @Mock PythonRecommendationClient pythonRecommendationClient;
    @Mock AvatarGenerationService avatarGenerationService;
    @InjectMocks RecommendationService recommendationService;

    @Test
    void createAvatarLookIsNotTransactional() throws NoSuchMethodException {
        Method method = RecommendationService.class
                .getMethod("createAvatarLook", Long.class);

        assertFalse(method.isAnnotationPresent(Transactional.class));
    }

    @Test
    void returnsExistingGeneratedAvatarWithoutCallingExternalApis() {
        when(persistenceService.loadContext(1L)).thenReturn(
                new AvatarLookContext(
                        1L, 10L, "/images/generated/avatar-10.png", List.of()));

        AvatarLookResponse response = recommendationService.createAvatarLook(1L);

        assertEquals("/images/generated/avatar-10.png", response.avatarImageUrl());
        verify(pythonRecommendationClient, never()).createAvatarLook(any());
        verify(avatarGenerationService, never()).generate(any());
    }

    @Test
    void savesRecommendationBeforeGeneratingAvatar() {
        when(persistenceService.loadContext(1L)).thenReturn(
                new AvatarLookContext(1L, null, null, List.of()));
        when(pythonRecommendationClient.createAvatarLook(any())).thenReturn(
                new PythonAvatarLookResponse(
                        1L,
                        "Modern",
                        List.of(new PythonAvatarLookResponse.Product(3L))
                ));
        when(persistenceService.saveRecommendation(any(), any())).thenReturn(
                new PreparedAvatarLook(10L, "/images/avatars/female.png"));
        when(avatarGenerationService.generate(10L)).thenReturn(
                "/images/generated/avatar-10.png");

        AvatarLookResponse response = recommendationService.createAvatarLook(1L);

        assertEquals(10L, response.styleProfileId());
        assertEquals("/images/generated/avatar-10.png", response.avatarImageUrl());
        verify(persistenceService).saveRecommendation(any(), any());
        verify(avatarGenerationService).generate(10L);
    }
}
