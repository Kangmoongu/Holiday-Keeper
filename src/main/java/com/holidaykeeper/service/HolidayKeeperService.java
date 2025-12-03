package com.holidaykeeper.service;

import static java.time.Duration.between;

import com.holidaykeeper.dto.request.HolidaySaveRequest;
import com.holidaykeeper.entity.Country;
import com.holidaykeeper.entity.Holiday;
import com.holidaykeeper.exception.CountryCodeNotFoundException;
import com.holidaykeeper.repository.CountryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class HolidayKeeperService {

    private final CountryRepository countryRepository;
    private final HolidayDataService holidayDataService;
    private final WebClient webClient;

    // WebClient → 국가 * 년도 데이터 호출 시 동시 처리 수
    private static final int CONCURRENCY = 10;

    /**
     * 최근 5년간의 모든 국가 공휴일 데이터를 조회 및 DB 저장.
     * - 국가 목록 조회
     * - WebClient 비동기 호출로 외부 API 요청
     * - 받은 데이터를 blocking DB 저장용 스레드로 넘김
     * - 전체 작업 종료까지 blockLast()로 대기
     * @return 저장 완료 및 통계 메세지
     */
    public String save() {

        // 호출 시점의 년도를 기준으로 계산 (정적 필드 고정 문제 방지)
        int startYear = LocalDateTime.now().getYear() - 5;
        int endYear = LocalDateTime.now().getYear();

        LocalDateTime startTime = LocalDateTime.now();
        log.info("[HolidayKeeperService] Loading holidays {}~{}", startYear, endYear);

        // 처리 대상 국가 목록 조회
        List<Country> countries = countryRepository.findAll();

        if (countries.isEmpty()) {
            return "국가 정보가 없습니다. 재시작이 필요합니다.";
        }

        // 저장/성공/실패 카운터
        AtomicInteger totalSaved = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 연도 리스트 생성
        List<Integer> years = IntStream.rangeClosed(startYear, endYear)
            .boxed()
            .toList();

        /**
         * Flux 구조
         *  - 국가 단위 병렬(flatMap)
         *  - 각 국가 내부에서 연도 단위 병렬(flatMap)
         *  - WebClient로 요청 → DB 저장 (blocking) → boundedElastic 전환
         */
        Flux.fromIterable(countries)
            .flatMap(country ->
                    Flux.fromIterable(years)
                        // 국가 × 연도 조합을 병렬 처리
                        .flatMap(year ->
                                fetchAndSave(country, year, totalSaved, successCount, failCount),
                            CONCURRENCY),
                CONCURRENCY)
            // DB 저장이 blocking이므로 elastic에서 수행
            .subscribeOn(Schedulers.boundedElastic())
            // 모든 처리 완료까지 대기 (배치 용도라 blockLast 사용)
            .blockLast();

        long seconds = between(startTime, LocalDateTime.now()).getSeconds();

        return String.format(
            "총 %d개 국가, %d개 공휴일 저장 완료 (성공: %d, 실패: %d, 소요시간: %d초)",
            countries.size(), totalSaved.get(), successCount.get(), failCount.get(), seconds
        );
    }

    /**
     * 특정 국가/특정 연도의 공휴일 데이터를 외부 API에서 조회한 뒤 DB에 저장.
     * - WebClient 호출 (비동기)
     * - 받은 데이터를 blocking DB 저장 스레드로 넘김
     * - 오류 발생 시 로그 기록 후 건너뜀
     * @param country   조회할  국가
     * @param year 조회할 년도
     * @param totalSaved 총 저장된 데이터 수
     * @param successCount 저장 성공한 데이터 수
     * @param failCount 실패한 데이터 수
     * @return Void
     */
    private Mono<Void> fetchAndSave(
        Country country,
        int year,
        AtomicInteger totalSaved,
        AtomicInteger successCount,
        AtomicInteger failCount
    ) {
        return webClient.get()
            .uri("/PublicHolidays/{year}/{code}", year, country.getCountryCode())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<HolidaySaveRequest>>() {})
            .flatMap(holidaySaveRequests -> {

                // 데이터가 없을 경우 (일부 국가/년도의 API 응답 특성)
                if (holidaySaveRequests == null || holidaySaveRequests.isEmpty()) {
                    return Mono.empty();
                }

                /**
                 * DB 저장은 blocking 작업이므로
                 * Mono.fromCallable + boundedElastic로 분리하여 실행
                 */
                return Mono.fromCallable(() -> {

                    // DTO → 엔티티 변환
                    List<Holiday> entities = holidaySaveRequests.stream()
                        .map(request -> new Holiday(request.date(), request.localName(), request.name(), country,
                            request.fixed(), request.global(), request.counties(), request.launchYear(), request.types()))
                        .toList();

                    // 단건 트랜잭션으로 저장
                    int saved = holidayDataService.saveHolidays(entities);

                    totalSaved.addAndGet(saved);
                    successCount.incrementAndGet();

                    log.info("[HolidayKeeperService] {}-{} 저장 {}",
                        country.getCountryCode(), year, saved);

                    return saved;

                }).publishOn(Schedulers.boundedElastic());
            })
            // 호출 오류 시 실패 카운트 증가 + 로그
            .doOnError(e -> {
                failCount.incrementAndGet();
                log.error("[HolidayKeeperService] {}-{} 실패: {}",
                    country.getCountryCode(), year, e.getMessage());
            })
            // 오류 발생해도 전체 플로우는 계속 진행
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    /**
     * 특정 연도·국가 데이터를 재호출하여 덮어쓰는 메서드
     * @param countryCode 국가코드
     * @param year 년도
     * @return 저장 완료 및 통계 메세지
     */
    public String refreshHolidays(String countryCode, Integer year) {

        LocalDateTime startTime = LocalDateTime.now();
        log.info("[HolidayKeeperService] Loading holidays countryCode: {}, year: {}", countryCode, year);

        // 처리 대상 국가 목록 조회
        Country country = countryRepository.findByCountryCode(countryCode)
            .orElseThrow(CountryCodeNotFoundException::new);

        // 저장/성공/실패 카운터
        AtomicInteger totalSaved = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

       fetchAndSave(country, year, totalSaved, successCount, failCount)
            .subscribeOn(Schedulers.boundedElastic())
            .block();

        long seconds = between(startTime, LocalDateTime.now()).getSeconds();

        return String.format(
            "%s 국가, %d개 공휴일 저장 완료 (성공: %d, 실패: %d, 소요시간: %d초)",
            countryCode, totalSaved.get(), successCount.get(), failCount.get(), seconds
        );
    }

    /**
     * 해당 연도의 모든 국가 공휴일 전체 갱신 (배치 자동화를 위한 메서드)
     * @param year 해당연도
     */
    public String refreshYearForAllCountries(int year) {

        List<Country> countries = countryRepository.findAll();
        if (countries.isEmpty()) {
            log.warn("[HolidayKeeperService] 국가 정보가 없어 연도 갱신을 종료합니다.");
            return "국가 정보가 없어 연도 갱신을 종료합니다.";
        }

        AtomicInteger totalSaved = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        Flux.fromIterable(countries)
            .flatMap(country ->
                    fetchAndSave(country, year, totalSaved, successCount, failCount),
                CONCURRENCY
            )
            .subscribeOn(Schedulers.boundedElastic())
            .blockLast();

        return String.format(
            "총 %d개 국가, %d개 공휴일 저장 완료 (성공: %d, 실패: %d)",
            countries.size(), totalSaved.get(), successCount.get(), failCount.get()
        );
    }
}
