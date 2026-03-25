package ru.univ.grain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.VisitStatus;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.services.VisitService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VisitService visitService;

    @Test
    void getAllVisits_ShouldReturnList() throws Exception {
        VisitDto visit1 = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.BOOKED)
                .build();

        VisitDto visit2 = VisitDto.builder()
                .clientId(2L)
                .workoutSessionId(1L)
                .status(VisitStatus.ATTENDED)
                .build();

        when(visitService.getAllVisits()).thenReturn(List.of(visit1, visit2));

        mockMvc.perform(get("/api/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clientId").value(1L))
                .andExpect(jsonPath("$[1].clientId").value(2L));
    }

    @Test
    void getAllVisits_ShouldReturnEmptyList() throws Exception {
        when(visitService.getAllVisits()).thenReturn(List.of());

        mockMvc.perform(get("/api/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getVisitById_ShouldReturnVisit() throws Exception {
        Long id = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.getVisitById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/visits/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(1L));
    }

    @Test
    void getVisitById_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        when(visitService.getVisitById(id))
                .thenThrow(new ResourceNotFoundException("Визит с id 999 не найден"));

        mockMvc.perform(get("/api/visits/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Визит с id 999 не найден"));
    }

    @Test
    void getClientVisits_ShouldReturnList() throws Exception {
        Long clientId = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(clientId)
                .workoutSessionId(1L)
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.getClientVisits(clientId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/visits/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientId").value(clientId));
    }

    @Test
    void getClientVisits_ShouldReturnEmptyList() throws Exception {
        Long clientId = 999L;

        when(visitService.getClientVisits(clientId)).thenReturn(List.of());

        mockMvc.perform(get("/api/visits/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientUpcomingVisits_ShouldReturnList() throws Exception {
        Long clientId = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(clientId)
                .workoutSessionId(1L)
                .visitTime(LocalDateTime.now().plusDays(1))
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.getClientUpcomingVisits(clientId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/visits/client/{clientId}/upcoming", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getClientUpcomingVisits_ShouldReturnEmptyList() throws Exception {
        Long clientId = 1L;

        when(visitService.getClientUpcomingVisits(clientId)).thenReturn(List.of());

        mockMvc.perform(get("/api/visits/client/{clientId}/upcoming", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientHistory_ShouldReturnList() throws Exception {
        Long clientId = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(clientId)
                .workoutSessionId(1L)
                .status(VisitStatus.ATTENDED)
                .build();

        when(visitService.getClientHistory(eq(clientId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/visits/client/{clientId}/history", clientId)
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getClientHistory_ShouldReturnEmptyList() throws Exception {
        Long clientId = 1L;

        when(visitService.getClientHistory(eq(clientId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/visits/client/{clientId}/history", clientId)
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientVisitsCount_ShouldReturnCount() throws Exception {
        Long clientId = 1L;
        long count = 5L;

        when(visitService.getClientVisitsCount(eq(clientId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(count);

        mockMvc.perform(get("/api/visits/client/{clientId}/count", clientId)
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void getScheduleVisits_ShouldReturnList() throws Exception {
        Long sessionId = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(sessionId)
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.getScheduleVisits(sessionId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/visits/session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getScheduleVisits_ShouldReturnEmptyList() throws Exception {
        Long sessionId = 999L;

        when(visitService.getScheduleVisits(sessionId)).thenReturn(List.of());

        mockMvc.perform(get("/api/visits/session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getTodayVisits_ShouldReturnList() throws Exception {
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.getTodayVisits()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/visits/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTodayVisits_ShouldReturnEmptyList() throws Exception {
        when(visitService.getTodayVisits()).thenReturn(List.of());

        mockMvc.perform(get("/api/visits/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSubscriptionUsedVisits_ShouldReturnCount() throws Exception {
        Long subscriptionId = 1L;
        long count = 8L;

        when(visitService.getSubscriptionUsedVisits(subscriptionId)).thenReturn(count);

        mockMvc.perform(get("/api/visits/subscription/{subscriptionId}/used", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));
    }

    @Test
    void getSubscriptionUsedVisits_ShouldReturnZero() throws Exception {
        Long subscriptionId = 999L;

        when(visitService.getSubscriptionUsedVisits(subscriptionId)).thenReturn(0L);

        mockMvc.perform(get("/api/visits/subscription/{subscriptionId}/used", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getVisitsByHourStats_ShouldReturnStats() throws Exception {
        List<Object[]> stats = List.of(
                new Object[]{10, 5L},
                new Object[]{12, 10L},
                new Object[]{14, 8L}
        );

        when(visitService.getVisitsByHourStats()).thenReturn(stats);

        mockMvc.perform(get("/api/visits/stats/hourly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getVisitsByHourStats_ShouldReturnEmptyList() throws Exception {
        when(visitService.getVisitsByHourStats()).thenReturn(List.of());

        mockMvc.perform(get("/api/visits/stats/hourly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createVisit_ShouldReturnCreatedVisit() throws Exception {
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .subscriptionId(1L)
                .build();

        VisitDto response = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .subscriptionId(1L)
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.createVisit(any(VisitDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(1L));
    }

    @Test
    void createVisit_ShouldReturn400_WhenInvalidData() throws Exception {
        VisitDto invalidDto = new VisitDto();

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void bookWorkout_ShouldReturnCreatedVisit() throws Exception {
        Long clientId = 1L;
        Long sessionId = 1L;
        Long subscriptionId = 1L;

        VisitDto response = VisitDto.builder()
                .clientId(clientId)
                .workoutSessionId(sessionId)
                .subscriptionId(subscriptionId)
                .status(VisitStatus.BOOKED)
                .build();

        when(visitService.bookWorkout(clientId, sessionId, subscriptionId)).thenReturn(response);

        mockMvc.perform(post("/api/visits/book")
                        .param("clientId", String.valueOf(clientId))
                        .param("sessionId", String.valueOf(sessionId))
                        .param("subscriptionId", String.valueOf(subscriptionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId));
    }

    @Test
    void updateVisit_ShouldReturnUpdatedVisit() throws Exception {
        Long id = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.ATTENDED)
                .build();

        VisitDto response = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.ATTENDED)
                .build();

        when(visitService.updateVisit(eq(id), any(VisitDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/visits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));
    }

    @Test
    void updateVisit_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.ATTENDED)
                .build();

        when(visitService.updateVisit(eq(id), any(VisitDto.class)))
                .thenThrow(new ResourceNotFoundException("Визит с id 999 не найден"));

        mockMvc.perform(put("/api/visits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchVisit_ShouldReturnUpdatedVisit() throws Exception {
        Long id = 1L;
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.CANCELLED)
                .build();

        VisitDto response = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.CANCELLED)
                .build();

        when(visitService.updateVisit(eq(id), any(VisitDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/visits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void patchVisit_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.CANCELLED)
                .build();

        when(visitService.updateVisit(eq(id), any(VisitDto.class)))
                .thenThrow(new ResourceNotFoundException("Визит с id 999 не найден"));

        mockMvc.perform(patch("/api/visits/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void markAttendance_ShouldReturnUpdatedVisit() throws Exception {
        Long id = 1L;
        boolean attended = true;

        VisitDto response = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.ATTENDED)
                .build();

        when(visitService.markAttendance(id, attended)).thenReturn(response);

        mockMvc.perform(patch("/api/visits/{id}/attendance", id)
                        .param("attended", String.valueOf(attended)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));
    }

    @Test
    void markAttendance_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;
        boolean attended = true;

        when(visitService.markAttendance(id, attended))
                .thenThrow(new ResourceNotFoundException("Визит с id 999 не найден"));

        mockMvc.perform(patch("/api/visits/{id}/attendance", id)
                        .param("attended", String.valueOf(attended)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_ShouldReturnUpdatedVisit() throws Exception {
        Long id = 1L;

        VisitDto response = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .status(VisitStatus.CANCELLED)
                .build();

        when(visitService.cancelBooking(id)).thenReturn(response);

        mockMvc.perform(patch("/api/visits/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelBooking_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        when(visitService.cancelBooking(id))
                .thenThrow(new ResourceNotFoundException("Визит с id 999 не найден"));

        mockMvc.perform(patch("/api/visits/{id}/cancel", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteVisit_ShouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(visitService).deleteVisit(id);

        mockMvc.perform(delete("/api/visits/{id}", id))
                .andExpect(status().isNoContent());

        verify(visitService).deleteVisit(id);
    }

    @Test
    void deleteVisit_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        doThrow(new ResourceNotFoundException("Визит с id 999 не найден"))
                .when(visitService).deleteVisit(id);

        mockMvc.perform(delete("/api/visits/{id}", id))
                .andExpect(status().isNotFound());
    }
}
