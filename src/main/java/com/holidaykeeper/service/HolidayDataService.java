package com.holidaykeeper.service;

import com.holidaykeeper.entity.Country;
import com.holidaykeeper.entity.Holiday;
import com.holidaykeeper.exception.CountryCodeNotFoundException;
import com.holidaykeeper.repository.CountryRepository;
import com.holidaykeeper.repository.HolidayRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayDataService {

    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;

    /**
     * 실제로 DB에 공휴일을 저장하는 메서드
     * @param holidays 저장할 공휴일 리스트
     * @return 저장한 공휴일 갯수
     */
    @Transactional
    public int saveHolidays(List<Holiday> holidays) {
        holidayRepository.saveAll(holidays);
        return holidays.size();
    }

    /**
     * 특정 연도, 국가의 공휴일 레코드 전체 삭제 메서드
     * @param countryCode 삭제하고자 하는 국가 코드
     * @param year        삭제할 연도
     * @return 삭제한 레코드 갯수
     */
    @Transactional
    public void delete(String countryCode, Integer year) {
        log.info("[HolidayController] deleting holiday");
        // 삭제할 연도, 국가의 국가코드를 통해 검색하여 없을경우 예외 처리
        Country country = countryRepository.findByCountryCode(countryCode)
            .orElseThrow(CountryCodeNotFoundException::new);

        // 공휴일 테이블에서 해당하는 국가 id를 가진 공휴일리스트에서 삭제할 연도가 일치하는지 검색후 id로 매핑
        List<UUID> deleteList = holidayRepository.findAllByCountry_Id(country.getId())
            .stream()
            .filter(holiday -> holiday.getDate().getYear() == year)
            .map(Holiday::getId)
            .toList();

        holidayRepository.deleteAllById(deleteList);
        log.info("[HolidayController] CountryCode: {}, Deleted Record Size:{}",countryCode,deleteList.size());
    }
}
