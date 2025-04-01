package com.klm.weather.model;



import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;

public class WeatherDTO {
    private Integer id;
    @NotNull(message = "Latitude cannot be null")
    private Float lat;
    @NotNull(message = "Longitude cannot be null")
    private Float lon;
    @NotNull(message = "City cannot be null")
    private String city;
    @NotNull(message = "State cannot be null")
    private String state;
    @NotNull(message = "Temperatures cannot be null")
    @Size(min = 1, message = "At least one temperature value is required")
    private List<Double> temperatures;



    // Constructor with all fields
    public WeatherDTO(Integer id, Date date, Float lat, Float lon, String city, String state, List<Double> temperatures) {
        this.id = id;
        this.date = date;
        this.lat = lat;
        this.lon = lon;
        this.city = city;
        this.state = state;
        this.temperatures = temperatures;
    }

    // Constructor without ID
    public WeatherDTO(Date date, Float lat, Float lon, String city, String state, List<Double> temperatures) {
        this.date = date;
        this.lat = lat;
        this.lon = lon;
        this.city = city;
        this.state = state;
        this.temperatures = temperatures;
    }

    // Default Constructor
    public WeatherDTO() {}

    @NotNull(message = "Date cannot be null")
    private Date date;

    public Integer getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public Float getLat() {
        return lat;
    }

    public Float getLon() {
        return lon;
    }

    public String getCity() {
        return city;
    }

    public List<Double> getTemperatures() {
        return temperatures;
    }

    public String getState() {
        return state;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTemperatures(List<Double> temperatures) {
        this.temperatures = temperatures;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
