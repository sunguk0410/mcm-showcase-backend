package likelion.mcmshowcase.closet.dto;

import java.util.List;

public record MyClosetListResponse(
        List<MyClosetListItemResponse> items
) {
}
