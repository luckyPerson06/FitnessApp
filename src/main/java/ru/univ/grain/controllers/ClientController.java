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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.services.ClientService;

import java.util.List;

@Tag(name = "Клиенты", description = "Управление клиентами фитнес-клуба")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Получить всех клиентов")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @Operation(summary = "Получить клиента по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент найден"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> getClientById(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @Operation(summary = "Получить клиента по email")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> getClientByEmail(
            @Parameter(description = "Email клиента", example = "ivan@mail.com") @PathVariable String email) {
        return ResponseEntity.ok(clientService.getClientByEmail(email));
    }

    @Operation(summary = "Найти клиентов по фамилии")
    @GetMapping("/lastname/{lastName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponseDto>> getClientsByLastName(
            @Parameter(description = "Фамилия клиента", example = "Иванов") @PathVariable String lastName) {
        return ResponseEntity.ok(clientService.getClientsByLastName(lastName));
    }

    @Operation(summary = "Найти клиентов по статусу")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponseDto>> getClientsByStatus(
            @Parameter(description = "Статус клиента", example = "ACTIVE") @PathVariable ClientStatus status) {
        return ResponseEntity.ok(clientService.getClientsByStatus(status));
    }

    @Operation(summary = "Создать нового клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Клиент создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "409", description = "Клиент с таким email уже существует")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> createClient(@Valid @RequestBody ClientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(dto));
    }

    @Operation(summary = "Частичное обновление клиента")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> patchClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long id,
            @Valid @RequestBody ClientPatchDto dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @Operation(summary = "Полное обновление клиента")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> updateClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long id,
            @Valid @RequestBody ClientPatchDto dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @Operation(summary = "Удалить клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Клиент удалён"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить абонемент клиенту")
    @PostMapping("/{clientId}/subscriptions/{subscriptionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> addSubscriptionToClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId,
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(clientService.addSubscriptionToClient(clientId, subscriptionId));
    }

    @Operation(summary = "Удалить абонемент у клиента")
    @DeleteMapping("/{clientId}/subscriptions/{subscriptionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDto> removeSubscriptionFromClient(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId,
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(clientService.removeSubscriptionFromClient(clientId, subscriptionId));
    }

    @Operation(summary = "Проверить существование клиента по email")
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email клиента", example = "ivan@mail.com") @PathVariable String email) {
        return ResponseEntity.ok(clientService.existsByEmail(email));
    }
}
