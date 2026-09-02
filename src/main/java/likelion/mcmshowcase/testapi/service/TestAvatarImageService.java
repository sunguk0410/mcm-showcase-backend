package likelion.mcmshowcase.testapi.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.testapi.dto.LatestAvatarImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestAvatarImageService {

    private final StyleProfileRepository styleProfileRepository;

    @Transactional(readOnly = true)
    public LatestAvatarImageResponse getLatestAvatarImage() {
        StyleProfile styleProfile = styleProfileRepository
                .findLatestWithAvatarImage(PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.AVATAR_IMAGE_NOT_FOUND));

        return new LatestAvatarImageResponse(
                styleProfile.getId(),
                styleProfile.getAvatarImageUrl()
        );
    }
}
