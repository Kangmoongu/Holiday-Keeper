package com.holidaykeeper.config;

import com.holidaykeeper.dto.CountryDto;
import com.holidaykeeper.entity.Country;
import com.holidaykeeper.repository.CountryRepository;
import com.holidaykeeper.service.HolidayKeeperService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class HolidayKeeperInitializer implements ApplicationRunner {

    private final HolidayKeeperService  holidayKeeperService;
    private final CountryInitializer countryInitializer;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try {
            countryInitializer.initializeCountries();   // countryInitializer가 별개의 트랜젝션에서 커밋된후 종료되어야 DB에서 country를 검색해 공휴일을 저장할수있다.
            holidayKeeperService.save();
            log.info("[HolidayKeeperInitializer] initialization completed successfully");
        } catch (Exception e) {
            log.error("[HolidayKeeperInitializer] Failed to initialization: {}", e.getMessage(), e);
        }
    }
}