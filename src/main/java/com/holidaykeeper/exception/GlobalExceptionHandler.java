package com.holidaykeeper.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_SERVER_ERROR","알수 없는 오류가 발생하였습니다.", LocalDateTime.now()));
    }

    @ExceptionHandler(HolidayKeeperException.class)
    public ResponseEntity<ErrorResponse> holidayKeeperException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_SERVER_ERROR","알수 없는 오류가 발생하였습니다.", LocalDateTime.now()));
    }


}
