package com.holidaykeeper.config;

import com.holidaykeeper.dto.CountryDto;
import com.holidaykeeper.entity.Country;
import com.holidaykeeper.repository.CountryRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
class CountryInitializer {

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;
    private static final String COUNTRY_API_URL = "https://date.nager.at/api/v3/AvailableCountries";

    @Transactional
    public void initializeCountries() {
        log.info("[HolidayKeeperInitializer] Starting country data initialization");

        // 1. 외부 API에서 국가 목록 조회
        ResponseEntity<List<CountryDto>> response = restTemplate.exchange(
            COUNTRY_API_URL,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CountryDto>>() {}
        );

        List<CountryDto> apiCountries = response.getBody();

        if (apiCountries == null || apiCountries.isEmpty()) {
            log.warn("[HolidayKeeperInitializer] No countries fetched from API");
            return;
        }

        log.info("[HolidayKeeperInitializer] Fetched {} countries from API", apiCountries.size());

        // 2. DB에서 기존 국가 목록 조회
        List<Country> existingCountries = countryRepository.findAll();
        Map<String, Country> existingMap = existingCountries.stream()
            .collect(Collectors.toMap(Country::getCountryCode, c -> c));

        log.info("[HolidayKeeperInitializer] Found {} existing countries in DB", existingMap.size());

        // 3. 신규 국가만 필터링
        List<Country> newCountries = apiCountries.stream()
            .filter(apiCountry -> !existingMap.containsKey(apiCountry.countryCode()))
            .map(apiCountry -> new Country(apiCountry.countryCode(), apiCountry.name()))
            .collect(Collectors.toList());

        // 4. 신규 국가 저장
        if (!newCountries.isEmpty()) {
            countryRepository.saveAll(newCountries);
            log.info("[HolidayKeeperInitializer] Saved {} new countries to DB", newCountries.size());

            // 저장된 국가 로그 출력
            newCountries.forEach(country ->
                log.debug("[HolidayKeeperInitializer] Added: {} ({})",
                    country.getName(), country.getCountryCode())
            );
        } else {
            log.info("[HolidayKeeperInitializer] No new countries to save");
        }
    }
}