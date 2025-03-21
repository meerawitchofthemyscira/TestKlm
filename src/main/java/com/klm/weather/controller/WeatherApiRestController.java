package com.klm.weather.controller;

import com.klm.weather.model.Weather;
import com.klm.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/weather")
public class WeatherApiRestController {

    @Autowired
    private  WeatherService weatherService;

    public void WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping
    public ResponseEntity<Weather> createWeatherRecord(@RequestBody Weather weather) {
        Weather createdWeather = weatherService.createWeatherRecord(weather);
        return ResponseEntity.status(201).body(createdWeather);
    }
    @GetMapping
    public ResponseEntity<List<Weather>> getAllWeatherRecords(
            @RequestParam Optional<String> date,
            @RequestParam Optional<String> city,
            @RequestParam Optional<String> sort) {
        List<Weather> records = weatherService.getAllWeatherRecords(date, city, sort);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Weather> getWeatherById(@PathVariable int id) {
        return weatherService.getWeatherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).build());
    }
    }
