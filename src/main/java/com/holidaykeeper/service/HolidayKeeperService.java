package com.holidaykeeper.service;

import com.holidaykeeper.dto.HolidayDto;
import com.holidaykeeper.repository.CountryRepository;
import com.holidaykeeper.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HolidayKeeperService {

    private final CountryRepository countryRepository;
    private final HolidayRepository holidayRepository;


    public HolidayDto save(){

    }
}
