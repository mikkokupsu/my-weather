package com.myweather.backend.database;

import java.time.LocalDateTime;
import java.util.List;

import com.myweather.backend.database.model.AirTemperature;
import com.myweather.exception.AwsError;
import com.myweather.exception.NotFound;

public interface IAirTemperatureRepository {

    /***
     * Search all air temperates where timestamp is larger or equal to start and lesser than stop.
     * @param location Location to search with.
     * @param start Inclusive timestamp to search with.
     * @param stop Exclusive timestamp to search with.
     * @return Air temperates where timestamp is larger or equal to start, and lesser that stop.
     */
    List<AirTemperature> get(String location, LocalDateTime start, LocalDateTime stop) throws AwsError;

    /***
     * Save a new air temperature.
     * @param airTemperature New air temperature to save.
     * @return Saved air temperature.
     */
    AirTemperature save(AirTemperature airTemperature) throws AwsError;

    /***
     * Update an existing air temperature.
     * @param airTemperature Air temperature with new values.
     * @return Updated air temperature.
     */
    AirTemperature update(AirTemperature airTemperature) throws AwsError, NotFound;

    /***
     * Delete an air temperature.
     * @param airTemperature Air temperature to delete.
     * @return Delete air temperature.
     */
    AirTemperature delete(AirTemperature airTemperature) throws AwsError, NotFound;
}