package com.holidaykeeper.controller;

import com.holidaykeeper.dto.response.HolidayPageResponse;
import com.holidaykeeper.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
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
    ResponseEntity<String> saveHolidays();

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


    @Operation(
        summary = "공휴일 데이터 검색",
        description = "특정 연도 및 국가의 공휴일을 필터기반으로 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "공휴일 검색 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HolidayPageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "공휴일 검색 실패 (잘못된 요청)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<HolidayPageResponse> searchHolidays(
        @Parameter(description = "커서 기반 페이지네이션을 위한 cursor ")
        String cursor,

        @Parameter(description = "idAfter 값 (UUID)")
        UUID idAfter,

        @Parameter(description = "국가 코드", example = "KR")
        String countryCode,

        @Parameter(description = "검색 시작일 (YYYY-MM-DD)", example = "2024-01-01")
        LocalDate fromDate,

        @Parameter(description = "검색 종료일 (YYYY-MM-DD)", example = "2024-12-31")
        LocalDate toDate,

        @Parameter(description = "공휴일 타입",
            schema = @Schema(allowableValues = {"Public", "Bank", "School", "Authorities", "Optional", "Observance"}))
        String holidayType,

        @Parameter(description = "페이지 크기 (기본값: 20)", example = "20")
        Integer size,

        @Parameter(description = "정렬 기준", example = "date",
            schema = @Schema(allowableValues = {"date", "name", "countryCode"}))
        String sortBy,

        @Parameter(description = "정렬 방향", example = "ASC",
            schema = @Schema(allowableValues = {"ASC", "DESC"}))
        String sortDirection,

        @Parameter(description = "포함되어있는 단어 검색")
        String nameLike
    );

}