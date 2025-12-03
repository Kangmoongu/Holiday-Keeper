package com.holidaykeeper.repository;

import com.holidaykeeper.entity.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HolidayRepositoryCustom {
    List<Holiday> findHolidaysWithCursor(
        String cursor,
        UUID idAfter,
        Integer size,
        String sortBy,
        String sortDirection,
        String countryCode,
        LocalDate fromDate,
        LocalDate toDate,
        String holidayType,
        String nameLike
        );

    Long countHolidays(String countryCode, LocalDate fromDate, LocalDate toDate, String holidayType, String nameLike);

}
