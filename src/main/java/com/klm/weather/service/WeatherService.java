package com.klm.weather.service;

import com.klm.weather.model.Weather;
import com.klm.weather.model.WeatherDTO;
import com.klm.weather.repository.WeatherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherRepository weatherRepository;

    public WeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public WeatherDTO createWeatherRecord(WeatherDTO weather) {

        Weather savedWeather = weatherRepository.save(convertToEntity(weather));
        return convertToDTO(savedWeather);
    }

    public Page<WeatherDTO> getAllWeatherRecords(String date, String city, String sort, int page, int size) {
        logger.info("Fetching weather records with filters - Date: {}, City: {}, Sort: {}", date, city, sort);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate filterDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date, dateFormatter) : null;
        Pageable pageable = PageRequest.of(page, size);

        Page<Weather> weatherPage;

        if (filterDate != null && city != null && !city.isEmpty()) {
            weatherPage = weatherRepository.findByDateAndCity(filterDate, city, pageable);
        } else if (filterDate != null) {
            weatherPage = weatherRepository.findByDate(filterDate, pageable);
        } else if (city != null && !city.isEmpty()) {
            weatherPage = weatherRepository.findByCity(city, pageable);
        } else {
            weatherPage = weatherRepository.findAll(pageable);
        }

        return weatherPage.map(this::convertToDTO);
    }

    // Helper method to convert Date to LocalDate
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public Optional<WeatherDTO> getWeatherById(Integer id) {
        return weatherRepository.findById(id).map(this::convertToDTO);
    }

    private WeatherDTO convertToDTO(Weather weather) {
        return new WeatherDTO(
                weather.getId(),
                weather.getDate(),  // No conversion needed, Date is retained
                weather.getLat(),
                weather.getLon(),
                weather.getCity(),
                weather.getState(),
                weather.getTemperatures()
        );
    }
    private Weather convertToEntity(WeatherDTO weatherDTO) {
        Weather weather = new Weather();
        weather.setCity(weatherDTO.getCity());
        weather.setState(weatherDTO.getState());
        weather.setLat(weatherDTO.getLat());
        weather.setLon(weatherDTO.getLon());
        weather.setTemperatures(weatherDTO.getTemperatures());
        weather.setDate(weatherDTO.getDate());
        return weather;
    }

}
