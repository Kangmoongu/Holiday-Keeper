package com.holidaykeeper.controller;

import com.holidaykeeper.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Holiday", description = "공휴일 관련 API")
public interface HolidayApi {

    @Operation(
        summary = "공휴일 데이터 초기 적재",
        description = "최근 5년(2020~2025)의 전체 국가 공휴일 데이터를 외부 API에서 수집하여 저장합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "데이터 적재 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "데이터 적재 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<String> loadHolidays();

}