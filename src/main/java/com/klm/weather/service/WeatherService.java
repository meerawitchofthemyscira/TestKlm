package com.klm.weather.service;

import com.klm.weather.model.Weather;
import com.klm.weather.repository.WeatherRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;

    public WeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public Weather createWeatherRecord(Weather weather) {
        return weatherRepository.save(weather);
    }

    public List<Weather> getAllWeatherRecords(Optional<String> date, Optional<String> city, Optional<String> sort) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return weatherRepository.findAll().stream()
                .filter(record -> date.map(d -> {
                    try {
                        // Parse the input string date to a LocalDate object
                        LocalDate filterDate = LocalDate.parse(d, dateFormatter);
                        // Convert the record's Date to LocalDate for comparison
                        LocalDate recordDate = convertToLocalDate(record.getDate());
                        return filterDate.equals(recordDate);
                    } catch (Exception e) {
                        // Handle parsing exception and return false if invalid
                        return false;
                    }
                }).orElse(true))
                .filter(record -> city.map(c -> Arrays.stream(c.split(","))
                        .anyMatch(cityName -> cityName.equalsIgnoreCase(record.getCity()))).orElse(true))
                .sorted((o1, o2) -> {
                    if (sort.isPresent()) {
                        if ("date".equals(sort.get())) {
                            int dateComparison = o1.getDate().compareTo(o2.getDate());
                            return dateComparison != 0 ? dateComparison : Integer.compare(o1.getId(), o2.getId());
                        } else if ("-date".equals(sort.get())) {
                            int dateComparison = o2.getDate().compareTo(o1.getDate());
                            return dateComparison != 0 ? dateComparison : Integer.compare(o1.getId(), o2.getId());
                        }
                    }
                    return Integer.compare(o1.getId(), o2.getId());
                })
                .collect(Collectors.toList());
    }

    public Optional<Weather> getWeatherById(int id) {
        return weatherRepository.findById(id);
    }

    private LocalDate convertToLocalDate(java.util.Date date) {
        // Convert Date to LocalDate
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }
}
