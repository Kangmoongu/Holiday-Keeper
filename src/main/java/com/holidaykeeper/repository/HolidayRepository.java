package com.holidaykeeper.repository;

import com.holidaykeeper.entity.Holiday;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    List<Holiday> findAllByCountry_Id(UUID id);
}
