package com.holidaykeeper.config;

import com.holidaykeeper.service.HolidayKeeperService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final HolidayKeeperService holidayKeeperService;

    @Scheduled(cron = "0 0 1 2 1 *", zone = "Asia/Seoul")
    public void refreshLastTwoYears() {
        int thisYear = LocalDateTime.now().getYear();
        int lastYear = thisYear - 1;

        log.info("[BatchScheduler] Running auto-refresh for {}, {}", lastYear, thisYear);

        holidayKeeperService.refreshYearForAllCountries(lastYear);
        holidayKeeperService.refreshYearForAllCountries(thisYear);

        log.info("[BatchScheduler] Refresh finished.");
    }

}
