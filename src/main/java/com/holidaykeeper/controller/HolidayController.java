package com.holidaykeeper.controller;


import com.holidaykeeper.dto.response.HolidayPageResponse;
import com.holidaykeeper.repository.CountryRepository;
import com.holidaykeeper.service.HolidayDataService;
import com.holidaykeeper.service.HolidayKeeperService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Slf4j
public class HolidayController implements HolidayApi{

    private final HolidayKeeperService holidayKeeperService;
    private final HolidayDataService holidayDataService;

    /**
     * 최근 5 년(2020 ~ 2025)의 공휴일을 외부 API에서 수집하여 저장하는 메서드
     * @return 저장성공시 완료 안내메시지
     */
    @Override
    @PostMapping
    public ResponseEntity<String> saveHolidays() {
        log.info("[HolidayController] Saving holiday");
        String result = holidayKeeperService.save();
        log.info("[HolidayController] Saved holiday");
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 연도·국가의 공휴일 레코드 전체 삭제
     * @return 삭제 완료 메시지
     */
    @Override
    @DeleteMapping("/{countryCode}/{year}")
    public ResponseEntity<Void> deleteHolidays(@PathVariable String countryCode, @PathVariable Integer year) {
        log.info("[HolidayController] Deleting holiday");
        holidayDataService.delete(countryCode, year);
        log.info("[HolidayController] Deleted holiday");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 연도별·국가별 필터 기반 공휴일 조회
     * @param cursor 다음 페이지의 첫번째 레코드 커서
     * @param idAfter 다음 페이지의 첫번째 레코드 id
     * @param countryCode 국가코드
     * @param fromDate 해당 날짜 부터
     * @param toDate 해당 날짜 까지 검색
     * @param holidayType 공휴일 타입
     * @param size 페이지 크기
     * @param sortBy 정렬기준
     * @param sortDirection 정렬방향
     * @return 페이징 된 응답
     */
    @Override
    public ResponseEntity<HolidayPageResponse> searchHolidays(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(required = false) String countryCode,
        @RequestParam(required = false) LocalDate fromDate,
        @RequestParam(required = false) LocalDate toDate,
        @RequestParam(required = false) String holidayType,
        @RequestParam Integer size,
        @RequestParam String sortBy,
        @RequestParam String sortDirection,
        @RequestParam(required = false) String nameLike) {
        log.info("[HolidayController] Searching holiday");
        HolidayPageResponse response = holidayDataService.search(cursor, idAfter, countryCode,
            fromDate, toDate, holidayType, size, sortBy, sortDirection,nameLike);
        log.info("[HolidayController] Searched holiday");
        return ResponseEntity.status(HttpStatus.OK)
            .body(response);
    }
}
