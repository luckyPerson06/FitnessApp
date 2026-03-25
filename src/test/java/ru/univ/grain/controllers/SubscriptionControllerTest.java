package ru.univ.grain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.services.SubscriptionService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @Test
    void getAllSubscriptions_ShouldReturnList() throws Exception {
        SubscriptionDto sub1 = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        SubscriptionDto sub2 = SubscriptionDto.builder()
                .name("Премиум")
                .price(BigDecimal.valueOf(5000))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(sub1, sub2));

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Базовый"))
                .andExpect(jsonPath("$[1].name").value("Премиум"));
    }

    @Test
    void getAllSubscriptions_ShouldReturnEmptyList() throws Exception {
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSubscriptionById_ShouldReturnSubscription() throws Exception {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.getSubscriptionById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/subscriptions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Базовый"));
    }

    @Test
    void getSubscriptionById_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        when(subscriptionService.getSubscriptionById(id))
                .thenThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"));

        mockMvc.perform(get("/api/subscriptions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Абонемент с id 999 не найден"));
    }

    @Test
    void getSubscriptionsByType_ShouldReturnList() throws Exception {
        SubscriptionType type = SubscriptionType.UNLIMITED;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Премиум")
                .price(BigDecimal.valueOf(5000))
                .subscriptionType(type)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.getSubscriptionsByType(type)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/type/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].subscriptionType").value("UNLIMITED"));
    }

    @Test
    void getSubscriptionsByType_ShouldReturnEmptyList() throws Exception {
        SubscriptionType type = SubscriptionType.LIMITED;

        when(subscriptionService.getSubscriptionsByType(type)).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/type/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSubscriptionsByStatus_ShouldReturnList() throws Exception {
        SubscriptionStatus status = SubscriptionStatus.ACTIVE;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(status)
                .build();

        when(subscriptionService.getSubscriptionsByStatus(status)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getSubscriptionsByStatus_ShouldReturnEmptyList() throws Exception {
        SubscriptionStatus status = SubscriptionStatus.EXPIRED;

        when(subscriptionService.getSubscriptionsByStatus(status)).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSubscriptionsByWorkoutType_ShouldReturnList() throws Exception {
        Long workoutTypeId = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/workout-type/{workoutTypeId}", workoutTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getSubscriptionsByWorkoutType_ShouldReturnEmptyList() throws Exception {
        Long workoutTypeId = 999L;

        when(subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId)).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/workout-type/{workoutTypeId}", workoutTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getActiveSubscriptions_ShouldReturnList() throws Exception {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.getActiveSubscriptions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getActiveSubscriptions_ShouldReturnEmptyList() throws Exception {
        when(subscriptionService.getActiveSubscriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getExpiredSubscriptions_ShouldReturnList() throws Exception {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Просроченный")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.EXPIRED)
                .build();

        when(subscriptionService.getExpiredSubscriptions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getExpiredSubscriptions_ShouldReturnEmptyList() throws Exception {
        when(subscriptionService.getExpiredSubscriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCancelledSubscriptions_ShouldReturnList() throws Exception {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Отмененный")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.CANCELLED)
                .build();

        when(subscriptionService.getCancelledSubscriptions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/cancelled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getCancelledSubscriptions_ShouldReturnEmptyList() throws Exception {
        when(subscriptionService.getCancelledSubscriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/cancelled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUsedSubscriptions_ShouldReturnList() throws Exception {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Использованный")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.USED)
                .build();

        when(subscriptionService.getUsedSubscriptions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/subscriptions/used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUsedSubscriptions_ShouldReturnEmptyList() throws Exception {
        when(subscriptionService.getUsedSubscriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createSubscription_ShouldReturnCreatedSubscription() throws Exception {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Новый абонемент")
                .price(BigDecimal.valueOf(4000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(10)
                .durationDays(30)
                .build();

        SubscriptionDto response = SubscriptionDto.builder()
                .name("Новый абонемент")
                .price(BigDecimal.valueOf(4000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(10)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.createSubscription(any(SubscriptionDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Новый абонемент"));
    }

    @Test
    void createSubscription_ShouldReturn400_WhenInvalidData() throws Exception {
        SubscriptionDto invalidDto = SubscriptionDto.builder()
                .name("")
                .price(BigDecimal.valueOf(-100))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(null)
                .durationDays(0)
                .build();

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void updateSubscription_ShouldReturnUpdatedSubscription() throws Exception {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Обновленный абонемент")
                .price(BigDecimal.valueOf(4500))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(60)
                .build();

        SubscriptionDto response = SubscriptionDto.builder()
                .name("Обновленный абонемент")
                .price(BigDecimal.valueOf(4500))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(60)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.updateSubscription(eq(id), any(SubscriptionDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/subscriptions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленный абонемент"));
    }

    @Test
    void updateSubscription_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Тест")
                .price(BigDecimal.valueOf(1000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(5)
                .durationDays(30)
                .build();

        when(subscriptionService.updateSubscription(eq(id), any(SubscriptionDto.class)))
                .thenThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"));

        mockMvc.perform(put("/api/subscriptions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchSubscription_ShouldReturnUpdatedSubscription() throws Exception {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3500))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .build();

        SubscriptionDto response = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3500))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionService.updateSubscription(eq(id), any(SubscriptionDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/subscriptions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(3500));
    }

    @Test
    void patchSubscription_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Тест")
                .price(BigDecimal.valueOf(1500))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(5)
                .durationDays(30)
                .build();

        when(subscriptionService.updateSubscription(eq(id), any(SubscriptionDto.class)))
                .thenThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"));

        mockMvc.perform(patch("/api/subscriptions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSubscription_ShouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(subscriptionService).deleteSubscription(id);

        mockMvc.perform(delete("/api/subscriptions/{id}", id))
                .andExpect(status().isNoContent());

        verify(subscriptionService).deleteSubscription(id);
    }

    @Test
    void deleteSubscription_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        doThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"))
                .when(subscriptionService).deleteSubscription(id);

        mockMvc.perform(delete("/api/subscriptions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void expireSubscription_ShouldReturnUpdatedSubscription() throws Exception {
        Long id = 1L;
        SubscriptionDto response = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.EXPIRED)
                .build();

        doNothing().when(subscriptionService).expireSubscription(id);
        when(subscriptionService.getSubscriptionById(id)).thenReturn(response);

        mockMvc.perform(post("/api/subscriptions/{id}/expire", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    @Test
    void expireSubscription_ShouldReturn404_WhenNotFound() throws Exception {
        Long id = 999L;

        doThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"))
                .when(subscriptionService).expireSubscription(id);

        mockMvc.perform(post("/api/subscriptions/{id}/expire", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void addWorkoutType_ShouldReturnOk() throws Exception {
        Long subscriptionId = 1L;
        Long workoutTypeId = 1L;
        SubscriptionDto response = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        doNothing().when(subscriptionService).addWorkoutType(subscriptionId, workoutTypeId);
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(response);

        mockMvc.perform(post("/api/subscriptions/{subscriptionId}/workout-types/{workoutTypeId}", subscriptionId, workoutTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Базовый"));
    }

    @Test
    void addWorkoutType_ShouldReturn404_WhenSubscriptionNotFound() throws Exception {
        Long subscriptionId = 999L;
        Long workoutTypeId = 1L;

        doThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"))
                .when(subscriptionService).addWorkoutType(subscriptionId, workoutTypeId);

        mockMvc.perform(post("/api/subscriptions/{subscriptionId}/workout-types/{workoutTypeId}", subscriptionId, workoutTypeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeWorkoutType_ShouldReturnOk() throws Exception {
        Long subscriptionId = 1L;
        Long workoutTypeId = 1L;
        SubscriptionDto response = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        doNothing().when(subscriptionService).removeWorkoutType(subscriptionId, workoutTypeId);
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(response);

        mockMvc.perform(delete("/api/subscriptions/{subscriptionId}/workout-types/{workoutTypeId}", subscriptionId, workoutTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Базовый"));
    }

    @Test
    void removeWorkoutType_ShouldReturn404_WhenSubscriptionNotFound() throws Exception {
        Long subscriptionId = 999L;
        Long workoutTypeId = 1L;

        doThrow(new ResourceNotFoundException("Абонемент с id 999 не найден"))
                .when(subscriptionService).removeWorkoutType(subscriptionId, workoutTypeId);

        mockMvc.perform(delete("/api/subscriptions/{subscriptionId}/workout-types/{workoutTypeId}", subscriptionId, workoutTypeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void existsByName_ShouldReturnTrue() throws Exception {
        String name = "Базовый";
        SubscriptionDto dto = SubscriptionDto.builder().name(name).build();

        when(subscriptionService.getSubscriptionByName(name)).thenReturn(dto);

        mockMvc.perform(get("/api/subscriptions/exists/name/{name}", name))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsByName_ShouldReturnFalse() throws Exception {
        String name = "Несуществующий";

        when(subscriptionService.getSubscriptionByName(name))
                .thenThrow(new ResourceNotFoundException("Абонемент с названием '" + name + "' не найден"));

        mockMvc.perform(get("/api/subscriptions/exists/name/{name}", name))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Абонемент с названием '" + name + "' не найден"));
    }
}