package com.holidaykeeper.repository;

import com.holidaykeeper.entity.Country;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, UUID> {

    Optional<Country> findByCountryCode(String countryCode);
}
