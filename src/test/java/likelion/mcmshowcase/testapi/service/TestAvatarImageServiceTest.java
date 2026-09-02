package likelion.mcmshowcase.testapi.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestAvatarImageServiceTest {

    @Mock
    StyleProfileRepository styleProfileRepository;

    @InjectMocks
    TestAvatarImageService testAvatarImageService;

    @Test
    void returnsLatestStoredAvatarWithoutCreatingAnything() {
        StyleProfile styleProfile = StyleProfile.create(
                ArSession.create(LocalDateTime.now()),
                "Test style",
                "/images/generated/avatar-9.png",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(styleProfile, "id", 9L);
        when(styleProfileRepository.findLatestWithAvatarImage(Pageable.ofSize(1)))
                .thenReturn(List.of(styleProfile));

        var response = testAvatarImageService.getLatestAvatarImage();

        assertEquals(9L, response.styleProfileId());
        assertEquals("/images/generated/avatar-9.png", response.avatarImageUrl());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(styleProfileRepository).findLatestWithAvatarImage(pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void returnsNotFoundWhenNoStoredAvatarExists() {
        when(styleProfileRepository.findLatestWithAvatarImage(Pageable.ofSize(1)))
                .thenReturn(List.of());

        CustomException exception = assertThrows(
                CustomException.class,
                testAvatarImageService::getLatestAvatarImage
        );

        assertEquals(ErrorCode.AVATAR_IMAGE_NOT_FOUND, exception.getErrorCode());
    }
}
