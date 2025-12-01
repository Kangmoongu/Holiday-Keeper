package com.holidaykeeper.controller;

import com.holidaykeeper.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    ResponseEntity<String> save();

    @Operation(
        summary = "공휴일 데이터 삭제",
        description = "특정 연도 및 국가의 공휴일 레코드를 전체 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "삭제 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "삭제 실패 (잘못된 요청)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "삭제 실패 (데이터 없음)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<Void> deleteHolidays(
        @Parameter(description = "국가 코드", required = true, example = "KR")
        String countryCode,
        @Parameter(description = "연도 (2020~2025)", required = true, example = "2025")
        Integer year
    );
}