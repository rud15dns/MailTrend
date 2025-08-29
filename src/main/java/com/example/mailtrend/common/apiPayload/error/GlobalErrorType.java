package com.example.mailtrend.common.apiPayload.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorType implements ErrorType {

    E500(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 내부 오류입니다."),
    MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다"), // "존재하지 않는 사용자" 401 Unauthorized
    ID_PW_ERROR(HttpStatus.UNAUTHORIZED, "아이디와 패스워드가 부정확합니다."),

    CARD_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "명함 생성에 실패했습니다."),

    NOT_ALLOWED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "카드를 찾을 수 없습니다."),
    MEMBER_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 삭제에서 오류가 생겼습니다"),
    ;

    private final HttpStatus status;

    private final String message;
}
