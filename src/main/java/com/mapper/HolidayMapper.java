package com.mapper;


import com.holidaykeeper.dto.HolidayDto;
import com.holidaykeeper.entity.Holiday;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class HolidayMapper {

    @Mapping(target = "countryCode", source = "country.countryCode")
    public abstract HolidayDto toDto(Holiday holiday);

}
