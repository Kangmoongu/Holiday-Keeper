package com.holidaykeeper.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_countries")
@Getter
@NoArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "name")
    private String name;

    public Country(String countryCode, String name) {
        this.countryCode = countryCode;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Country{" +
            "countryCode='" + countryCode + '\'' +
            ", name='" + name + '\'' +
            '}';
    }

}
