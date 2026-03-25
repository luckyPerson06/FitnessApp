package ru.univ.grain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.dto.WorkoutSessionBulkRequest;
import ru.univ.grain.entities.WorkoutSessionStatus;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.services.WorkoutSessionService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutSessionController.class)
class WorkoutSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkoutSessionService workoutSessionService;

    @Test
    void getAllSessions_ShouldReturnList() throws Exception {
        WorkoutSessionDto session1 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        WorkoutSessionDto session2 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(2L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .maxParticipants(8)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getAllSessions()).thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[1].dayOfWeek").value("WEDNESDAY"));
    }

    @Test
    void getSessionById_ShouldReturnSession() throws Exception {
        Long id = 1L;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getSessionById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/sessions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }

    @Test
    void getSessionsByTrainer_ShouldReturnList() throws Exception {
        Long trainerId = 1L;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(trainerId)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getSessionsByTrainer(trainerId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/trainer/{trainerId}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getSessionsByWorkoutType_ShouldReturnList() throws Exception {
        Long workoutTypeId = 1L;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(workoutTypeId)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getSessionsByWorkoutType(workoutTypeId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/workout-type/{workoutTypeId}", workoutTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getSessionsByDay_ShouldReturnList() throws Exception {
        DayOfWeek day = DayOfWeek.MONDAY;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getSessionsByDay(day)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/day/{dayOfWeek}", day))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    void getActiveSessionsByDay_ShouldReturnList() throws Exception {
        DayOfWeek day = DayOfWeek.MONDAY;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getActiveSessionsByDay(day)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/day/{dayOfWeek}/active", day))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTodaySessions_ShouldReturnList() throws Exception {
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getTodaySessions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getSessionsByStatus_ShouldReturnList() throws Exception {
        WorkoutSessionStatus status = WorkoutSessionStatus.SCHEDULED;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(status)
                .build();

        when(workoutSessionService.getSessionsByStatus(status)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void getAllScheduledSessions_ShouldReturnList() throws Exception {
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getAllScheduledSessions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/scheduled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getSessionsByTime_ShouldReturnList() throws Exception {
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime time = LocalTime.of(10, 0);
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(time)
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.getSessionsByTime(day, time)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/time")
                        .param("dayOfWeek", day.toString())
                        .param("time", "10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void checkTrainerAvailability_ShouldReturnTrue() throws Exception {
        Long trainerId = 1L;
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 30);

        when(workoutSessionService.isTrainerAvailable(trainerId, day, start, end)).thenReturn(true);

        mockMvc.perform(get("/api/sessions/check-availability")
                        .param("trainerId", String.valueOf(trainerId))
                        .param("dayOfWeek", day.toString())
                        .param("start", "10:00:00")
                        .param("end", "11:30:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void findOverlappingSessions_ShouldReturnList() throws Exception {
        Long trainerId = 1L;
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 30);

        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(trainerId)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(start)
                .endTime(end)
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.findOverlappingSessions(trainerId, day, start, end))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/sessions/overlapping")
                        .param("trainerId", String.valueOf(trainerId))
                        .param("dayOfWeek", day.toString())
                        .param("start", "10:00:00")
                        .param("end", "11:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getBookedCount_ShouldReturnCount() throws Exception {
        Long sessionId = 1L;
        long count = 5L;

        when(workoutSessionService.getBookedCount(sessionId)).thenReturn(count);

        mockMvc.perform(get("/api/sessions/{sessionId}/booked-count", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void hasAvailableSpots_ShouldReturnTrue() throws Exception {
        Long sessionId = 1L;

        when(workoutSessionService.hasAvailableSpots(sessionId)).thenReturn(true);

        mockMvc.perform(get("/api/sessions/{sessionId}/available-spots", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void createSession_ShouldReturnCreatedSession() throws Exception {
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .build();

        WorkoutSessionDto response = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.createSession(any(WorkoutSessionDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }

    @Test
    void updateSession_ShouldReturnUpdatedSession() throws Exception {
        Long id = 1L;
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .maxParticipants(15)
                .build();

        WorkoutSessionDto response = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .maxParticipants(15)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.updateSession(eq(id), any(WorkoutSessionDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/sessions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("WEDNESDAY"));
    }

    @Test
    void patchSession_ShouldReturnUpdatedSession() throws Exception {
        Long id = 1L;
        WorkoutSessionDto patchDto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(12)
                .build();

        WorkoutSessionDto response = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(12)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.updateSession(eq(id), any(WorkoutSessionDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/sessions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxParticipants").value(12));
    }

    @Test
    void updateSessionStatus_ShouldReturnUpdatedSession() throws Exception {
        Long id = 1L;
        WorkoutSessionStatus status = WorkoutSessionStatus.CONFIRMED;

        WorkoutSessionDto response = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(status)
                .build();

        when(workoutSessionService.updateSessionStatus(id, status)).thenReturn(response);

        mockMvc.perform(patch("/api/sessions/{id}/status", id)
                        .param("status", status.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void deleteSession_ShouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(workoutSessionService).deleteSession(id);

        mockMvc.perform(delete("/api/sessions/{id}", id))
                .andExpect(status().isNoContent());

        verify(workoutSessionService).deleteSession(id);
    }

    @Test
    void getSessionsByTrainerNameAndDay_ShouldReturnPage() throws Exception {
        String lastName = "Смирнова";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        Page<WorkoutSessionDto> pageResult = new PageImpl<>(List.of(dto));

        when(workoutSessionService.getSessionsByTrainerLastNameAndDay(lastName, day, page, size))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/sessions/by-trainer-name-and-day")
                        .param("trainerLastName", lastName)
                        .param("dayOfWeek", day.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getSessionsByTrainerNameAndDayCached_ShouldReturnPage() throws Exception {
        String lastName = "Смирнова";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        Page<WorkoutSessionDto> pageResult = new PageImpl<>(List.of(dto));

        when(workoutSessionService.getSessionsByTrainerLastNameAndDayCached(lastName, day, page, size))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/sessions/by-trainer-name-and-day/cached")
                        .param("trainerLastName", lastName)
                        .param("dayOfWeek", day.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void createSessionsBulkWithTransaction_ShouldReturnCreatedSessions() throws Exception {
        WorkoutSessionDto dto1 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .build();

        WorkoutSessionDto dto2 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .maxParticipants(10)
                .build();

        WorkoutSessionBulkRequest request = WorkoutSessionBulkRequest.builder()
                .sessions(List.of(dto1, dto2))
                .build();

        WorkoutSessionDto response1 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        WorkoutSessionDto response2 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.createSessionsBulkWithTransaction(anyList()))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(post("/api/sessions/bulk/with-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void createSessionsBulkWithoutTransaction_ShouldReturnMultiStatus() throws Exception {
        WorkoutSessionDto dto1 = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .build();

        WorkoutSessionBulkRequest request = WorkoutSessionBulkRequest.builder()
                .sessions(List.of(dto1))
                .build();

        WorkoutSessionDto response = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        when(workoutSessionService.createSessionsBulkWithoutTransaction(anyList()))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/sessions/bulk/without-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateSessionStatus_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;
        WorkoutSessionStatus status = WorkoutSessionStatus.CONFIRMED;

        when(workoutSessionService.updateSessionStatus(id, status))
                .thenThrow(new ResourceNotFoundException("Тренировка с id 999 не найдена"));

        mockMvc.perform(patch("/api/sessions/{id}/status", id)
                        .param("status", status.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Тренировка с id 999 не найдена"));
    }

    @Test
    void getSessionsByTrainerNameAndDayNative_ShouldReturnPage() throws Exception {
        String lastName = "Смирнова";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(day)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.SCHEDULED)
                .build();

        Page<WorkoutSessionDto> pageResult = new PageImpl<>(List.of(dto));

        when(workoutSessionService.getSessionsByTrainerLastNameAndDayNative(lastName, day, page, size))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/sessions/by-trainer-name-and-day-native")
                        .param("trainerLastName", lastName)
                        .param("dayOfWeek", day.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getSessionsByTrainerNameAndDayNative_ShouldReturnEmptyPage() throws Exception {
        String lastName = "НесуществующаяФамилия";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        Page<WorkoutSessionDto> emptyPage = new PageImpl<>(List.of());

        when(workoutSessionService.getSessionsByTrainerLastNameAndDayNative(lastName, day, page, size))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/sessions/by-trainer-name-and-day-native")
                        .param("trainerLastName", lastName)
                        .param("dayOfWeek", day.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void createSessionsBulkWithoutTransaction_ShouldReturnBadRequest_WhenAllFail() throws Exception {
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .build();

        WorkoutSessionBulkRequest request = WorkoutSessionBulkRequest.builder()
                .sessions(List.of(dto))
                .build();

        when(workoutSessionService.createSessionsBulkWithoutTransaction(anyList()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/sessions/bulk/without-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



}
