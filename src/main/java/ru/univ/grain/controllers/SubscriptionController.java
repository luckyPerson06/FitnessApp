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
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.services.SubscriptionService;

import java.util.List;

@Tag(name = "Абонементы", description = "Управление абонементами")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Получить все абонементы")
    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @Operation(summary = "Получить абонемент по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Абонемент найден"),
            @ApiResponse(responseCode = "404", description = "Абонемент не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@Parameter(description = "ID абонемента", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @Operation(summary = "Получить абонементы по типу")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByType(@Parameter(description = "Тип абонемента", example = "LIMITED") @PathVariable SubscriptionType type) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByType(type));
    }

    @Operation(summary = "Получить абонементы по статусу")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByStatus(@Parameter(description = "Статус абонемента", example = "ACTIVE") @PathVariable SubscriptionStatus status) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByStatus(status));
    }

    @Operation(summary = "Получить абонементы по типу тренировки")
    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByWorkoutType(@Parameter(description = "ID типа тренировки", example = "1") @PathVariable Long workoutTypeId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId));
    }

    @Operation(summary = "Получить активные абонементы")
    @GetMapping("/active")
    public ResponseEntity<List<SubscriptionDto>> getActiveSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
    }

    @Operation(summary = "Получить истекшие абонементы")
    @GetMapping("/expired")
    public ResponseEntity<List<SubscriptionDto>> getExpiredSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getExpiredSubscriptions());
    }

    @Operation(summary = "Получить отменённые абонементы")
    @GetMapping("/cancelled")
    public ResponseEntity<List<SubscriptionDto>> getCancelledSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getCancelledSubscriptions());
    }

    @Operation(summary = "Получить использованные абонементы")
    @GetMapping("/used")
    public ResponseEntity<List<SubscriptionDto>> getUsedSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getUsedSubscriptions());
    }

    @Operation(summary = "Создать новый абонемент")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Абонемент создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Абонемент с таким названием уже существует", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createSubscription(dto));
    }

    @Operation(summary = "Полное обновление абонемента")
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> updateSubscription(
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long id,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @Operation(summary = "Частичное обновление абонемента")
    @PatchMapping("/{id}")
    public ResponseEntity<SubscriptionDto> patchSubscription(
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long id,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @Operation(summary = "Удалить абонемент")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Абонемент удалён"),
            @ApiResponse(responseCode = "404", description = "Абонемент не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@Parameter(description = "ID абонемента", example = "1") @PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пометить абонемент как истекший")
    @PostMapping("/{id}/expire")
    public ResponseEntity<SubscriptionDto> expireSubscription(@Parameter(description = "ID абонемента", example = "1") @PathVariable Long id) {
        subscriptionService.expireSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @Operation(summary = "Добавить тип тренировки к абонементу")
    @PostMapping("/{subscriptionId}/workout-types/{workoutTypeId}")
    public ResponseEntity<SubscriptionDto> addWorkoutType(
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId,
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable Long workoutTypeId) {
        subscriptionService.addWorkoutType(subscriptionId, workoutTypeId);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(subscriptionId));
    }

    @Operation(summary = "Удалить тип тренировки из абонемента")
    @DeleteMapping("/{subscriptionId}/workout-types/{workoutTypeId}")
    public ResponseEntity<SubscriptionDto> removeWorkoutType(
            @Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId,
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable Long workoutTypeId) {
        subscriptionService.removeWorkoutType(subscriptionId, workoutTypeId);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(subscriptionId));
    }

    @Operation(summary = "Проверить существование абонемента по названию")
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@Parameter(description = "Название абонемента", example = "Базовый") @PathVariable String name) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByName(name) != null);
    }
}
