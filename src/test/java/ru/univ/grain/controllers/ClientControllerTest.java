package ru.univ.grain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.services.ClientService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    @Test
    void getAllClients_ShouldReturnListOfClients() throws Exception {
        ClientResponseDto client1 = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();
        ClientResponseDto client2 = ClientResponseDto.builder()
                .id(2L)
                .fullName("Петров Петр Петрович")
                .email("petr@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getAllClients()).thenReturn(List.of(client1, client2));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("ivan@mail.com"))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllClients_ShouldReturnEmptyList_WhenNoClients() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientById_ShouldReturnClient_WhenExists() throws Exception {
        Long clientId = 1L;
        ClientResponseDto client = ClientResponseDto.builder()
                .id(clientId)
                .fullName("Иванов Иван Иванович")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getClientResponseById(clientId)).thenReturn(client);

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void getClientById_ShouldReturn404_WhenNotFound() throws Exception {
        Long clientId = 999L;

        when(clientService.getClientResponseById(clientId))
                .thenThrow(new ResourceNotFoundException("Клиент с id 999 не найден"));

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Клиент с id 999 не найден"));
    }

    @Test
    void getClientByEmail_ShouldReturnClient_WhenExists() throws Exception {
        String email = "ivan@mail.com";
        ClientResponseDto client = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .email(email)
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getClientByEmail(email)).thenReturn(client);

        mockMvc.perform(get("/api/clients/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void getClientByEmail_ShouldReturn404_WhenNotFound() throws Exception {
        String email = "notfound@mail.com";

        when(clientService.getClientByEmail(email))
                .thenThrow(new ResourceNotFoundException("Клиент с email notfound@mail.com не найден"));

        mockMvc.perform(get("/api/clients/email/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Клиент с email notfound@mail.com не найден"));
    }

    @Test
    void getClientsByLastName_ShouldReturnList() throws Exception {
        String lastName = "Иванов";
        ClientResponseDto client = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getClientsByLastName(lastName)).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients/lastname/{lastName}", lastName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getClientsByLastName_ShouldReturnEmptyList_WhenNoClients() throws Exception {
        String lastName = "НесуществующаяФамилия";

        when(clientService.getClientsByLastName(lastName)).thenReturn(List.of());

        mockMvc.perform(get("/api/clients/lastname/{lastName}", lastName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientsByStatus_ShouldReturnList() throws Exception {
        ClientStatus status = ClientStatus.ACTIVE;
        ClientResponseDto client = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .email("ivan@mail.com")
                .status(status)
                .build();

        when(clientService.getClientsByStatus(status)).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getClientsByStatus_ShouldReturnEmptyList_WhenNoClients() throws Exception {
        ClientStatus status = ClientStatus.BLOCKED;

        when(clientService.getClientsByStatus(status)).thenReturn(List.of());

        mockMvc.perform(get("/api/clients/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientsWithActiveSubscriptions_ShouldReturnList() throws Exception {
        ClientResponseDto client = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getClientsWithActiveSubscriptions()).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients/active-subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getClientsWithActiveSubscriptions_ShouldReturnEmptyList_WhenNoClients() throws Exception {
        when(clientService.getClientsWithActiveSubscriptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients/active-subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getBookedClientsForSession_ShouldReturnList() throws Exception {
        Long sessionId = 1L;
        ClientResponseDto client = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван Иванович")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getBookedClientsForSession(sessionId)).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients/booked-session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getBookedClientsForSession_ShouldReturnEmptyList_WhenNoClients() throws Exception {
        Long sessionId = 1L;

        when(clientService.getBookedClientsForSession(sessionId)).thenReturn(List.of());

        mockMvc.perform(get("/api/clients/booked-session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createClient_ShouldReturnCreatedClient() throws Exception {
        ClientDto dto = ClientDto.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .email("ivan@mail.com")
                .password("password123")
                .phoneNumber("+79991234567")
                .build();

        ClientResponseDto response = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.createClient(any(ClientDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void createClient_ShouldReturn400_WhenInvalidData() throws Exception {
        ClientDto invalidDto = ClientDto.builder()
                .firstName("")
                .lastName("")
                .email("invalid")
                .build();

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void patchClient_ShouldUpdateClient() throws Exception {
        Long clientId = 1L;
        ClientPatchDto patchDto = ClientPatchDto.builder()
                .firstName("Анна")
                .phoneNumber("+79998887766")
                .build();

        ClientResponseDto response = ClientResponseDto.builder()
                .id(clientId)
                .fullName("Иванова Анна")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.updateClient(eq(clientId), any(ClientPatchDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId));
    }

    @Test
    void patchClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        Long clientId = 999L;
        ClientPatchDto patchDto = ClientPatchDto.builder()
                .firstName("Анна")
                .build();

        when(clientService.updateClient(eq(clientId), any(ClientPatchDto.class)))
                .thenThrow(new ResourceNotFoundException("Клиент с id 999 не найден"));

        mockMvc.perform(patch("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClient_ShouldReturnUpdatedClient() throws Exception {
        Long clientId = 1L;
        ClientPatchDto patchDto = ClientPatchDto.builder()
                .firstName("Анна")
                .lastName("Иванова")
                .phoneNumber("+79998887766")
                .build();

        ClientResponseDto response = ClientResponseDto.builder()
                .id(clientId)
                .fullName("Иванова Анна")
                .email("anna@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.updateClient(eq(clientId), any(ClientPatchDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId));
    }

    @Test
    void updateClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        Long clientId = 999L;
        ClientPatchDto patchDto = ClientPatchDto.builder()
                .firstName("Анна")
                .lastName("Иванова")
                .build();

        when(clientService.updateClient(eq(clientId), any(ClientPatchDto.class)))
                .thenThrow(new ResourceNotFoundException("Клиент с id 999 не найден"));

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient_ShouldReturnNoContent() throws Exception {
        Long clientId = 1L;

        doNothing().when(clientService).deleteClientById(clientId);

        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClientById(clientId);
    }

    @Test
    void deleteClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        Long clientId = 999L;

        doThrow(new ResourceNotFoundException("Клиент с id 999 не найден"))
                .when(clientService).deleteClientById(clientId);

        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addSubscriptionToClient_ShouldReturnUpdatedClient() throws Exception {
        Long clientId = 1L;
        Long subscriptionId = 1L;

        ClientResponseDto response = ClientResponseDto.builder()
                .id(clientId)
                .fullName("Иванов Иван")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.addSubscriptionToClient(clientId, subscriptionId)).thenReturn(response);

        mockMvc.perform(post("/api/clients/{clientId}/subscriptions/{subscriptionId}", clientId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId));
    }

    @Test
    void addSubscriptionToClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        Long clientId = 999L;
        Long subscriptionId = 1L;

        when(clientService.addSubscriptionToClient(clientId, subscriptionId))
                .thenThrow(new ResourceNotFoundException("Клиент с id 999 не найден"));

        mockMvc.perform(post("/api/clients/{clientId}/subscriptions/{subscriptionId}", clientId, subscriptionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeSubscriptionFromClient_ShouldReturnUpdatedClient() throws Exception {
        Long clientId = 1L;
        Long subscriptionId = 1L;

        ClientResponseDto response = ClientResponseDto.builder()
                .id(clientId)
                .fullName("Иванов Иван")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.removeSubscriptionFromClient(clientId, subscriptionId)).thenReturn(response);

        mockMvc.perform(delete("/api/clients/{clientId}/subscriptions/{subscriptionId}", clientId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId));
    }

    @Test
    void removeSubscriptionFromClient_ShouldReturn404_WhenClientNotFound() throws Exception {
        Long clientId = 999L;
        Long subscriptionId = 1L;

        when(clientService.removeSubscriptionFromClient(clientId, subscriptionId))
                .thenThrow(new ResourceNotFoundException("Клиент с id 999 не найден"));

        mockMvc.perform(delete("/api/clients/{clientId}/subscriptions/{subscriptionId}", clientId, subscriptionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void existsByEmail_ShouldReturnTrue() throws Exception {
        String email = "ivan@mail.com";

        when(clientService.existsByEmail(email)).thenReturn(true);

        mockMvc.perform(get("/api/clients/exists/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsByEmail_ShouldReturnFalse() throws Exception {
        String email = "notfound@mail.com";

        when(clientService.existsByEmail(email)).thenReturn(false);

        mockMvc.perform(get("/api/clients/exists/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void createClientWithNewSubscription_ShouldReturnCreatedClient() throws Exception {
        ClientDto clientDto = ClientDto.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .email("ivan@mail.com")
                .password("password123")
                .build();

        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .name("Базовый")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .build();

        ClientWithSubscriptionRequest request = ClientWithSubscriptionRequest.builder()
                .client(clientDto)
                .subscription(subscriptionDto)
                .build();

        ClientResponseDto response = ClientResponseDto.builder()
                .id(1L)
                .fullName("Иванов Иван")
                .email("ivan@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.createClientWithNewSubscription(any(ClientDto.class), any(SubscriptionDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/clients/with-new-subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createClientWithNewSubscription_ShouldReturn400_WhenInvalidData() throws Exception {
        ClientDto invalidClient = ClientDto.builder()
                .firstName("")
                .email("invalid")
                .build();

        SubscriptionDto invalidSubscription = SubscriptionDto.builder()
                .name("")
                .price(BigDecimal.valueOf(-100))
                .build();

        ClientWithSubscriptionRequest request = ClientWithSubscriptionRequest.builder()
                .client(invalidClient)
                .subscription(invalidSubscription)
                .build();

        mockMvc.perform(post("/api/clients/with-new-subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
