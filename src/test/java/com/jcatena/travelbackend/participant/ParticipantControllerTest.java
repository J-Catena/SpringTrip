package com.jcatena.travelbackend.participant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcatena.travelbackend.participant.dto.ParticipantRequest;
import com.jcatena.travelbackend.participant.dto.ParticipantResponse;
import com.jcatena.travelbackend.participant.dto.ParticipantUpdateRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = ParticipantController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)


@Disabled("Temporarily disabled: security + MockMvc config pending")
class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipantService participantService;

    @Test
    void addParticipant_shouldReturnParticipantResponse() throws Exception {
        Long tripId = 1L;

        ParticipantRequest request = new ParticipantRequest();
        request.setName("Juan");

        ParticipantResponse response = ParticipantResponse.builder()
                .id(10L)
                .name("Juan")
                .tripId(tripId)
                .build();

        BDDMockito.given(
                participantService.addParticipantToTrip(
                        BDDMockito.eq(tripId),
                        anyLong(),                    // userId ignorado
                        any(ParticipantRequest.class)
                )
        ).willReturn(response);

        mockMvc.perform(
                        post("/api/trips/{tripId}/participants", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.tripId").value(tripId));
    }

    @Test
    void getParticipants_shouldReturnList() throws Exception {
        Long tripId = 1L;

        List<ParticipantResponse> list = List.of(
                ParticipantResponse.builder()
                        .id(10L).name("Juan").tripId(tripId).build(),
                ParticipantResponse.builder()
                        .id(11L).name("Maria").tripId(tripId).build()
        );

        BDDMockito.given(
                participantService.getParticipantsByTrip(
                        BDDMockito.eq(tripId),
                        anyLong()              // userId ignorado
                )
        ).willReturn(list);

        mockMvc.perform(
                        get("/api/trips/{tripId}/participants", tripId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Juan"))
                .andExpect(jsonPath("$[1].name").value("Maria"));
    }

    @Test
    void updateParticipant_shouldReturnUpdated() throws Exception {
        Long tripId = 1L;
        Long participantId = 10L;

        ParticipantUpdateRequest request = new ParticipantUpdateRequest();
        request.setName("Juan Actualizado");

        ParticipantResponse response = ParticipantResponse.builder()
                .id(participantId)
                .name("Juan Actualizado")
                .tripId(tripId)
                .build();

        BDDMockito.given(
                participantService.updateParticipant(
                        BDDMockito.eq(tripId),
                        BDDMockito.eq(participantId),
                        anyLong(),                        // userId ignorado
                        any(ParticipantUpdateRequest.class)
                )
        ).willReturn(response);

        mockMvc.perform(
                        put("/api/trips/{tripId}/participants/{participantId}", tripId, participantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(participantId))
                .andExpect(jsonPath("$.name").value("Juan Actualizado"))
                .andExpect(jsonPath("$.tripId").value(tripId));
    }
}
