package com.klm.weather.repository;

import com.klm.weather.model.Weather;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Integer> {

    @Query("SELECT w FROM Weather w " +
            "WHERE (:date IS NULL OR w.date IS NULL OR w.date = :date) " +
            "AND (:cities IS NULL OR LOWER(w.city) IN (:cities))")
    Page<Weather> findWeatherRecords(@Param("date") Date date,
                                     @Param("cities") List<String> cities,
                                     Pageable pageable);
}

