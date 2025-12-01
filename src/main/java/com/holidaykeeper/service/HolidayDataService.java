package com.holidaykeeper.service;

import com.holidaykeeper.entity.Holiday;
import com.holidaykeeper.repository.HolidayRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayDataService {

    private final HolidayRepository holidayRepository;

    @Transactional
    public int saveHolidays(List<Holiday> holidays) {
        holidayRepository.saveAll(holidays);
        return holidays.size();
    }
}
