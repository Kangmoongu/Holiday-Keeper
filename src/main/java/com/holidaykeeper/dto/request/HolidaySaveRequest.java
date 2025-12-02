package com.holidaykeeper.dto.request;

import java.time.LocalDate;
import java.util.List;

public record HolidaySaveRequest(
    LocalDate date,
    String localName,
    String name,
    String countryCode,
    boolean fixed,
    boolean global,
    List<String> counties,
    Integer launchYear,
    List<String> types

) {

}
