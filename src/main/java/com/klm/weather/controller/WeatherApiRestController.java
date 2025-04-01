package com.klm.weather.controller;

import com.klm.weather.model.Weather;
import com.klm.weather.model.WeatherDTO;
import com.klm.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/weather")
public class WeatherApiRestController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherApiRestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping
    public ResponseEntity<WeatherDTO> createWeatherRecord(@RequestBody Weather weather) {
        // Converting to WeatherDTO after creating the record in the database
        WeatherDTO createdWeather = weatherService.createWeatherRecord(weather);
        return ResponseEntity.status(201).body(createdWeather);
    }

    @GetMapping
    public ResponseEntity<Page<WeatherDTO>> getAllWeatherRecords(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,   // Default to first page
            @RequestParam(defaultValue = "10") int size   // Default to 10 results per page
    ) {
        // Fetch paginated weather records based on the given filters
        Page<WeatherDTO> records = weatherService.getAllWeatherRecords(date, city, sort, page, size);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeatherDTO> getWeatherById(@PathVariable Integer id) {
        return weatherService.getWeatherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
