package com.myweather.backend.database.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AirTemperature {

    @JsonProperty("timestamp")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    public final LocalDateTime timestamp;

    @JsonProperty("location")
    public final String location;

    @JsonProperty("value")
    public final Double value;

    public AirTemperature(LocalDateTime timestamp, Double value, String location) {
        this.timestamp = timestamp;
        this.value = value;
        this.location = location;
    }
}
