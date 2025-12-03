package com.holidaykeeper.repository;

import com.holidaykeeper.entity.Holiday;
import com.holidaykeeper.entity.QHoliday;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class HolidayRepositoryImpl implements HolidayRepositoryCustom{


    private final JPAQueryFactory jpaQueryFactory;
    private final QHoliday holiday = QHoliday.holiday;


    /**
     * 커서 페이지네이션을 통해 검색된 공휴일 리스트를 반환하는 메서드
     * @param cursor 커서
     * @param idAfter 다음 페이지 첫번째 항목의 UUID
     * @param size 한 페이지가 가지는 user의 수
     * @param sortBy 정렬자
     * @param sortDirection 정렬 방향
     * @param countryCode 국가 코드
     * @param fromDate 검색 시간(~부터)
     * @param toDate (~까지)
     * @param holidayType 공휴일 타입
     * @param nameLike 해당 단어가 포함된 공휴일 검색
     * @return 페이지네이션으로 검색된 공휴일 리스트
     */
    @Override
    public List<Holiday> findHolidaysWithCursor(String cursor, UUID idAfter, Integer size,
        String sortBy, String sortDirection, String countryCode, LocalDate fromDate,
        LocalDate toDate, String holidayType, String nameLike) {

        log.info("[HolidayRepositoryImpl] Searching holidays");
        OrderSpecifier<?> tieBreakerOrder = holiday.id.asc(); // 이름 또는 기간이 동일하여 정렬순서가 정해지지 않을때를 대비해서 이름또는 기간이 동일할때는 id 오름차순으로 레코드를 보여줌 (tie-breaker)
        Order order = sortDirection.equals("ASC") ? Order.ASC : Order.DESC;
        List<Holiday> result = jpaQueryFactory.selectFrom(holiday)
            .where(
                buildCursorCondition(cursor, idAfter, order, sortBy),
                countryCodeCondition(countryCode),
                dateCondition(fromDate,toDate),
                nameLikeCondition(nameLike),
                holidayTypeCondition(holidayType)
            )
            .orderBy(buildOrderSpecifier(sortBy, order),tieBreakerOrder)
            .limit(size)
            .fetch();

        log.info("[HolidayRepositoryImpl] Searched holidays");
        return result;

    }


    /**
     * 커서를 기준으로 검색범위를 지정하는 메서드
     * @param cursor 검색하길 원하는 커서
     * @param idAfter  보조 검색자(UUID)
     * @param sortBy  정렬할 필드 (이름, 기간)
     * @param order 정렬 순서
     * @return  검색 범위 조건
     */
    private BooleanExpression buildCursorCondition(String cursor, UUID idAfter,Order order, String sortBy){
        if(cursor == null || idAfter == null){
            return null;
        }
        switch(sortBy){
            case "name":
                if(order.equals(Order.ASC)){
                    return holiday.name.gt(cursor)
                        .or(holiday.name.eq(cursor).and(holiday.id.goe(idAfter)));
                }
                else{
                    return holiday.name.loe(cursor)
                        .or(holiday.name.eq(cursor).and(holiday.id.goe(idAfter)));
                }
            case "date":
                if(order.equals(Order.ASC)){
                    return holiday.date.goe(LocalDate.parse(cursor))
                        .or(holiday.date.eq(LocalDate.parse(cursor)).and(holiday.id.goe(idAfter)));
                }
                else{
                    return holiday.date.loe(LocalDate.parse(cursor))
                        .or(holiday.date.eq(LocalDate.parse(cursor)).and(holiday.id.goe(idAfter)));
                }
            default:
                return null;
        }
    }

    /**
     * 정렬 조건 생성 메서드 (기본적으로 이름순으로 검색)
     *
     * @param sortBy 정렬하길 원하는 필드
     * @param order    정렬방법
     * @return OrderSpecifier 결정된 정렬순서
     */
    private OrderSpecifier<?> buildOrderSpecifier(String sortBy, Order order){

        switch (sortBy) {
            case "name":
                return new OrderSpecifier<>(order, holiday.name);
            case "date":
                return new OrderSpecifier<>(order, holiday.date);
            default:
                return new OrderSpecifier<>(order, holiday.name);
        }

    }

    /**
     *  검색된 공휴일이 총 몇개인지 검색하는 메서드
     * @param countryCode 국가 코드
     * @param fromDate 검색 시간(~부터)
     * @param toDate (~까지)
     * @param holidayType 공휴일 타입
     * @param nameLike 해당 단어가 포함된 공휴일 검색
     * @return 검색된 공휴일 수
     */
    @Override
    public Long countHolidays(String countryCode, LocalDate fromDate, LocalDate toDate,
        String holidayType, String nameLike) {
        log.info("[HolidayRepositoryImpl] Counting holidays");
        Long result = jpaQueryFactory
            .select(holiday.count())
            .from(holiday)
            .where(
                countryCodeCondition(countryCode),
                dateCondition(fromDate,toDate),
                nameLikeCondition(nameLike),
                holidayTypeCondition(holidayType)
            ).fetchOne();
        log.info("[HolidayRepositoryImpl] Counted holidays: {}", result);
        return result;
    }


    /**
     * 국가코드가 일치하는 레코드만 검색
     * @param countryCode 찾고자하는 국가코드
     * @return 국가코드가 일치하는 공휴일
     */
    private BooleanExpression countryCodeCondition(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        return holiday.country.countryCode.eq(countryCode);
    }

    /**
     * FromDate와 ToDate 사이의 기간을 검색
     * @param fromDate ~부터
     * @param toDate ~까지
     * @return 해당 기간 동안의 공휴일
     */
    private BooleanExpression dateCondition(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return null;
        }
        return holiday.date.between(fromDate, toDate);
    }

    /**
     * 해당되는 단어가 존재하는 공휴일을 검색
     * @param nameLike 찾고자하는 단어
     * @return 해당 단어가 포함된 공휴일
     */
    private BooleanExpression nameLikeCondition(String nameLike) {
        if (nameLike == null || nameLike.isBlank()) {
            return null;
        }
        return holiday.name.likeIgnoreCase("%" + nameLike + "%");
    }


    /**
     * 해당 공휴일 타입을 포함하는 공휴일을 검색
     * @param holidayType 찾고자하는 공휴일 타입
     * @return 해당되는 타입의 공휴일
     */
    private BooleanExpression holidayTypeCondition(String holidayType) {
        if(holidayType == null || holidayType.isBlank()){
            return null;
        }

        return holiday.types.likeIgnoreCase("%" + holidayType + "%");

    }
}
