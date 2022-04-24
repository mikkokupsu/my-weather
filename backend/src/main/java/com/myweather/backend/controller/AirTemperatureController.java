package com.myweather.backend.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.myweather.backend.database.IAirTemperatureRepository;
import com.myweather.backend.database.model.AirTemperature;
import com.myweather.exception.AwsError;
import com.myweather.exception.NotFound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin("*")
@RestController
public class AirTemperatureController {

    private static final Logger logger = LogManager.getLogger(AirTemperatureController.class);

    private IAirTemperatureRepository repository;

    public AirTemperatureController(IAirTemperatureRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping("/temperature/air")
    public List<AirTemperature> get(@RequestParam("location") String location, @RequestParam("start") String start, @RequestParam("stop") String stop) throws AwsError {
        LocalDateTime startDate = parseLocalDateTime("start", start);
        LocalDateTime stopDateTime = parseLocalDateTime("stop", stop);
        return this.repository.get(location, startDate, stopDateTime);
    }

    @PostMapping(value = "/temperature/air")
    public AirTemperature save(@RequestBody AirTemperature airTemperature) throws AwsError {
        return this.repository.save(airTemperature);
    }

    @PutMapping("/temperature/air/{location}/{timestamp}")
    public AirTemperature update(@RequestBody AirTemperature airTemperature, @PathVariable("location") String location, @PathVariable("timestamp") String timestamp) throws AwsError, NotFound {
        if (location == null || !location.equals(airTemperature.location) || 
                timestamp == null || !parseLocalDateTime("timestamp", timestamp).equals(airTemperature.timestamp)) {

            String msg = String.format("Path parameters did not match with payload locations path=%s payload=%s, timestamps path=%s payload=%s",
                location, airTemperature.location, timestamp, airTemperature.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logger.error(msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        return this.repository.update(airTemperature);
    }

    @DeleteMapping("/temperature/air/{location}/{timestamp}")
    public AirTemperature delete(@PathVariable("location") String location, @PathVariable("timestamp") String timestamp) throws AwsError, NotFound {
        return this.repository.delete(new AirTemperature(parseLocalDateTime("timestamp", timestamp), 0.0, location));
    }

    private static LocalDateTime parseLocalDateTime(String key, String value) {
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception exception) {
            logger.error(String.format("Could not convert value=%s to timestamp", value), exception);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid timestamp: %s=%s", key, value), exception);
        }
    }
}
