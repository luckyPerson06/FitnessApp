package ru.univ.grain.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.*;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.services.ClientService;

import java.util.List;

@Tag(name = "Клиенты", description = "Управление клиентами фитнес-клуба")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Получить всех клиентов", description = "Возвращает список всех клиентов")
    @GetMapping
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @Operation(summary = "Получить клиента по ID", description = "Возвращает клиента по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент найден"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getClientById(@Parameter(description = "ID клиента", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientResponseById(id));
    }

    @Operation(summary = "Получить клиента по email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент найден"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDto> getClientByEmail(@Parameter(description = "Email клиента", example = "ivan@mail.com") @PathVariable String email) {
        return ResponseEntity.ok(clientService.getClientByEmail(email));
    }

    @Operation(summary = "Найти клиентов по фамилии")
    @GetMapping("/lastname/{lastName}")
    public ResponseEntity<List<ClientResponseDto>> getClientsByLastName(@Parameter(description = "Фамилия клиента", example = "Иванов") @PathVariable String lastName) {
        return ResponseEntity.ok(clientService.getClientsByLastName(lastName));
    }

    @Operation(summary = "Найти клиентов по статусу")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClientResponseDto>> getClientsByStatus(@Parameter(description = "Статус клиента", example = "ACTIVE") @PathVariable ClientStatus status) {
        return ResponseEntity.ok(clientService.getClientsByStatus(status));
    }

    @Operation(summary = "Получить клиентов с активными абонементами")
    @GetMapping("/active-subscriptions")
    public ResponseEntity<List<ClientResponseDto>> getClientsWithActiveSubscriptions() {
        return ResponseEntity.ok(clientService.getClientsWithActiveSubscriptions());
    }

    @Operation(summary = "Получить клиентов, записанных на тренировку")
    @GetMapping("/booked-session/{sessionId}")
    public ResponseEntity<List<ClientResponseDto>> getBookedClientsForSession(@Parameter(description = "ID тренировки", example = "1") @PathVariable Long sessionId) {
        return ResponseEntity.ok(clientService.getBookedClientsForSession(sessionId));
    }

    @Operation(summary = "Создать нового клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Клиент создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Клиент с таким email уже существует", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<ClientResponseDto> createClient(@Valid @RequestBody ClientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(dto));
    }

    @Operation(summary = "Частичное обновление клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент обновлён"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponseDto> patchClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long id,
            @Valid @RequestBody ClientPatchDto dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @Operation(summary = "Полное обновление клиента")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> updateClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long id,
            @Valid @RequestBody ClientPatchDto dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @Operation(summary = "Удалить клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Клиент удалён"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@Parameter(description = "ID клиента", example = "1") @PathVariable Long id) {
        clientService.deleteClientById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить абонемент клиенту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Абонемент добавлен"),
            @ApiResponse(responseCode = "404", description = "Клиент или абонемент не найдены", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{clientId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ClientResponseDto> addSubscriptionToClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId,
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(clientService.addSubscriptionToClient(clientId, subscriptionId));
    }

    @Operation(summary = "Удалить абонемент у клиента")
    @DeleteMapping("/{clientId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ClientResponseDto> removeSubscriptionFromClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId,
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(clientService.removeSubscriptionFromClient(clientId, subscriptionId));
    }

    @Operation(summary = "Проверить существование клиента по email")
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@Parameter(description = "Email клиента", example = "ivan@mail.com") @PathVariable String email) {
        return ResponseEntity.ok(clientService.existsByEmail(email));
    }

    @Operation(summary = "Создать клиента с новым абонементом (с транзакцией)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Клиент и абонемент созданы"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Клиент или абонемент уже существует", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/with-new-subscription")
    public ResponseEntity<ClientResponseDto> createClientWithNewSubscription(
            @Valid @RequestBody ClientWithSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClientWithNewSubscription(
                request.getClient(), request.getSubscription()));
    }
}
