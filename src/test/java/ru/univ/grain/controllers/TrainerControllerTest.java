package ru.univ.grain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.TrainerStatus;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.services.TrainerService;

import java.time.DayOfWeek;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerService trainerService;

    @Test
    void getAllTrainers_ShouldReturnList() throws Exception {
        TrainerDto trainer1 = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(TrainerStatus.ACTIVE)
                .build();

        TrainerDto trainer2 = TrainerDto.builder()
                .firstName("Иван")
                .lastName("Петров")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.getAllTrainers()).thenReturn(List.of(trainer1, trainer2));

        mockMvc.perform(get("/api/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Анна"))
                .andExpect(jsonPath("$[1].firstName").value("Иван"));
    }

    @Test
    void getTrainerById_ShouldReturnTrainer() throws Exception {
        Long id = 1L;
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.getTrainerById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/trainers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Анна"));
    }

    @Test
    void getTrainerById_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        when(trainerService.getTrainerById(id))
                .thenThrow(new ResourceNotFoundException("Тренер с id 999 не найден"));

        mockMvc.perform(get("/api/trainers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrainersByStatus_ShouldReturnList() throws Exception {
        TrainerStatus status = TrainerStatus.ACTIVE;
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(status)
                .build();

        when(trainerService.getTrainersByStatus(status)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/trainers/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getActiveTrainers_ShouldReturnList() throws Exception {
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.getActiveTrainers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/trainers/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTrainersBySpecialization_ShouldReturnList() throws Exception {
        String specialization = "Йога";
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.getTrainersBySpecialization(specialization)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/trainers/specialization/{name}", specialization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTrainersWithSessionOnDay_ShouldReturnList() throws Exception {
        DayOfWeek day = DayOfWeek.MONDAY;
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.getTrainersWithSessionOnDay(day)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/trainers/day/{dayOfWeek}", day))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createTrainer_ShouldReturnCreatedTrainer() throws Exception {
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .description("Опытный тренер по йоге")
                .build();

        TrainerDto response = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.createTrainer(any(TrainerDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Анна"));
    }

    @Test
    void updateTrainer_ShouldReturnUpdatedTrainer() throws Exception {
        Long id = 1L;
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .description("Обновленное описание")
                .status(TrainerStatus.ACTIVE)
                .build();

        TrainerDto response = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .description("Обновленное описание")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.updateTrainer(eq(id), any(TrainerDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/trainers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Анна"));
    }

    @Test
    void patchTrainer_ShouldReturnUpdatedTrainer() throws Exception {
        Long id = 1L;
        TrainerDto patchDto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова") 
                .description("Частичное обновление")
                .build();

        TrainerDto response = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .description("Частичное обновление")
                .status(TrainerStatus.ACTIVE)
                .build();

        when(trainerService.updateTrainer(eq(id), any(TrainerDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/trainers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Частичное обновление"));
    }

    @Test
    void deleteTrainer_ShouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(trainerService).deleteTrainer(id);

        mockMvc.perform(delete("/api/trainers/{id}", id))
                .andExpect(status().isNoContent());

        verify(trainerService).deleteTrainer(id);
    }

    @Test
    void addSpecialization_ShouldReturnOk() throws Exception {
        Long trainerId = 1L;
        Long workoutTypeId = 1L;

        doNothing().when(trainerService).addSpecialization(trainerId, workoutTypeId);

        mockMvc.perform(post("/api/trainers/{trainerId}/specializations/{workoutTypeId}", trainerId, workoutTypeId))
                .andExpect(status().isOk());

        verify(trainerService).addSpecialization(trainerId, workoutTypeId);
    }

    @Test
    void removeSpecialization_ShouldReturnNoContent() throws Exception {
        Long trainerId = 1L;
        Long workoutTypeId = 1L;

        doNothing().when(trainerService).removeSpecialization(trainerId, workoutTypeId);

        mockMvc.perform(delete("/api/trainers/{trainerId}/specializations/{workoutTypeId}", trainerId, workoutTypeId))
                .andExpect(status().isNoContent());

        verify(trainerService).removeSpecialization(trainerId, workoutTypeId);
    }

    @Test
    void demonstrateNPlus1Problem_ShouldReturnStats() throws Exception {
        int[] nPlus1Stats = {3, 5, 2};
        int[] solutionStats = {3, 5, 2};

        when(trainerService.demonstrateNPlus1Problem()).thenReturn(nPlus1Stats);
        when(trainerService.demonstrateSolution()).thenReturn(solutionStats);

        mockMvc.perform(get("/api/trainers/demo/nplus1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['N+1 проблема (много запросов)'].[0]").value(3))
                .andExpect(jsonPath("$.['N+1 проблема (много запросов)'].[1]").value(5))
                .andExpect(jsonPath("$.['N+1 проблема (много запросов)'].[2]").value(2))
                .andExpect(jsonPath("$.['Решение (один запрос с JOIN)'].[0]").value(3))
                .andExpect(jsonPath("$.['Решение (один запрос с JOIN)'].[1]").value(5))
                .andExpect(jsonPath("$.['Решение (один запрос с JOIN)'].[2]").value(2));
    }
}
