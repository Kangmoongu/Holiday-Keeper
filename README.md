# Holiday Keeper

전 세계 120개국의 공휴일 데이터를 관리하는 REST API 서비스입니다.

## 프로젝트 개요

- **외부 API**: [Nager.Date Public Holiday API](https://date.nager.at)
- **데이터 범위**: 119개국, 2020-2025년 (최근 5년)
- **기술 스택**: Spring Boot 3.4.12, Spring WebFlux, JPA, H2 Database, Querydsl 5.0.0, MapStruct 1.6.3

## 주요 기능

1. **초기 데이터 로드**: 애플리케이션 시작 시 최근 5년간의 모든 국가 공휴일 자동 저장
2. **공휴일 검색**: 국가, 연도, 월, 공휴일명으로 필터링 및 페이징 조회
3. **데이터 갱신**: 특정 국가/연도의 공휴일 데이터 재조회 및 갱신
4. **데이터 삭제**: 특정 국가/연도의 공휴일 데이터 전체 삭제
5. **배치 자동화**: 매년 1월 2일 01:00 KST에 전년도·금년도 데이터 자동 동기화

## 빌드 & 실행 방법

### 1. 저장소 클론
```bash
git clone https://github.com/Kangmoongu/Holiday-Keeper
cd Holiday-Keeper
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

- 포트: `8080`
- 실행 완료 로그: `[HolidayKeeperInitializer] initialization completed successfully`

### 3. H2 Console 접속 (선택)
```
URL: http://localhost:8080/h2
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (공백)
```

## API 명세

### 1. 초기 데이터 저장
최근 5년간의 모든 국가 공휴일 데이터를 외부 API에서 가져와 DB에 저장합니다.
```http
POST /api/holidays
```

**응답 예시**
```
총 119개 국가, 9999개 공휴일 저장 완료 (성공: 714, 실패: 0, 소요시간: 10초)
```

---

### 2. 공휴일 검색
커서 기반 페이지네이션으로 공휴일을 검색합니다.
```http
GET /api/holidays
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| countryCode | String | X | 국가 코드 | KR, US, JP |
| fromDate | LocalDate | X | 시작 날짜 (YYYY-MM-DD) | 2024-01-01 |
| toDate | LocalDate | X | 종료 날짜 (YYYY-MM-DD) | 2024-12-31 |
| holidayType | String | X | 공휴일 타입 | Public |
| nameLike | String | X | 공휴일 이름 (부분 검색) | Christmas |
| cursor | String | X | 다음 페이지 커서 (이전 응답에서 제공) | - |
| idAfter | UUID | X | 다음 페이지의 첫 번째 레코드 ID | - |
| size | Integer | O | 페이지 크기 | 20 |
| sortBy | String | O | 정렬 기준 | date, name |
| sortDirection | String | O | 정렬 방향 | ASC, DESC |

**요청 예시**
```http
한국의 2024년 공휴일 조회 (날짜 오름차순, 20개씩)
GET /api/holidays?countryCode=KR&fromDate=2024-01-01&toDate=2024-12-31&size=20&sortBy=date&sortDirection=ASC

'Christmas' 키워드로 검색 (이름 오름차순, 10개씩)
GET /api/holidays?nameLike=Christmas&size=10&sortBy=name&sortDirection=ASC

2024년 12월의 모든 공휴일 (날짜 기준)
GET /api/holidays?fromDate=2024-12-01&toDate=2024-12-31&size=20&sortBy=date&sortDirection=ASC

Public 타입 공휴일만 조회
GET /api/holidays?holidayType=Public&size=20&sortBy=date&sortDirection=ASC

다음 페이지 조회 (cursor와 idAfter는 이전 응답에서 제공)
GET /api/holidays?countryCode=KR&size=20&sortBy=date&sortDirection=ASC&cursor=xxx&idAfter=xxx
```

**응답 예시**
```json
{
  "data": [
    {
      "id": "86b8b1e1-556d-46e4-9d03-66d71fb9962a",
      "date": "2024-01-01",
      "localName": "새해",
      "name": "New Year's Day",
      "countryCode": "KR",
      "fixed": true,
      "global": true,
      "counties": null,
      "launchYear": null,
      "types": ["Public"]
    },
    {
      "id": "b2234649-3dd8-4e02-814a-b095ca57b9e1",
      "date": "2024-02-09",
      "localName": "설날",
      "name": "Lunar New Year",
      "countryCode": "KR",
      "fixed": false,
      "global": true,
      "counties": null,
      "launchYear": null,
      "types": ["Public"]
    }
  ],
    "nextCursor": "Lunar New Year",
    "nextIdAfter": "f1c336b3-0bc7-48cb-bd8c-a90411e21d3b",
    "hasNext": true,
    "totalCount": 4,
    "SortBy": "name",
    "SortDirection": "ASC"
}
```

**커서 기반 페이지네이션**
- 첫 번째 요청: `cursor`와 `idAfter` 파라미터 없이 요청
- 다음 페이지: 응답의 `pageInfo.cursor`와 `pageInfo.idAfter` 값을 사용하여 요청
- `hasNext`가 `false`이면 마지막 페이지

---

---

### 3. 특정 국가/연도 데이터 갱신
특정 국가의 특정 연도 공휴일 데이터를 재조회하여 갱신합니다.
```http
PATCH /api/holidays/{countryCode}/{year}
```

**PathVariables**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| countryCode | String | O | 국가 코드 |
| year | Integer | O | 연도 |

**요청 예시**
```http
PATCH /api/holidays/KR/2024
```

**응답 예시**
```
KR 국가, 15개 공휴일 저장 완료 (성공: 1, 실패: 0, 소요시간: 1초)
```

---

### 4. 특정 국가/연도 데이터 삭제
특정 국가의 특정 연도 공휴일 데이터를 전체 삭제 합니다.
```http
DELETE /api/holidays/{countryCode}/{year}
```

**PathVariables**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| countryCode | String | O | 국가 코드 |
| year | Integer | O | 연도 |

**요청 예시**
```http
DELETE /api/holidays/KR/2024
```

**응답 예시**
```
204 No Content
```


## Swagger UI 문서 확인

애플리케이션 실행 후 아래 URL에서 API 문서를 확인할 수 있습니다.

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api-docs
```

Swagger UI에서 직접 API를 테스트할 수 있습니다.

## 기술 스택

- **Framework**: Spring Boot 3.4.12
- **Language**: Java 21
- **Reactive**: Spring WebFlux (비동기 API 호출)
- **Database**: H2 (In-Memory)
- **ORM**: Spring Data JPA, QueryDSL
- **API Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Gradle

## 아키텍처 특징

### 1. 비동기 처리 (WebFlux)
- 119개국 × 6년 = 714회의 API 호출을 병렬 처리
- `Flux.flatMap`을 활용한 동시성 제어 (CONCURRENCY = 10)

### 2. 동적 쿼리 (QueryDSL)
- 다중 필터 조건을 동적으로 조합
- 커서 페이지네이션 및 정렬 지원

### 3. 배치 스케줄링
- Spring `@Scheduled` 활용
- 매년 1월 2일 01:00 KST 자동 실행
- 전년도 + 금년도 데이터 자동 갱신

## 프로젝트 구조
```
src/main/java/com/holidaykeeper/
├── config/
│   ├── BatchConfig.java              # 스케줄링 활성화
│   ├── BatchScheduler.java           # 배치 스케줄러
│   ├── ConuntryInitializer.java      # 초기 국가 데이터 저장
│   ├── HolidayKeeperInitializer.java # 초기 공휴일 데이터 저장
│   ├── QuerydslConfig.java           # QueryDSL 설정
│   ├── RestTemplateConfig.java       # RestTemplate 설정
│   └── WebClientConfig.java          # WebClient 설정
├── controller/
│   ├── HolidayApi.java               # 공휴일 API 명세
│   └── HolidayController.java        # 공휴일 API
├── dto/
│   ├── request/
│   │   └── HolidaySaveRequest.java
│   ├── response/
│   |   └── HolidaySaveResponse.java
│   ├── CountryDto.java
│   └── HolidayDto.java
├── entity/
│   ├── Country.java
│   └── Holiday.java
├── exception/
│   ├── CountryCodeNotFoundException.java
|   ├── ErrorCode.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── HolidayKeeperException.java        
├── repository/
│   ├── CountryRepository.java
│   ├── HolidayRepository.java        
|   ├── HolidayRepositoryCustom.java  
│   └── HolidayRepositoryImpl.java    # QueryDSL 구현
└── service/
    ├── HolidayDataService.java       
    └── HolidayKeeperService.java     
```

## 배치 자동화

### 스케줄 설정
```java
@Scheduled(cron = "0 0 1 2 1 *", zone = "Asia/Seoul")
public void refreshLastTwoYears() {
    // 매년 1월 2일 01:00 KST 실행
    // 전년도 + 금년도 데이터 자동 갱신
}
```

### 동작 방식
1. 매년 1월 2일 새벽 1시에 자동 실행
2. 전년도(예: 2024)와 금년도(예: 2025) 데이터 갱신
3. 모든 국가에 대해 2개 연도 데이터 재조회 후 저장

## 성능 최적화

1. **병렬 처리**: WebFlux를 활용한 동시 API 호출 (10개씩)
2. **페이징**: 데이터 조회 시 커서 페이지네이션으로으로 OFFSET 페이지네이션 대비 성능 확보


## 제작자

**강문구** - Backend Developer
