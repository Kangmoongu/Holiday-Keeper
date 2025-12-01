package com.holidaykeeper.controller;


import com.holidaykeeper.dto.HolidayDto;
import com.holidaykeeper.service.HolidayKeeperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    @PostMapping("/save")
    public ResponseEntity<String> save() {
        log.info("[HolidayController] Saving holiday");
        String result = holidayKeeperService.save();
        log.info("[HolidayController] Saved holiday");
        return ResponseEntity.ok(result);
    }

}
