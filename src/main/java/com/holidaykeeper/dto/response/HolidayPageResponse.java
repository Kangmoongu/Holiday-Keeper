package com.holidaykeeper.dto.response;

import com.holidaykeeper.dto.HolidayDto;
import java.util.List;
import java.util.UUID;

public record HolidayPageResponse (
    List<HolidayDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String SortBy,
    String SortDirection
    ){
}
