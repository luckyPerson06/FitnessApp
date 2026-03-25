package ru.univ.grain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.WorkoutCategory;
import ru.univ.grain.services.WorkoutTypeService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutTypeController.class)
class WorkoutTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkoutTypeService workoutTypeService;

    @Test
    void getAllWorkoutTypes_ShouldReturnList() throws Exception {
        WorkoutTypeDto type1 = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        WorkoutTypeDto type2 = WorkoutTypeDto.builder()
                .name("Пилатес")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.getAllWorkoutTypes()).thenReturn(List.of(type1, type2));

        mockMvc.perform(get("/api/workout-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Йога"))
                .andExpect(jsonPath("$[1].name").value("Пилатес"));
    }

    @Test
    void getWorkoutTypeById_ShouldReturnWorkoutType() throws Exception {
        Long id = 1L;
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.getWorkoutTypeById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/workout-types/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Йога"));
    }

    @Test
    void getWorkoutTypeByName_ShouldReturnWorkoutType() throws Exception {
        String name = "Йога";
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name(name)
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.getWorkoutTypeByName(name)).thenReturn(dto);

        mockMvc.perform(get("/api/workout-types/name/{name}", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void getActiveWorkoutTypes_ShouldReturnList() throws Exception {
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.getActiveWorkoutTypes()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/workout-types/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getWorkoutTypesByCategory_ShouldReturnList() throws Exception {
        WorkoutCategory category = WorkoutCategory.GROUP;
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Йога")
                .category(category)
                .isActive(true)
                .build();

        when(workoutTypeService.getWorkoutTypesByCategory(category)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/workout-types/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("GROUP"));
    }

    @Test
    void getWorkoutTypesByTrainer_ShouldReturnList() throws Exception {
        Long trainerId = 1L;
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.getWorkoutTypesByTrainer(trainerId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/workout-types/trainer/{trainerId}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getWorkoutTypesBySubscription_ShouldReturnList() throws Exception {
        Long subscriptionId = 1L;
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.getWorkoutTypesBySubscription(subscriptionId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/workout-types/subscription/{subscriptionId}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createWorkoutType_ShouldReturnCreatedWorkoutType() throws Exception {
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Новый тип")
                .description("Описание нового типа")
                .category(WorkoutCategory.GROUP)
                .build();

        WorkoutTypeDto response = WorkoutTypeDto.builder()
                .name("Новый тип")
                .description("Описание нового типа")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .build();

        when(workoutTypeService.createWorkoutType(any(WorkoutTypeDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/workout-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Новый тип"));
    }

    @Test
    void updateWorkoutType_ShouldReturnUpdatedWorkoutType() throws Exception {
        Long id = 1L;
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Обновленный тип")
                .description("Обновленное описание")
                .category(WorkoutCategory.INDIVIDUAL)
                .build();

        WorkoutTypeDto response = WorkoutTypeDto.builder()
                .name("Обновленный тип")
                .description("Обновленное описание")
                .category(WorkoutCategory.INDIVIDUAL)
                .isActive(true)
                .build();

        when(workoutTypeService.updateWorkoutType(eq(id), any(WorkoutTypeDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/workout-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленный тип"));
    }

    @Test
    void patchWorkoutType_ShouldReturnUpdatedWorkoutType() throws Exception {
        Long id = 1L;
        WorkoutTypeDto patchDto = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .description("Новое описание")
                .build();

        WorkoutTypeDto response = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .description("Новое описание")
                .isActive(true)
                .build();

        when(workoutTypeService.updateWorkoutType(eq(id), any(WorkoutTypeDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/workout-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }

    @Test
    void deactivateWorkoutType_ShouldReturnUpdatedWorkoutType() throws Exception {
        Long id = 1L;

        WorkoutTypeDto response = WorkoutTypeDto.builder()
                .name("Йога")
                .category(WorkoutCategory.GROUP)
                .isActive(false)
                .build();

        doNothing().when(workoutTypeService).deactivateWorkoutType(id);
        when(workoutTypeService.getWorkoutTypeById(id)).thenReturn(response);

        mockMvc.perform(patch("/api/workout-types/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteWorkoutType_ShouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(workoutTypeService).deleteWorkoutType(id);

        mockMvc.perform(delete("/api/workout-types/{id}", id))
                .andExpect(status().isNoContent());

        verify(workoutTypeService).deleteWorkoutType(id);
    }

    @Test
    void existsByName_ShouldReturnTrue() throws Exception {
        String name = "Йога";

        when(workoutTypeService.existsByName(name)).thenReturn(true);

        mockMvc.perform(get("/api/workout-types/exists/name/{name}", name))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsByName_ShouldReturnFalse() throws Exception {
        String name = "Несуществующий";

        when(workoutTypeService.existsByName(name)).thenReturn(false);

        mockMvc.perform(get("/api/workout-types/exists/name/{name}", name))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
