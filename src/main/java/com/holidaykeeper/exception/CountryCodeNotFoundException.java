package com.holidaykeeper.exception;

import java.util.UUID;

public class CountryCodeNotFoundException extends HolidayKeeperException {

    public CountryCodeNotFoundException() {
        super(ErrorCode.COUNTRY_CODE_NOT_FOUND);
    }


}

