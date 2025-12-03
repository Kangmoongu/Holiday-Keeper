package com.holidaykeeper.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record HolidayDto(
    UUID id,
    LocalDate date,
    String localName,
    String name,
    String countryCode,
    boolean fixed,
    boolean global,
    List<String> counties,
    Integer launchYear,
    List<String> types
){
}
