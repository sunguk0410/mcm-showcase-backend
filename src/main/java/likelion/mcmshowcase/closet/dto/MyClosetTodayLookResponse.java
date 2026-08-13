package likelion.mcmshowcase.closet.dto;

import java.util.List;

public record MyClosetTodayLookResponse(
        List<MyClosetProductResponse> products
) {
}
