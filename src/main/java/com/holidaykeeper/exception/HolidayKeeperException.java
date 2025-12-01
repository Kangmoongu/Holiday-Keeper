package com.holidaykeeper.exception;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class HolidayKeeperException extends RuntimeException {

    private final LocalDateTime timestamp;
    private final ErrorCode errorCode;

    public HolidayKeeperException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
    }
}
