package com.holidaykeeper.controller;


import com.holidaykeeper.service.HolidayKeeperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Slf4j
public class HolidayController implements HolidayApi{

    private final HolidayKeeperService holidayKeeperService;

    /**
     * 최근 5 년(2020 ~ 2025)의 공휴일을 외부 API에서 수집하여 저장하는 메서드
     * @return 저장성공시 완료 안내메시지
     */
    @Override
    @PostMapping
    public ResponseEntity<String> save() {
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
    public ResponseEntity<String> deleteHolidays(@PathVariable String countryCode, @PathVariable Integer year) {
        log.info("[HolidayController] Deleting holiday");
        holidayKeeperService.delete(countryCode,year);
        log.info("[HolidayController] Deleted holiday");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
