package com.jcatena.travelbackend.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcatena.travelbackend.trip.dto.TripRequest;
import com.jcatena.travelbackend.trip.dto.TripResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TripController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad (JWT, etc.) en este test
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    @Test
    void createTrip_shouldReturnCreatedTripResponse() throws Exception {
        // given (request JSON)
        TripRequest request = new TripRequest();
        request.setName("Viaje a Asturias");
        request.setDescription("Ruta norte");
        request.setCurrency("EUR");
        request.setStartDate(LocalDate.of(2025, 7, 1));
        request.setEndDate(LocalDate.of(2025, 7, 5));

        // lo que el servicio devolverá
        TripResponse response = TripResponse.builder()
                .id(1L)
                .name("Viaje a Asturias")
                .description("Ruta norte")
                .currency("EUR")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 5))
                .ownerId(10L)
                .build();

        given(tripService.createTrip(any(TripRequest.class))).willReturn(response);

        // when + then
        mockMvc.perform(
                        post("/api/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Viaje a Asturias"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.ownerId").value(10L));

        // opcional: verificar que se llamó al servicio
        Mockito.verify(tripService).createTrip(any(TripRequest.class));
    }

    @Test
    void getMyTrips_shouldReturnListOfTrips() throws Exception {
        // given
        TripResponse trip1 = TripResponse.builder()
                .id(1L)
                .name("Viaje a Asturias")
                .description("Ruta norte")
                .currency("EUR")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 5))
                .ownerId(10L)
                .build();

        TripResponse trip2 = TripResponse.builder()
                .id(2L)
                .name("Escapada a Lisboa")
                .description("Fin de semana")
                .currency("EUR")
                .startDate(LocalDate.of(2025, 9, 10))
                .endDate(LocalDate.of(2025, 9, 12))
                .ownerId(10L)
                .build();

        given(tripService.listTrips()).willReturn(List.of(trip1, trip2));

        // when + then
        mockMvc.perform(
                        get("/api/trips")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // tamaño de la lista
                .andExpect(jsonPath("$.length()").value(2))
                // primer trip
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Viaje a Asturias"))
                .andExpect(jsonPath("$[0].currency").value("EUR"))
                // segundo trip
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Escapada a Lisboa"))
                .andExpect(jsonPath("$[1].currency").value("EUR"));

        Mockito.verify(tripService).listTrips();
    }

}

