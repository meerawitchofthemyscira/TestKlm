package com.klm.weather.repository;

import com.klm.weather.model.Weather;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Integer> {
    Page<Weather> findByDate(LocalDate date, Pageable pageable);

    Page<Weather> findByCity(String city, Pageable pageable);

    @Query("SELECT w FROM Weather w WHERE w.date = :date AND w.city = :city")
    Page<Weather> findByDateAndCity(@Param("date") LocalDate date, @Param("city") String city, Pageable pageable);
}

