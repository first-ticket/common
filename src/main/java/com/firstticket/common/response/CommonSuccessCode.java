package com.firstticket.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonSuccessCode implements SuccessCode {

    OK(HttpStatus.OK, "요청이 성공했습니다"),
    CREATED(HttpStatus.CREATED, "리소스가 생성되었습니다"),
    DELETED(HttpStatus.OK, "리소스가 삭제되었습니다");

    private final HttpStatus status;
    private final String message;
}
