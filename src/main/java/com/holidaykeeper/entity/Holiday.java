package com.holidaykeeper.entity;

import com.holidaykeeper.mapper.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_holidays")
@Getter
@NoArgsConstructor
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "local_name")
    private String localName;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="country_id")
    private Country country;

    @Column(name = "fixed")
    private boolean fixed;

    @Column(name = "global")
    private boolean global;

    @Column(name = "counties")
    @Convert(converter = StringListConverter.class)
    private List<String> counties;

    @Column(name = "launch_year")
    private Integer launchYear;

    @Column(name = "types")
    @Convert(converter = StringListConverter.class)
    private List<String> types;

    public Holiday(LocalDate date, String localName, String name, Country country, boolean fixed,
        boolean global, List<String> counties, Integer launchYear, List<String> types) {
        this.date = date;
        this.localName = localName;
        this.name = name;
        this.country = country;
        this.fixed = fixed;
        this.global = global;
        this.counties = counties;
        this.launchYear = launchYear;
        this.types = types;
    }

    @Override
    public String toString() {
        return "Holiday{" +
            "date=" + date +
            ", localName='" + localName + '\'' +
            ", name='" + name + '\'' +
            ", country=" + country +
            '}';
    }
}
