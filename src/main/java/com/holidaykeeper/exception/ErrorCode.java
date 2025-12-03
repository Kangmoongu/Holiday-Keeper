package com.holidaykeeper.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // CountryCode 관련 에러코드
    COUNTRY_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지않는 국가코드입니다.");



    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
