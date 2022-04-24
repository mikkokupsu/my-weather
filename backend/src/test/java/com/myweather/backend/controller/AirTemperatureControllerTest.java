package com.myweather.backend.controller;

import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;

import com.myweather.backend.controller.AirTemperatureController;
import com.myweather.backend.database.IAirTemperatureRepository;
import com.myweather.backend.database.model.AirTemperature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("AirTemperatureController Tests")
public class AirTemperatureControllerTest {
    
    private IAirTemperatureRepository airTemperatureRepository;

    private AirTemperatureController controller;

    private final String location = "test location";
    private final LocalDateTime start = LocalDateTime.of(2022, 4, 24, 10, 0, 0);
    private final LocalDateTime stop = LocalDateTime.of(2022, 4, 24, 12, 0, 0);
    private final String startParam = "2022-04-24T10:00:00";
    private final String stopParam = "2022-04-24T12:00:00";

    private final AirTemperature airTemperature = new AirTemperature(LocalDateTime.of(2022, 4, 24, 11, 0, 0), 1.23d, location);


    @BeforeEach
    public void init() {
        this.airTemperatureRepository = Mockito.mock(IAirTemperatureRepository.class);
        
        this.controller = new AirTemperatureController(this.airTemperatureRepository);
    }

    @Test
    public void testGetHappy() throws Exception {
        Mockito.when(airTemperatureRepository.get(anyString(), any(), any())).thenReturn(Collections.emptyList());

        controller.get(location, startParam, stopParam);

        Mockito.verify(airTemperatureRepository).get(location, start, stop);
        Mockito.verifyNoMoreInteractions(airTemperatureRepository);
    }

    @Test
    public void testGetInvalidStart() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.get(location, "2022-04-24 10:00", stopParam));
        Mockito.verifyNoInteractions(airTemperatureRepository);
    }

    @Test
    public void testGetInvalidStop() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.get(location, startParam, "2022-04-24 12:00"));
        Mockito.verifyNoInteractions(airTemperatureRepository);
    }

    @Test
    public void testSaveHappy() throws Exception {
        Mockito.when(airTemperatureRepository.save(any())).thenReturn(airTemperature);

        controller.save(airTemperature);

        Mockito.verify(airTemperatureRepository).save(airTemperature);
        Mockito.verifyNoMoreInteractions(airTemperatureRepository);
    }

    @Test
    public void testUpdateHappy() throws Exception {
        Mockito.when(airTemperatureRepository.update(any())).thenReturn(airTemperature);

        controller.update(airTemperature, location, "2022-04-24T11:00:00");

        Mockito.verify(airTemperatureRepository).update(airTemperature);
        Mockito.verifyNoMoreInteractions(airTemperatureRepository);
    }

    @Test
    public void testUpdateInvalidLocationNull() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.update(airTemperature, null, "2022-04-24T11:00:00"));

        Mockito.verifyNoInteractions(airTemperatureRepository);
    }

    @Test
    public void testUpdateInvalidLocationOther() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.update(airTemperature, "other location", "2022-04-24T11:00:00"));

        Mockito.verifyNoInteractions(airTemperatureRepository);
    }
    
    @Test
    public void testUpdateInvalidTimestampNull() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.update(airTemperature, location, null));

        Mockito.verifyNoInteractions(airTemperatureRepository);
    }

    @Test
    public void testUpdateInvalidTimestampDifferent() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.update(airTemperature, location, "2022-04-24T12:00:00"));

        Mockito.verifyNoInteractions(airTemperatureRepository);
    }

    @Test
    public void testUpdateInvalidTimestampInvalid() throws Exception {
        Assertions.assertThrows(ResponseStatusException.class, () -> controller.update(airTemperature, location, "2022-04-24 11:00:00"));

        Mockito.verifyNoInteractions(airTemperatureRepository);
    }

    @Test
    public void testDeleteHappy() throws Exception {        
        Mockito.when(airTemperatureRepository.delete(any())).thenReturn(airTemperature);

        controller.delete(location, "2022-04-24T11:00:00");

        Mockito.verify(airTemperatureRepository).delete(argThat(
            (AirTemperature at) -> at.location.equals(location) && at.timestamp.equals(LocalDateTime.of(2022, 4, 24, 11, 0, 0))));
    }
}
