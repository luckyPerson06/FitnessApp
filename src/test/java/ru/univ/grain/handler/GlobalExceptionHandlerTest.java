package ru.univ.grain.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.controllers.ClientController;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.services.ClientService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    @Test
    void handleResourceNotFound_ShouldReturn404() throws Exception {
        Long clientId = 999L;
        when(clientService.getClientResponseById(clientId))
                .thenThrow(new ResourceNotFoundException("Клиент с id " + clientId + " не найден"));

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Клиент с id 999 не найден"))
                .andExpect(jsonPath("$.path").value("/api/clients/999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleDuplicateResource_ShouldReturn409() throws Exception {
        ClientDto dto = ClientDto.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .email("existing@mail.com")
                .password("password")
                .build();

        when(clientService.createClient(any(ClientDto.class)))
                .thenThrow(new DuplicateResourceException("Клиент с email existing@mail.com уже существует"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Клиент с email existing@mail.com уже существует"));
    }

    @Test
    void handleBusinessException_ShouldReturn400() throws Exception {
        Long clientId = 1L;
        Long subscriptionId = 1L;

        when(clientService.addSubscriptionToClient(clientId, subscriptionId))
                .thenThrow(new BusinessException("Клиент уже имеет этот абонемент"));

        mockMvc.perform(post("/api/clients/{clientId}/subscriptions/{subscriptionId}", clientId, subscriptionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Клиент уже имеет этот абонемент"));
    }

    @Test
    void handleValidationExceptions_ShouldReturn400WithValidationErrors() throws Exception {
        ClientDto invalidDto = ClientDto.builder()
                .firstName("")
                .lastName("Иванов")
                .email("invalid-email")
                .password("pass")
                .build();

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ошибка валидации"))
                .andExpect(jsonPath("$.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void handleTypeMismatch_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/clients/status/{status}", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("имеет неверный тип")));
    }

    @Test
    void handleMessageNotReadable_ShouldReturn400_WhenInvalidJson() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Неверный формат запроса. Проверьте структуру JSON"));
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturn409() throws Exception {
        ClientDto dto = ClientDto.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .email("duplicate@mail.com")
                .password("password")
                .build();

        when(clientService.createClient(any(ClientDto.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("Запись с такими данными уже существует")));
    }

    @Test
    void handleGenericException_ShouldReturn500() throws Exception {
        Long clientId = 1L;
        when(clientService.getClientResponseById(clientId))
                .thenThrow(new RuntimeException("Неожиданная ошибка"));

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"));
    }
}