package likelion.mcmshowcase.testapi.service;

import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.testapi.dto.LatestAvatarImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Avatar image not found"));

        return new LatestAvatarImageResponse(
                styleProfile.getId(),
                styleProfile.getAvatarImageUrl()
        );
    }
}
