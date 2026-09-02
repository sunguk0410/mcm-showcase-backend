package likelion.mcmshowcase.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("COMMON_001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("COMMON_002", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON("COMMON_003", "요청 본문 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_PARAMETER("COMMON_004", "필수 요청값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER_TYPE("COMMON_005", "요청값의 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("COMMON_006", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    API_NOT_FOUND("COMMON_007", "요청한 API를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    MEMBER_NOT_FOUND("MEMBER_001", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_LOGIN_CREDENTIALS("MEMBER_002", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),

    PRODUCT_NOT_FOUND("PRODUCT_001", "상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    STYLE_PROFILE_NOT_FOUND("CLOSET_001", "스타일 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    CUSTOMER_SESSION_NOT_FOUND("VISIT_001", "고객 세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CUSTOMER_SESSION_ALREADY_ENDED("VISIT_002", "고객 세션이 이미 종료되었습니다.", HttpStatus.CONFLICT),
    INVALID_CUSTOMER_SESSION_END_TIME("VISIT_003", "고객 세션 종료 시간이 올바르지 않습니다.", HttpStatus.CONFLICT),
    INVALID_ZONE_INTERACTION_TIME("VISIT_004", "매장 구역 체류 시간이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    STORE_ZONE_NOT_FOUND("VISIT_005", "매장 구역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("VISIT_006", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ZONE_CATEGORY_NOT_FOUND("VISIT_007", "매장 구역과 카테고리의 연결 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DWELL_TIME_OUT_OF_RANGE("VISIT_008", "매장 체류 시간이 허용 범위를 초과했습니다.", HttpStatus.BAD_REQUEST),

    AR_SESSION_NOT_FOUND("AR_001", "AR 세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ACTIVE_AR_SESSION_NOT_FOUND("AR_002", "활성화된 AR 세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    AR_SESSION_ALREADY_ENDED("AR_003", "AR 세션이 이미 종료되었습니다.", HttpStatus.CONFLICT),
    INVALID_AR_SESSION_END_TIME("AR_004", "AR 세션 종료 시간이 올바르지 않습니다.", HttpStatus.CONFLICT),
    AR_SESSION_MEMBER_CONFLICT("AR_005", "AR 세션이 다른 회원과 이미 연결되어 있습니다.", HttpStatus.CONFLICT),
    AR_SESSION_CUSTOMER_CONFLICT("AR_006", "AR 세션이 다른 고객 세션과 이미 연결되어 있습니다.", HttpStatus.CONFLICT),
    INACTIVE_CUSTOMER_SESSION("AR_007", "활성 상태의 고객 세션만 AR 세션과 연결할 수 있습니다.", HttpStatus.CONFLICT),
    INTERACTION_ON_ENDED_AR_SESSION("AR_008", "종료된 AR 세션에는 상호작용을 추가할 수 없습니다.", HttpStatus.CONFLICT),
    AR_SESSION_GENDER_NOT_SET("AR_009", "AR 세션의 성별 정보가 설정되지 않았습니다.", HttpStatus.BAD_REQUEST),

    RECOMMENDATION_DATA_NOT_FOUND("RECOMMENDATION_001", "추천 초기화에 필요한 선호 데이터를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    RECOMMENDATION_EMPTY_AVATAR_LOOK("RECOMMENDATION_002", "추천 결과에 아바타 상품이 없습니다.", HttpStatus.BAD_GATEWAY),
    RECOMMENDATION_SERVER_INVALID_RESPONSE("RECOMMENDATION_003", "추천 서버가 올바르지 않은 응답을 반환했습니다.", HttpStatus.BAD_GATEWAY),
    RECOMMENDATION_SERVER_UNAVAILABLE("RECOMMENDATION_004", "추천 서버를 사용할 수 없습니다.", HttpStatus.BAD_GATEWAY),
    INVALID_RECOMMENDATION_EVALUATION_REQUEST("RECOMMENDATION_005", "추천 평가 요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    RECOMMENDATION_EVALUATION_SERVER_UNAVAILABLE("RECOMMENDATION_006", "추천 평가 서버를 사용할 수 없습니다.", HttpStatus.BAD_GATEWAY),

    TODAY_LOOK_NOT_FOUND("AVATAR_001", "오늘의 룩 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    BASE_AVATAR_IMAGE_MISSING("AVATAR_002", "기본 아바타 이미지가 없습니다.", HttpStatus.BAD_REQUEST),
    TODAY_LOOK_EMPTY("AVATAR_003", "오늘의 룩에 상품이 없습니다.", HttpStatus.BAD_REQUEST),
    TOO_MANY_AVATAR_REFERENCE_IMAGES("AVATAR_004", "아바타 생성에 사용할 상품 이미지가 너무 많습니다.", HttpStatus.BAD_REQUEST),
    PRODUCT_IMAGE_MISSING("AVATAR_005", "상품 이미지가 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PUBLIC_IMAGE_URL("AVATAR_006", "공개 이미지 URL을 생성할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_AVATAR_STORAGE_PATH("AVATAR_007", "아바타 이미지 저장 경로가 올바르지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AVATAR_IMAGE_SAVE_FAILED("AVATAR_008", "생성된 아바타 이미지를 저장하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BACKGROUND_REMOVAL_INVALID_RESPONSE("AVATAR_009", "배경 제거 서버가 올바르지 않은 응답을 반환했습니다.", HttpStatus.BAD_GATEWAY),
    BACKGROUND_REMOVAL_SERVER_UNAVAILABLE("AVATAR_010", "배경 제거 서버를 사용할 수 없습니다.", HttpStatus.BAD_GATEWAY),
    FLUX_GENERATION_TIMEOUT("AVATAR_011", "아바타 이미지 생성 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    FLUX_API_KEY_NOT_CONFIGURED("AVATAR_012", "아바타 생성 서버 설정이 올바르지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FLUX_INVALID_RESPONSE("AVATAR_013", "아바타 생성 서버가 올바르지 않은 응답을 반환했습니다.", HttpStatus.BAD_GATEWAY),
    FLUX_SERVER_UNAVAILABLE("AVATAR_014", "아바타 생성 서버를 사용할 수 없습니다.", HttpStatus.BAD_GATEWAY),
    AVATAR_IMAGE_NOT_FOUND("AVATAR_015", "아바타 이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
