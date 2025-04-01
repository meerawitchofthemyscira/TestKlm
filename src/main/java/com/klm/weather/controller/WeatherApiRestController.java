package com.klm.weather.controller;

import com.klm.weather.model.Weather;
import com.klm.weather.model.WeatherDTO;
import com.klm.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/weather")
@Validated
@Tag(name = "Weather API", description = "Endpoints for weather data")
public class WeatherApiRestController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherApiRestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new weather record", description = "Adds a new weather record. Only accessible by ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Weather record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<WeatherDTO> createWeatherRecord(@Valid @RequestBody Weather weather) {
        // Converting to WeatherDTO after creating the record in the database
        WeatherDTO createdWeather = weatherService.createWeatherRecord(weather);
        return ResponseEntity.status(201).body(createdWeather);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    @Operation(summary = "Get all weather records", description = "Retrieves a paginated list of weather records with optional filters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of weather records"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<Page<WeatherDTO>> getAllWeatherRecords(
            @Parameter(description = "Filter by date (YYYY-MM-DD)", example = "2024-03-25")
            @RequestParam(required = false) String date,
            @Parameter(description = "Filter by city name", example = "London")
            @RequestParam(required = false) String city,
            @Parameter(description = "Sorting format (field,direction)", example = "date,asc")
            @RequestParam(defaultValue = "date,asc") String sort,
            @Parameter(description = "Page number (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,   // Default to first page
            @Parameter(description = "Page size (number of results per page)", example = "10")
            @RequestParam(defaultValue = "10") int size   // Default to 10 results per page
    ) {
        // Fetch paginated weather records based on the given filters
        Page<WeatherDTO> records = weatherService.getAllWeatherRecords(date, city, sort, page, size);
        return ResponseEntity.ok(records);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get weather record by ID", description = "Retrieves a single weather record by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weather record found"),
            @ApiResponse(responseCode = "404", description = "Weather record not found")
    })
    public ResponseEntity<WeatherDTO> getWeatherById(@PathVariable Integer id) {
        return weatherService.getWeatherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
