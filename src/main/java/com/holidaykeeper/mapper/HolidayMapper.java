package com.holidaykeeper.mapper;


import com.holidaykeeper.dto.HolidayDto;
import com.holidaykeeper.entity.Holiday;
import java.util.List;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class HolidayMapper {

    @Mapping(target = "countryCode", source = "country.countryCode")
    @Mapping(target = "counties", expression ="java(convertToList(holiday.getCounties()))")
    @Mapping(target = "types", expression ="java(convertToList(holiday.getTypes()))")
    public abstract HolidayDto toDto(Holiday holiday);


    /**
     * DB에 List<String>을 String 으로 바꿔서 저장했던것을 다시 List<String> 으로 바꿔서 return하는 메서드
     * @return 변환된 List
     */
    protected List<String> convertToList(String str){
        return str == null || str.isBlank()
            ? List.of()
            : Stream.of(str.split(","))
            .map(String::trim)
            .toList();
    }

}
